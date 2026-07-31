import argparse
import json
import math
import mimetypes
import os
import re
import sys
import time
from pathlib import Path
from urllib.parse import parse_qs, urlparse

import requests


FIGMA_API_BASE = "https://api.figma.com/v1"
EXPORT_BATCH_SIZE = 50
MAX_REQUEST_RETRIES = 6
MAX_RETRY_DELAY_SECONDS = 60
MAX_AUTOMATED_RETRY_AFTER_SECONDS = 300
SEMANTIC_ASSET_KEYWORDS = (
    "logo",
    "icon",
    "illustration",
    "image",
    "img",
    "photo",
    "avatar",
    "fingerprint",
    "biometric",
    "vault",
    "safe",
    "complete",
    "success",
    "notification",
    "bell",
    "database",
    "category",
    "pin",
    "headset",
    "scan",
    "home",
    "settings",
    "folder",
    "document",
    "shield",
)
LEAF_ASSET_TYPES = {
    "VECTOR",
    "BOOLEAN_OPERATION",
    "STAR",
    "LINE",
    "ELLIPSE",
    "POLYGON",
    "REGULAR_POLYGON",
}


class FigmaRateLimitError(RuntimeError):
    pass


def parse_figma_url(url: str):
    parsed = urlparse(url)
    parts = parsed.path.strip("/").split("/")

    # Expected:
    # /design/<file_key>/<file_name>
    # /file/<file_key>/<file_name>
    if len(parts) < 2:
        raise ValueError("Không tìm thấy file key trong URL Figma.")

    file_key = parts[1]

    query = parse_qs(parsed.query)
    node_id = query.get("node-id", [None])[0]

    if node_id:
        node_id = node_id.replace("-", ":")

    return file_key, node_id


def round_num(value):
    if isinstance(value, (int, float)) and not isinstance(value, bool):
        if math.isfinite(value):
            return round(value, 2)
    return value


def clean_dict(obj):
    """
    Xóa key None / list rỗng / dict rỗng cho output gọn hơn.
    """
    if isinstance(obj, dict):
        result = {}
        for k, v in obj.items():
            cleaned = clean_dict(v)
            if cleaned is None:
                continue
            if cleaned == {}:
                continue
            if cleaned == []:
                continue
            result[k] = cleaned
        return result

    if isinstance(obj, list):
        return [clean_dict(x) for x in obj if clean_dict(x) not in (None, {}, [])]

    return obj


def slugify(value):
    value = re.sub(r"[^a-zA-Z0-9._-]+", "_", str(value).strip()).strip("_")
    return value or "asset"


def color_obj_to_hex(color, opacity=None):
    if not color:
        return None

    r = round(color.get("r", 0) * 255)
    g = round(color.get("g", 0) * 255)
    b = round(color.get("b", 0) * 255)

    a = color.get("a", 1)
    if opacity is not None:
        a = a * opacity

    if a < 1:
        return {
            "hex": f"#{r:02X}{g:02X}{b:02X}",
            "alpha": round(a, 3),
            "flutter": f"Color(0x{round(a * 255):02X}{r:02X}{g:02X}{b:02X})",
        }

    return {
        "hex": f"#{r:02X}{g:02X}{b:02X}",
        "alpha": 1,
        "flutter": f"Color(0xFF{r:02X}{g:02X}{b:02X})",
    }


def extract_paint(paint, image_assets=None):
    paint_type = paint.get("type")
    opacity = paint.get("opacity", 1)

    data = {
        "type": paint_type,
        "visible": paint.get("visible", True),
        "opacity": round_num(opacity),
        "blendMode": paint.get("blendMode"),
    }

    if paint_type == "SOLID":
        data["color"] = color_obj_to_hex(paint.get("color"), opacity)

    elif paint_type in ("GRADIENT_LINEAR", "GRADIENT_RADIAL", "GRADIENT_ANGULAR", "GRADIENT_DIAMOND"):
        stops = []
        for stop in paint.get("gradientStops", []):
            stops.append({
                "position": round_num(stop.get("position")),
                "color": color_obj_to_hex(stop.get("color"), opacity),
            })
        data["gradient"] = {
            "stops": stops,
            "transform": paint.get("gradientTransform"),
        }

    elif paint_type == "IMAGE":
        image_ref = paint.get("imageRef")
        data["image"] = {
            "scaleMode": paint.get("scaleMode"),
            "imageRef": image_ref,
            "rotation": paint.get("rotation"),
            "filters": paint.get("filters"),
        }
        if image_assets and image_ref in image_assets:
            data["image"]["asset"] = image_assets[image_ref]

    return clean_dict(data)


def extract_effect(effect):
    data = {
        "type": effect.get("type"),
        "visible": effect.get("visible", True),
        "radius": round_num(effect.get("radius")),
        "spread": round_num(effect.get("spread")),
        "offset": {
            "x": round_num((effect.get("offset") or {}).get("x")),
            "y": round_num((effect.get("offset") or {}).get("y")),
        },
        "color": color_obj_to_hex(effect.get("color")),
        "blendMode": effect.get("blendMode"),
    }
    return clean_dict(data)


def extract_arc_data(arc_data):
    if not isinstance(arc_data, dict):
        return None
    return clean_dict({
        "startingAngle": round_num(arc_data.get("startingAngle")),
        "endingAngle": round_num(arc_data.get("endingAngle")),
        "innerRadius": round_num(arc_data.get("innerRadius")),
    })


def extract_layout(node):
    data = {
        "layoutMode": node.get("layoutMode"),
        "layoutWrap": node.get("layoutWrap"),
        "primaryAxisSizingMode": node.get("primaryAxisSizingMode"),
        "counterAxisSizingMode": node.get("counterAxisSizingMode"),
        "primaryAxisAlignItems": node.get("primaryAxisAlignItems"),
        "counterAxisAlignItems": node.get("counterAxisAlignItems"),
        "counterAxisAlignContent": node.get("counterAxisAlignContent"),
        "itemSpacing": round_num(node.get("itemSpacing")),
        "counterAxisSpacing": round_num(node.get("counterAxisSpacing")),
        "paddingLeft": round_num(node.get("paddingLeft")),
        "paddingRight": round_num(node.get("paddingRight")),
        "paddingTop": round_num(node.get("paddingTop")),
        "paddingBottom": round_num(node.get("paddingBottom")),
        "layoutAlign": node.get("layoutAlign"),
        "layoutGrow": node.get("layoutGrow"),
        "layoutPositioning": node.get("layoutPositioning"),
        "layoutSizingHorizontal": node.get("layoutSizingHorizontal"),
        "layoutSizingVertical": node.get("layoutSizingVertical"),
        "minWidth": round_num(node.get("minWidth")),
        "maxWidth": round_num(node.get("maxWidth")),
        "minHeight": round_num(node.get("minHeight")),
        "maxHeight": round_num(node.get("maxHeight")),
    }
    return clean_dict(data)


def extract_constraints(node):
    return clean_dict({
        "constraints": node.get("constraints"),
        "layoutAlign": node.get("layoutAlign"),
        "layoutGrow": node.get("layoutGrow"),
        "layoutPositioning": node.get("layoutPositioning"),
    })


def extract_text(node):
    if node.get("type") != "TEXT":
        return None

    style = node.get("style", {})

    data = {
        "characters": node.get("characters"),
        "style": {
            "fontFamily": style.get("fontFamily"),
            "fontPostScriptName": style.get("fontPostScriptName"),
            "fontSize": round_num(style.get("fontSize")),
            "fontWeight": style.get("fontWeight"),
            "italic": style.get("italic"),
            "lineHeightPx": round_num(style.get("lineHeightPx")),
            "lineHeightPercent": round_num(style.get("lineHeightPercent")),
            "letterSpacing": round_num(style.get("letterSpacing")),
            "textAlignHorizontal": style.get("textAlignHorizontal"),
            "textAlignVertical": style.get("textAlignVertical"),
            "textAutoResize": style.get("textAutoResize"),
            "textCase": style.get("textCase"),
            "textDecoration": style.get("textDecoration"),
            "paragraphIndent": round_num(style.get("paragraphIndent")),
            "paragraphSpacing": round_num(style.get("paragraphSpacing")),
            "listSpacing": round_num(style.get("listSpacing")),
        },
        "characterStyleOverrides": node.get("characterStyleOverrides"),
        "styleOverrideTable": node.get("styleOverrideTable"),
        "lineTypes": node.get("lineTypes"),
        "lineIndentations": node.get("lineIndentations"),
        "textTruncation": node.get("textTruncation"),
        "maxLines": node.get("maxLines"),
    }

    return clean_dict(data)


def extract_node(node, image_assets=None, node_assets=None):
    box = node.get("absoluteBoundingBox")
    relative_box = node.get("absoluteRenderBounds")
    node_id = node.get("id")

    data = {
        "id": node_id,
        "name": node.get("name"),
        "type": node.get("type"),
        "visible": node.get("visible", True),
        "locked": node.get("locked", False),
        "opacity": round_num(node.get("opacity")),
        "blendMode": node.get("blendMode"),
        "scrollBehavior": node.get("scrollBehavior"),

        "size": {
            "width": round_num((box or {}).get("width")),
            "height": round_num((box or {}).get("height")),
        },

        "position": {
            "x": round_num((box or {}).get("x")),
            "y": round_num((box or {}).get("y")),
        },

        "renderBounds": {
            "x": round_num((relative_box or {}).get("x")),
            "y": round_num((relative_box or {}).get("y")),
            "width": round_num((relative_box or {}).get("width")),
            "height": round_num((relative_box or {}).get("height")),
        },

        "rotation": round_num(node.get("rotation")),
        "relativeTransform": node.get("relativeTransform"),
        "preserveRatio": node.get("preserveRatio"),
        "targetAspectRatio": node.get("targetAspectRatio"),
        "clipsContent": node.get("clipsContent"),
        "isMask": node.get("isMask"),
        "maskType": node.get("maskType"),
        "overflowDirection": node.get("overflowDirection"),
        "numberOfFixedChildren": node.get("numberOfFixedChildren"),

        "layout": extract_layout(node),
        "constraints": extract_constraints(node),
        "layoutGrids": node.get("layoutGrids"),

        "cornerRadius": round_num(node.get("cornerRadius")),
        "cornerSmoothing": round_num(node.get("cornerSmoothing")),
        "rectangleCornerRadii": node.get("rectangleCornerRadii"),
        "arcData": extract_arc_data(node.get("arcData")),

        "strokeWeight": round_num(node.get("strokeWeight")),
        "strokeAlign": node.get("strokeAlign"),
        "strokeCap": node.get("strokeCap"),
        "strokeJoin": node.get("strokeJoin"),
        "strokeDashes": [round_num(x) for x in node.get("strokeDashes", [])]
        if isinstance(node.get("strokeDashes"), list) else None,
        "dashPattern": node.get("dashPattern"),
        "complexStrokeProperties": node.get("complexStrokeProperties"),
        "backgroundColor": color_obj_to_hex(node.get("backgroundColor")),
        "background": [
            extract_paint(p, image_assets=image_assets) for p in node.get("background", [])
            if isinstance(p, dict)
        ] if isinstance(node.get("background"), list) else None,

        "fills": [
            extract_paint(p, image_assets=image_assets) for p in node.get("fills", [])
            if isinstance(p, dict) and p.get("visible", True)
        ] if isinstance(node.get("fills"), list) else None,

        "strokes": [
            extract_paint(p, image_assets=image_assets) for p in node.get("strokes", [])
            if isinstance(p, dict) and p.get("visible", True)
        ] if isinstance(node.get("strokes"), list) else None,

        "effects": [
            extract_effect(e) for e in node.get("effects", [])
            if isinstance(e, dict) and e.get("visible", True)
        ] if isinstance(node.get("effects"), list) else None,

        "text": extract_text(node),

        "componentId": node.get("componentId"),
        "componentProperties": node.get("componentProperties"),
        "variantProperties": node.get("variantProperties"),
        "styles": node.get("styles"),
        "exportSettings": node.get("exportSettings"),
        "interactions": node.get("interactions"),
        "reactions": node.get("reactions"),
        "prototypeDevice": node.get("prototypeDevice"),
        "transitionNodeID": node.get("transitionNodeID"),
        "transitionDuration": node.get("transitionDuration"),
        "transitionEasing": node.get("transitionEasing"),
        "assetExports": (node_assets or {}).get(node_id),

        "children": [
            extract_node(child, image_assets=image_assets, node_assets=node_assets)
            for child in node.get("children", [])
        ],
    }

    return clean_dict(data)


def collect_design_tokens(spec_root):
    colors = {}
    text_styles = {}
    spacing = set()
    radius = set()

    for node in walk_nodes(spec_root):
        for paint_key in ("fills", "strokes"):
            for paint in node.get(paint_key, []) or []:
                if paint.get("type") == "SOLID" and paint.get("color"):
                    color = paint["color"]
                    key = f"{color.get('hex')}@{color.get('alpha', 1)}"
                    colors.setdefault(key, {
                        "hex": color.get("hex"),
                        "alpha": color.get("alpha", 1),
                        "flutter": color.get("flutter"),
                        "count": 0,
                    })
                    colors[key]["count"] += 1

        text = node.get("text") or {}
        style = text.get("style") or {}
        if style:
            key = "|".join(str(style.get(k)) for k in (
                "fontFamily", "fontSize", "fontWeight", "lineHeightPx", "letterSpacing"
            ))
            text_styles.setdefault(key, {
                "fontFamily": style.get("fontFamily"),
                "fontSize": style.get("fontSize"),
                "fontWeight": style.get("fontWeight"),
                "lineHeightPx": style.get("lineHeightPx"),
                "letterSpacing": style.get("letterSpacing"),
                "textCase": style.get("textCase"),
                "textDecoration": style.get("textDecoration"),
                "examples": [],
                "count": 0,
            })
            text_styles[key]["count"] += 1
            chars = text.get("characters")
            if chars and len(text_styles[key]["examples"]) < 3:
                text_styles[key]["examples"].append(chars[:80])

        layout = node.get("layout") or {}
        for key in ("itemSpacing", "counterAxisSpacing", "paddingLeft", "paddingRight", "paddingTop", "paddingBottom"):
            value = layout.get(key)
            if isinstance(value, (int, float)):
                spacing.add(value)
        if isinstance(node.get("cornerRadius"), (int, float)):
            radius.add(node["cornerRadius"])

    return clean_dict({
        "colors": sorted(colors.values(), key=lambda item: item["count"], reverse=True),
        "textStyles": sorted(text_styles.values(), key=lambda item: item["count"], reverse=True),
        "spacing": sorted(spacing),
        "radius": sorted(radius),
    })


def collect_screen_summaries(spec_root):
    screens = []
    for node in walk_nodes(spec_root):
        if node.get("type") not in {"FRAME", "COMPONENT", "INSTANCE"}:
            continue
        size = node.get("size") or {}
        width = size.get("width")
        height = size.get("height")
        if not isinstance(width, (int, float)) or not isinstance(height, (int, float)):
            continue
        if width < 240 or height < 320:
            continue

        text_nodes = []
        asset_nodes = []
        for child in walk_nodes(node):
            text = child.get("text") or {}
            if text.get("characters") and len(text_nodes) < 12:
                text_nodes.append(text["characters"][:80])
            if child.get("assetExports") and len(asset_nodes) < 12:
                asset_nodes.append({
                    "name": child.get("name"),
                    "type": child.get("type"),
                    "assets": child.get("assetExports"),
                })

        screens.append(clean_dict({
            "id": node.get("id"),
            "name": node.get("name"),
            "type": node.get("type"),
            "size": size,
            "layout": node.get("layout"),
            "fills": node.get("fills"),
            "texts": text_nodes,
            "assets": asset_nodes,
        }))

    return screens


def relative_rect(node, screen):
    node_box = node.get("absoluteBoundingBox") or {}
    screen_box = screen.get("absoluteBoundingBox") or {}
    pos = node.get("position") or {"x": node_box.get("x"), "y": node_box.get("y")}
    size = node.get("size") or {"width": node_box.get("width"), "height": node_box.get("height")}
    origin = screen.get("position") or {"x": screen_box.get("x"), "y": screen_box.get("y")}
    x = pos.get("x")
    y = pos.get("y")
    if isinstance(x, (int, float)) and isinstance(origin.get("x"), (int, float)):
        x = round_num(x - origin["x"])
    if isinstance(y, (int, float)) and isinstance(origin.get("y"), (int, float)):
        y = round_num(y - origin["y"])
    return clean_dict({
        "x": x,
        "y": y,
        "width": size.get("width"),
        "height": size.get("height"),
    })


def text_node_entry(node, screen=None):
    text = node.get("text") or {}
    style = text.get("style") or {}
    entry = {
        "nodeId": node.get("id"),
        "name": node.get("name"),
        "characters": text.get("characters"),
        "rect": relative_rect(node, screen) if screen else {
            "x": (node.get("position") or {}).get("x"),
            "y": (node.get("position") or {}).get("y"),
            "width": (node.get("size") or {}).get("width"),
            "height": (node.get("size") or {}).get("height"),
        },
        "style": clean_dict({
            "fontFamily": style.get("fontFamily"),
            "fontSize": style.get("fontSize"),
            "fontWeight": style.get("fontWeight"),
            "lineHeightPx": style.get("lineHeightPx"),
            "letterSpacing": style.get("letterSpacing"),
            "textAlignHorizontal": style.get("textAlignHorizontal"),
            "textAlignVertical": style.get("textAlignVertical"),
        }),
        "fills": node.get("fills"),
    }
    return clean_dict(entry)


def collect_text_nodes(spec_root):
    items = []
    for node in walk_nodes(spec_root):
        if node.get("type") == "TEXT" and (node.get("text") or {}).get("characters"):
            items.append(text_node_entry(node))
    return items


def nearest_screen(node, screens):
    node_box = node.get("absoluteBoundingBox") or {}
    node_pos = node.get("position") or {"x": node_box.get("x"), "y": node_box.get("y")}
    x = node_pos.get("x")
    y = node_pos.get("y")
    if not isinstance(x, (int, float)) or not isinstance(y, (int, float)):
        return None
    for screen in screens:
        screen_box = screen.get("absoluteBoundingBox") or {}
        pos = screen.get("position") or {"x": screen_box.get("x"), "y": screen_box.get("y")}
        size = screen.get("size") or {"width": screen_box.get("width"), "height": screen_box.get("height")}
        sx, sy = pos.get("x"), pos.get("y")
        sw, sh = size.get("width"), size.get("height")
        if all(isinstance(v, (int, float)) for v in (sx, sy, sw, sh)):
            if sx <= x <= sx + sw and sy <= y <= sy + sh:
                return screen
    return None


def direct_child_entry(child, screen):
    text = child.get("text") or {}
    return clean_dict({
        "nodeId": child.get("id"),
        "name": child.get("name"),
        "type": child.get("type"),
        "rect": relative_rect(child, screen),
        "layout": child.get("layout"),
        "fills": child.get("fills"),
        "backgroundColor": child.get("backgroundColor"),
        "strokes": child.get("strokes"),
        "strokeWeight": child.get("strokeWeight"),
        "strokeAlign": child.get("strokeAlign"),
        "strokeDashes": child.get("strokeDashes"),
        "cornerRadius": child.get("cornerRadius"),
        "targetAspectRatio": child.get("targetAspectRatio"),
        "arcData": child.get("arcData"),
        "interactions": child.get("interactions"),
        "text": text.get("characters"),
        "assetExports": child.get("assetExports"),
    })


def collect_screen_blueprints(spec_root):
    screens = []
    for node in walk_nodes(spec_root):
        if node.get("type") not in {"FRAME", "COMPONENT", "INSTANCE"}:
            continue
        size = node.get("size") or {}
        width = size.get("width")
        height = size.get("height")
        if not isinstance(width, (int, float)) or not isinstance(height, (int, float)):
            continue
        if width < 240 or height < 320:
            continue

        texts = [
            text_node_entry(child, screen=node)
            for child in walk_nodes(node)
            if child.get("type") == "TEXT" and (child.get("text") or {}).get("characters")
        ]
        assets = [
            clean_dict({
                "nodeId": child.get("id"),
                "name": child.get("name"),
                "type": child.get("type"),
                "rect": relative_rect(child, node),
                "assetExports": child.get("assetExports"),
            })
            for child in walk_nodes(node)
            if child.get("assetExports")
        ]
        screens.append(clean_dict({
            "nodeId": node.get("id"),
            "name": node.get("name"),
            "type": node.get("type"),
            "size": size,
            "position": node.get("position"),
            "layout": node.get("layout"),
            "directChildren": [
                direct_child_entry(child, node)
                for child in node.get("children", []) or []
            ],
            "texts": texts,
            "assets": assets,
        }))
    return screens


def render_screen_blueprints_markdown(blueprints):
    lines = ["# Figma Screen Blueprints", ""]
    for screen in blueprints:
        size = screen.get("size") or {}
        lines.append(f"## {screen.get('name')} `{screen.get('nodeId')}`")
        lines.append(f"- Size: {size.get('width')} x {size.get('height')}")
        layout = screen.get("layout") or {}
        if layout:
            lines.append(f"- Layout: `{layout.get('layoutMode')}` gap `{layout.get('itemSpacing')}`")
            paddings = [layout.get(k) for k in ("paddingLeft", "paddingTop", "paddingRight", "paddingBottom")]
            if any(v is not None for v in paddings):
                lines.append(f"- Padding: L={paddings[0]} T={paddings[1]} R={paddings[2]} B={paddings[3]}")
        lines.append("")
        lines.append("### Direct Children")
        for child in screen.get("directChildren", [])[:24]:
            rect = child.get("rect") or {}
            lines.append(
                f"- `{child.get('name')}` `{child.get('type')}` "
                f"x={rect.get('x')} y={rect.get('y')} w={rect.get('width')} h={rect.get('height')}"
            )
        if screen.get("texts"):
            lines.append("")
            lines.append("### Text Nodes")
            for text in screen.get("texts", [])[:24]:
                rect = text.get("rect") or {}
                style = text.get("style") or {}
                chars = str(text.get("characters", "")).replace("\n", "\\n")
                lines.append(
                    f"- `{chars[:60]}` x={rect.get('x')} y={rect.get('y')} "
                    f"{style.get('fontFamily')} {style.get('fontSize')}px "
                    f"w{style.get('fontWeight')} line {style.get('lineHeightPx')}"
                )
        if screen.get("assets"):
            lines.append("")
            lines.append("### Asset Nodes")
            for asset in screen.get("assets", [])[:24]:
                rect = asset.get("rect") or {}
                lines.append(
                    f"- `{asset.get('name')}` `{asset.get('nodeId')}` "
                    f"x={rect.get('x')} y={rect.get('y')} w={rect.get('width')} h={rect.get('height')}"
                )
        lines.append("")
    return "\n".join(lines)


def render_ai_context(specs):
    document = specs["document"]
    source = specs.get("source", {})
    tokens = specs.get("designTokens", {})
    screens = specs.get("screens", [])

    lines = [
        "# Figma AI Context",
        "",
        "Use this file for quick orientation only. Before implementing, follow the no-missing-details protocol in `tools/figma-importer/UI_VIBE_GUIDE.md`.",
        "",
        "Read `figma_ai_build_packet.md` as the primary all-screen handoff. Implement from that packet plus `figma_screen_blueprints.md`, `figma_asset_manifest.md`, and especially `figma_specs.json`; for untouched Figma API data or suspected missing fields, inspect `figma_raw.json`.",
        "",
        "Detail-critical fields such as `strokeDashes`, `strokeAlign`, `strokeCap`, `strokeJoin`, exact fills/strokes, image fills, and nested node attributes must be verified in `figma_specs.json` or `figma_raw.json` before choosing fallbacks.",
        "",
        "## Source",
        "",
        f"- File: `{source.get('name', '-')}`",
        f"- File key: `{source.get('fileKey', '-')}`",
        f"- Node: `{source.get('nodeId', 'FULL FILE')}`",
        f"- Root: `{document.get('name')}` `({document.get('type')})`",
        "",
        "## Screens",
        "",
    ]

    for screen in screens[:30]:
        size = screen.get("size", {})
        lines.append(f"- `{screen.get('name')}`: {size.get('width')}x{size.get('height')} `{screen.get('type')}`")
        if screen.get("texts"):
            lines.append(f"  - Text: {', '.join('`' + item + '`' for item in screen['texts'][:6])}")
        if screen.get("assets"):
            asset_names = [asset.get("name", "-") for asset in screen["assets"][:6]]
            lines.append(f"  - Assets: {', '.join('`' + item + '`' for item in asset_names)}")

    lines.extend(["", "## Core Colors", ""])
    for color in (tokens.get("colors") or [])[:24]:
        lines.append(f"- `{color.get('hex')}` alpha `{color.get('alpha')}` count `{color.get('count')}`")

    lines.extend(["", "## Text Styles", ""])
    for style in (tokens.get("textStyles") or [])[:24]:
        examples = ", ".join(f"`{item}`" for item in style.get("examples", [])[:2])
        lines.append(
            f"- {style.get('fontFamily')} {style.get('fontSize')}px "
            f"w{style.get('fontWeight')} line {style.get('lineHeightPx')} "
            f"letter {style.get('letterSpacing')} count `{style.get('count')}` {examples}"
        )

    lines.extend(["", "## Spacing And Radius", ""])
    lines.append(f"- Spacing: `{tokens.get('spacing', [])}`")
    lines.append(f"- Radius: `{tokens.get('radius', [])}`")
    lines.append("")
    return "\n".join(lines)


def is_primary_screen_node(node):
    size = node.get("size") or {}
    width = size.get("width")
    height = size.get("height")
    if not isinstance(width, (int, float)) or not isinstance(height, (int, float)):
        return False
    return width >= 390 and height >= 800


def node_path(root, target_id):
    path = []

    def visit(node, current):
        next_path = current + [node.get("name") or node.get("id") or "-"]
        if node.get("id") == target_id:
            path.extend(next_path)
            return True
        for child in node.get("children", []) or []:
            if visit(child, next_path):
                return True
        return False

    visit(root, [])
    return " / ".join(path)


def paint_summary(paints):
    parts = []
    for paint in paints or []:
        paint_type = paint.get("type")
        if paint_type == "SOLID" and paint.get("color"):
            color = paint.get("color") or {}
            if color.get("hex") is None:
                color = color_obj_to_hex(color) or {}
            alpha = color.get("alpha")
            suffix = "" if alpha in (None, 1) else f"@{alpha}"
            parts.append(f"{color.get('hex')}{suffix}")
        elif paint_type == "IMAGE":
            asset = ((paint.get("image") or {}).get("asset") or {}).get("path")
            parts.append(f"IMAGE:{asset or (paint.get('image') or {}).get('imageRef')}")
        else:
            parts.append(str(paint_type))
    return ", ".join(part for part in parts if part)


def style_override_runs(text):
    chars = text.get("characters") or ""
    overrides = text.get("characterStyleOverrides") or []
    table = text.get("styleOverrideTable") or {}
    if not chars or not overrides or not table:
        return []
    runs = []
    run_start = 0
    limit = min(len(chars), len(overrides))
    for index in range(1, limit + 1):
        if index == limit or overrides[index] != overrides[run_start]:
            override_id = str(overrides[run_start])
            override = table.get(override_id) or {}
            run_text = chars[run_start:index]
            details = []
            fill = paint_summary(override.get("fills"))
            if fill:
                details.append(f"fill={fill}")
            for key in ("fontSize", "fontWeight", "textDecoration", "textCase"):
                if key in override:
                    details.append(f"{key}={round_num(override.get(key))}")
            if details and run_text:
                runs.append({
                    "text": run_text.replace("\n", "\\n"),
                    "range": f"{run_start}:{index}",
                    "details": " ".join(details),
                })
            run_start = index
    return runs


def risk_entry(node, risk, root, primary_screens):
    screen = nearest_screen(node, primary_screens)
    rect = relative_rect(node, screen) if screen else {
        "x": (node.get("position") or {}).get("x"),
        "y": (node.get("position") or {}).get("y"),
        "width": (node.get("size") or {}).get("width"),
        "height": (node.get("size") or {}).get("height"),
    }
    return clean_dict({
        "risk": risk,
        "id": node.get("id"),
        "name": node.get("name"),
        "type": node.get("type"),
        "screen": (screen or {}).get("name"),
        "screenId": (screen or {}).get("id"),
        "rect": rect,
        "path": node_path(root, node.get("id")),
    })


def collect_risk_entries(specs):
    root = specs.get("document") or {}
    primary_screens = [
        child for child in root.get("children", []) or []
        if is_primary_screen_node(child)
    ]
    risks = []
    for node in walk_nodes(root):
        fills = node.get("fills") or []
        strokes = node.get("strokes") or []
        text = node.get("text") or {}
        if node.get("strokeDashes"):
            entry = risk_entry(node, "dashed-stroke", root, primary_screens)
            entry["details"] = f"dashes={node.get('strokeDashes')} stroke={paint_summary(strokes)} weight={node.get('strokeWeight')}"
            risks.append(entry)
        override_runs = style_override_runs(text)
        if override_runs:
            entry = risk_entry(node, "text-style-overrides", root, primary_screens)
            entry["details"] = "; ".join(
                f"`{run['text']}` range={run['range']} {run['details']}"
                for run in override_runs[:6]
            )
            risks.append(entry)
        if node.get("type") == "LINE":
            entry = risk_entry(node, "divider-line", root, primary_screens)
            entry["details"] = f"stroke={paint_summary(strokes)} weight={node.get('strokeWeight')}"
            risks.append(entry)
        if node.get("interactions"):
            entry = risk_entry(node, "interaction", root, primary_screens)
            pieces = []
            for interaction in node.get("interactions", [])[:3]:
                trigger = (interaction.get("trigger") or {}).get("type")
                actions = interaction.get("actions") or []
                action_text = []
                for action in actions[:3]:
                    transition = action.get("transition") or {}
                    action_text.append(
                        f"{action.get('type')} nav={action.get('navigation')} "
                        f"dest={action.get('destinationId')} trans={transition.get('type')} "
                        f"duration={round_num(transition.get('duration'))}"
                    )
                pieces.append(f"{trigger}: {'; '.join(action_text)}")
            entry["details"] = " | ".join(pieces)
            risks.append(entry)
        if node.get("assetExports"):
            entry = risk_entry(node, "asset-export", root, primary_screens)
            paths = [asset.get("path") for asset in node.get("assetExports", []) if asset.get("path")]
            entry["details"] = ", ".join(paths[:4])
            risks.append(entry)
        if any(paint.get("type") == "IMAGE" for paint in fills + strokes):
            entry = risk_entry(node, "image-fill", root, primary_screens)
            entry["details"] = f"fills={paint_summary(fills)} strokes={paint_summary(strokes)}"
            risks.append(entry)
        if node.get("cornerSmoothing") not in (None, 0, 0.0):
            entry = risk_entry(node, "corner-smoothing", root, primary_screens)
            entry["details"] = f"radius={node.get('cornerRadius')} smoothing={node.get('cornerSmoothing')}"
            risks.append(entry)
        if node.get("rectangleCornerRadii"):
            entry = risk_entry(node, "per-corner-radius", root, primary_screens)
            entry["details"] = f"radii={node.get('rectangleCornerRadii')}"
            risks.append(entry)
        if node.get("targetAspectRatio"):
            entry = risk_entry(node, "target-aspect-ratio", root, primary_screens)
            entry["details"] = f"targetAspectRatio={node.get('targetAspectRatio')}"
            risks.append(entry)
        if node.get("arcData"):
            arc = node.get("arcData") or {}
            full_circle = (
                arc.get("startingAngle") in (None, 0, 0.0)
                and arc.get("innerRadius") in (None, 0, 0.0)
                and arc.get("endingAngle") in (6.28, 6.2831854820251465)
            )
            if not full_circle:
                entry = risk_entry(node, "arc-data", root, primary_screens)
                entry["details"] = f"arc={arc}"
                risks.append(entry)
    return risks


def summarize_direct_child(child):
    rect = child.get("rect") or {}
    bits = [
        f"`{child.get('name')}`",
        f"`{child.get('nodeId')}`",
        child.get("type") or "-",
        f"x={rect.get('x')} y={rect.get('y')} w={rect.get('width')} h={rect.get('height')}",
    ]
    if child.get("layout"):
        layout = child.get("layout") or {}
        bits.append(f"layout={layout.get('layoutMode')} gap={layout.get('itemSpacing')}")
    if child.get("fills"):
        bits.append(f"fill={paint_summary(child.get('fills'))}")
    if child.get("strokes"):
        stroke = f"stroke={paint_summary(child.get('strokes'))} weight={child.get('strokeWeight')}"
        if child.get("strokeDashes"):
            stroke += f" dashes={child.get('strokeDashes')}"
        bits.append(stroke)
    if child.get("cornerRadius") is not None:
        bits.append(f"radius={child.get('cornerRadius')}")
    if child.get("assetExports"):
        bits.append("assetExport=yes")
    if child.get("interactions"):
        bits.append("interaction=yes")
    return " | ".join(bits)


def render_ai_build_packet(specs):
    source = specs.get("source") or {}
    tokens = specs.get("designTokens") or {}
    primary_ids = {
        child.get("id")
        for child in (specs.get("document") or {}).get("children", []) or []
        if is_primary_screen_node(child)
    }
    blueprints = [
        screen for screen in specs.get("screenBlueprints", [])
        if screen.get("nodeId") in primary_ids
    ]
    risks = collect_risk_entries(specs)
    risks_by_screen = {}
    for risk in risks:
        risks_by_screen.setdefault(risk.get("screen") or "GLOBAL/UNKNOWN", []).append(risk)

    lines = [
        "# Figma AI Build Packet",
        "",
        "This is the primary handoff for building all extracted screens in one pass. It is generated from `figma_specs.json` and is designed to reduce token usage without allowing visual guessing.",
        "",
        "## Non-Negotiable Rules",
        "",
        "- Do not implement from screenshots, memory, or `figma_ai_context.md` alone.",
        "- Use this packet for global planning, then verify every component you code against `figma_specs.json` by `nodeId`.",
        "- If a detail is missing, conflicting, or surprising, query the same `id` in `figma_raw.json` before choosing a fallback.",
        "- Do not ignore any node listed in Global Risk Report or Per-Screen Risk Nodes.",
        "- Preserve children order. `LINE` nodes are real dividers unless proven otherwise.",
        "- Text overrides are part of the design. Use spans/annotated text when a range has a different fill/font.",
        "",
        "## Required Query Commands",
        "",
        "```bash",
        "jq '.. | objects | select(.id? == \"NODE_ID\")' figma_specs.json",
        "jq '.. | objects | select(.id? == \"NODE_ID\")' figma_raw.json",
        "```",
        "",
        "## Source",
        "",
        f"- File: `{source.get('name', '-')}`",
        f"- File key: `{source.get('fileKey', '-')}`",
        f"- Node: `{source.get('nodeId', 'FULL FILE')}`",
        f"- Primary screens: `{len(blueprints)}`",
        "",
        "## Core Tokens",
        "",
        "### Colors",
    ]
    for color in (tokens.get("colors") or [])[:18]:
        lines.append(f"- `{color.get('hex')}` alpha `{color.get('alpha')}` count `{color.get('count')}`")
    lines.extend(["", "### Text Styles"])
    for style in (tokens.get("textStyles") or [])[:14]:
        examples = ", ".join(f"`{item}`" for item in style.get("examples", [])[:2])
        lines.append(
            f"- {style.get('fontFamily')} {style.get('fontSize')}px "
            f"w{style.get('fontWeight')} line={style.get('lineHeightPx')} "
            f"letter={style.get('letterSpacing')} count={style.get('count')} {examples}"
        )
    lines.extend([
        "",
        f"- Spacing values: `{tokens.get('spacing', [])}`",
        f"- Radius values: `{tokens.get('radius', [])}`",
        "",
        "## All Primary Screens",
        "",
    ])
    for index, screen in enumerate(blueprints, start=1):
        size = screen.get("size") or {}
        layout = screen.get("layout") or {}
        text_preview = [str((text.get("characters") or "")).replace("\n", "\\n") for text in screen.get("texts", [])[:8]]
        lines.append(
            f"{index}. `{screen.get('name')}` `{screen.get('nodeId')}` "
            f"{size.get('width')}x{size.get('height')} layout={layout.get('layoutMode')} gap={layout.get('itemSpacing')}"
        )
        if text_preview:
            lines.append(f"   Text: {', '.join('`' + item[:40] + '`' for item in text_preview)}")
        if screen.get("assets"):
            asset_names = [f"`{asset.get('name')}` `{asset.get('nodeId')}`" for asset in screen.get("assets", [])[:6]]
            lines.append(f"   Assets: {', '.join(asset_names)}")

    lines.extend(["", "## Global Risk Report", ""])
    risk_groups = [
        ("dashed-stroke", "Dashed Strokes"),
        ("text-style-overrides", "Text Overrides"),
        ("divider-line", "Divider Lines"),
        ("interaction", "Interactions"),
        ("asset-export", "Asset Exports"),
        ("image-fill", "Image Fills"),
        ("target-aspect-ratio", "Target Aspect Ratios"),
        ("corner-smoothing", "Corner Smoothing"),
        ("per-corner-radius", "Per-Corner Radius"),
        ("arc-data", "Non-Full Arc Data"),
    ]
    for risk_key, title in risk_groups:
        items = [risk for risk in risks if risk.get("risk") == risk_key]
        if not items:
            continue
        lines.append(f"### {title} ({len(items)})")
        for risk in items[:40]:
            rect = risk.get("rect") or {}
            lines.append(
                f"- `{risk.get('id')}` `{risk.get('name')}` {risk.get('type')} "
                f"screen=`{risk.get('screen')}` rect=x{rect.get('x')} y{rect.get('y')} "
                f"w{rect.get('width')} h{rect.get('height')} :: {risk.get('details')}"
            )
        if len(items) > 40:
            lines.append(f"- ... {len(items) - 40} more. Query `figma_specs.json` by risk type before implementation.")
        lines.append("")

    lines.extend(["## Per-Screen Contracts", ""])
    for screen in blueprints:
        size = screen.get("size") or {}
        layout = screen.get("layout") or {}
        lines.append(f"### {screen.get('name')} `{screen.get('nodeId')}`")
        lines.append(
            f"- Size: {size.get('width')}x{size.get('height')} "
            f"layout={layout.get('layoutMode')} gap={layout.get('itemSpacing')} "
            f"padding=({layout.get('paddingLeft')},{layout.get('paddingTop')},{layout.get('paddingRight')},{layout.get('paddingBottom')})"
        )
        lines.append("- Direct children, in order:")
        for child in screen.get("directChildren", [])[:16]:
            lines.append(f"  - {summarize_direct_child(child)}")
        screen_risks = risks_by_screen.get(screen.get("name"), [])
        if screen_risks:
            lines.append("- Risk nodes in this screen, all must be handled:")
            for risk in screen_risks[:24]:
                rect = risk.get("rect") or {}
                lines.append(
                    f"  - {risk.get('risk')}: `{risk.get('id')}` `{risk.get('name')}` "
                    f"{risk.get('type')} rect=x{rect.get('x')} y{rect.get('y')} "
                    f"w{rect.get('width')} h{rect.get('height')} :: {risk.get('details')}"
                )
            if len(screen_risks) > 24:
                lines.append(f"  - ... {len(screen_risks) - 24} more risk nodes. Query screen subtree before coding.")
        lines.append("")

    lines.extend([
        "## Final Verification Checklist",
        "",
        "- Every primary screen above has a route/state or intentional implementation mapping.",
        "- Every dashed-stroke node is rendered dashed, not solid.",
        "- Every text-style-overrides node uses spans/rich text for overridden ranges.",
        "- Every divider-line node is either rendered or explicitly mapped to an equivalent border.",
        "- Every asset-export/image-fill node is visible on its intended background.",
        "- Button fills, strokes, radius, text size/weight, and disabled/selected states match `figma_specs.json`.",
        "- Icons use the node/vector size from `figma_specs.json`, not arbitrary library defaults.",
        "- Per-screen direct child order is preserved.",
        "- If implementation differs from this packet, document the node id and reason.",
        "",
    ])
    return "\n".join(lines)


def walk_nodes(node):
    yield node
    for child in node.get("children", []) or []:
        yield from walk_nodes(child)


def has_image_fill(node):
    for paint_key in ("fills", "strokes"):
        paints = node.get(paint_key)
        if not isinstance(paints, list):
            continue
        for paint in paints:
            if isinstance(paint, dict) and paint.get("type") == "IMAGE" and paint.get("imageRef"):
                return True
    return False


def collect_image_refs(root):
    refs = set()
    for node in walk_nodes(root):
        for paint_key in ("fills", "strokes"):
            paints = node.get(paint_key)
            if not isinstance(paints, list):
                continue
            for paint in paints:
                if isinstance(paint, dict) and paint.get("type") == "IMAGE" and paint.get("imageRef"):
                    refs.add(paint["imageRef"])
    return refs


def is_exportable_node(node):
    if node.get("visible") is False:
        return False
    if not node.get("id"):
        return False
    box = node.get("absoluteBoundingBox") or node.get("absoluteRenderBounds")
    return bool(box and box.get("width") and box.get("height"))


def is_vector_like_node(node):
    return node.get("type") in LEAF_ASSET_TYPES


def is_major_visual_node(node):
    return node.get("type") in {
        "FRAME",
        "COMPONENT",
        "COMPONENT_SET",
        "INSTANCE",
        "GROUP",
        "SECTION",
    }


def node_size(node):
    box = node.get("absoluteBoundingBox") or node.get("absoluteRenderBounds") or {}
    return float(box.get("width") or 0), float(box.get("height") or 0)


def is_screen_like_node(node):
    if node.get("type") not in {"FRAME", "SECTION"}:
        return False
    width, height = node_size(node)
    name = str(node.get("name", "")).lower()
    has_screen_name = any(word in name for word in ("screen", "dashboard", "vault -", "home dashboard"))
    return has_screen_name and width >= 320 and height >= 560


def node_name_matches_keywords(node, keywords):
    name = str(node.get("name", "")).lower()
    return any(keyword in name for keyword in keywords)


def has_descendant_asset_candidate(node, keywords):
    for child in node.get("children", []) or []:
        if not isinstance(child, dict):
            continue
        if node_name_matches_keywords(child, keywords):
            return True
        if has_image_fill(child) or child.get("exportSettings"):
            return True
        if has_descendant_asset_candidate(child, keywords):
            return True
    return False


def node_has_leaf_visual_descendants(node):
    children = node.get("children", []) or []
    if not children:
        return False
    for child in children:
        if not isinstance(child, dict):
            continue
        if child.get("type") in LEAF_ASSET_TYPES or has_image_fill(child):
            return True
        if node_has_leaf_visual_descendants(child):
            return True
    return False


def has_text_descendant(node):
    for child in node.get("children", []) or []:
        if not isinstance(child, dict):
            continue
        if child.get("type") == "TEXT":
            return True
        if has_text_descendant(child):
            return True
    return False


def is_semantic_asset_node(node, keywords):
    if is_screen_like_node(node):
        return False

    if has_image_fill(node) or node.get("exportSettings"):
        return True

    if not node_name_matches_keywords(node, keywords):
        return False

    node_type = node.get("type")
    if node_type in {"FRAME", "GROUP", "COMPONENT", "COMPONENT_SET", "INSTANCE"}:
        if has_text_descendant(node) and not has_image_fill(node) and not node.get("exportSettings"):
            return False
        return node_has_leaf_visual_descendants(node)

    return node_type in LEAF_ASSET_TYPES


def node_contains_id(node, target_id):
    if node.get("id") == target_id:
        return True
    return any(
        isinstance(child, dict) and node_contains_id(child, target_id)
        for child in node.get("children", []) or []
    )


def prune_descendant_export_nodes(nodes):
    pruned = []
    for node in nodes:
        node_id = node.get("id")
        if not node_id:
            continue
        if any(node_contains_id(parent, node_id) for parent in pruned):
            continue
        pruned = [
            existing for existing in pruned
            if not node_contains_id(node, existing.get("id"))
        ]
        pruned.append(node)
    return pruned


def collect_export_nodes(root, mode, semantic_keywords=SEMANTIC_ASSET_KEYWORDS, include_leaf_vectors=False):
    nodes = []
    seen = set()
    for node in walk_nodes(root):
        if not is_exportable_node(node):
            continue

        include = False
        if mode == "all":
            include = True
        elif mode == "major":
            include = is_major_visual_node(node) or is_vector_like_node(node) or has_image_fill(node)
        elif mode == "semantic":
            include = is_semantic_asset_node(node, semantic_keywords)
            if include_leaf_vectors and not include:
                include = is_vector_like_node(node)
        elif mode == "assets":
            include = is_vector_like_node(node) or has_image_fill(node) or bool(node.get("exportSettings"))

        node_id = node.get("id")
        if include and node_id not in seen:
            nodes.append(node)
            seen.add(node_id)

    if mode == "semantic":
        return prune_descendant_export_nodes(nodes)

    return nodes


def file_ext_from_response(response, fallback=".bin"):
    content_type = response.headers.get("Content-Type", "").split(";")[0].strip()
    ext = mimetypes.guess_extension(content_type)
    if ext == ".jpe":
        ext = ".jpg"
    return ext or fallback


def ensure_unique_path(path):
    if not path.exists():
        return path

    stem = path.stem
    suffix = path.suffix
    parent = path.parent
    index = 2
    while True:
        candidate = parent / f"{stem}_{index}{suffix}"
        if not candidate.exists():
            return candidate
        index += 1


def request_json(url, token, params=None):
    headers = {"X-Figma-Token": token}
    for attempt in range(MAX_REQUEST_RETRIES + 1):
        response = requests.get(url, headers=headers, params=params, timeout=90)
        if response.status_code != 429:
            response.raise_for_status()
            return response.json()

        retry_after = response.headers.get("Retry-After")
        if retry_after:
            try:
                retry_after_seconds = float(retry_after)
            except ValueError:
                retry_after_seconds = 10
        else:
            retry_after_seconds = 2 ** attempt

        plan_tier = response.headers.get("X-Figma-Plan-Tier", "unknown")
        rate_limit_type = response.headers.get("X-Figma-Rate-Limit-Type", "unknown")
        upgrade_link = response.headers.get("X-Figma-Upgrade-Link")
        if retry_after_seconds > MAX_AUTOMATED_RETRY_AFTER_SECONDS:
            details = [
                "Figma API bị rate limit quá lâu, dừng để tránh treo tool.",
                f"Retry-After: {retry_after_seconds:.0f}s",
                f"X-Figma-Plan-Tier: {plan_tier}",
                f"X-Figma-Rate-Limit-Type: {rate_limit_type}",
            ]
            if upgrade_link:
                details.append(f"X-Figma-Upgrade-Link: {upgrade_link}")
            details.append("Gợi ý: chạy lại với --no-assets hoặc --asset-mode major sau khi quota hồi.")
            raise FigmaRateLimitError("\n".join(details))

        delay = min(retry_after_seconds, MAX_RETRY_DELAY_SECONDS)

        if attempt >= MAX_REQUEST_RETRIES:
            response.raise_for_status()

        print(
            "Figma API rate limit 429 "
            f"(plan={plan_tier}, type={rate_limit_type}). "
            f"Chờ {delay:.0f}s rồi thử lại..."
        )
        time.sleep(delay)

    raise RuntimeError("Không thể gọi Figma API sau khi retry.")


def download_url(url, output_path):
    response = requests.get(url, timeout=120)
    response.raise_for_status()
    output_path = ensure_unique_path(output_path)
    output_path.write_bytes(response.content)
    return output_path, file_ext_from_response(response, output_path.suffix)


def chunked(items, size):
    for index in range(0, len(items), size):
        yield items[index:index + size]


def build_asset_name(node, fmt):
    node_id = str(node.get("id", "node")).replace(":", "-")
    node_name = slugify(node.get("name", "node"))[:70]
    return f"{node_name}_{node_id}.{fmt}"


def node_path_map(root):
    result = {}

    def visit(node, names):
        current = names + [str(node.get("name", "node"))]
        node_id = node.get("id")
        if node_id:
            result[node_id] = " / ".join(current)
        for child in node.get("children", []) or []:
            if isinstance(child, dict):
                visit(child, current)

    visit(root, [])
    return result


def png_dimensions(path):
    try:
        with Path(path).open("rb") as f:
            header = f.read(24)
        if header[:8] == b"\x89PNG\r\n\x1a\n":
            return {
                "width": int.from_bytes(header[16:20], "big"),
                "height": int.from_bytes(header[20:24], "big"),
            }
    except OSError:
        return None
    return None


def classify_asset_role(name, path_text):
    text = f"{name} {path_text}".lower()
    if "logo" in text:
        return "logo"
    if any(word in text for word in ("fingerprint", "biometric")):
        return "fingerprint_icon"
    if any(word in text for word in ("loading", "spinner")):
        return "loading"
    if any(word in text for word in ("notification", "bell")):
        return "notification"
    if "navigation frame" in text or "navigation" in text:
        return "nav_icon"
    if "right" in text or "left" in text:
        return "chevron"
    if any(word in text for word in ("storage", "location", "headset", "document", "category", "shield", "note")):
        return "feature_icon"
    if any(word in text for word in ("image", "illustration", "vault", "safe", "complete", "removebg")):
        return "illustration"
    return "asset"


def node_lookup(root):
    return {node.get("id"): node for node in walk_nodes(root) if node.get("id")}


def screen_candidates_from_spec(spec_root):
    return [
        node for node in walk_nodes(spec_root)
        if node.get("type") in {"FRAME", "COMPONENT", "INSTANCE"}
        and isinstance(((node.get("size") or node.get("absoluteBoundingBox") or {})).get("width"), (int, float))
        and isinstance(((node.get("size") or node.get("absoluteBoundingBox") or {})).get("height"), (int, float))
        and ((node.get("size") or node.get("absoluteBoundingBox") or {})).get("width") >= 240
        and ((node.get("size") or node.get("absoluteBoundingBox") or {})).get("height") >= 320
    ]


def render_asset_manifest(node_assets, root):
    paths = node_path_map(root)
    lookup = node_lookup(root)
    screens = screen_candidates_from_spec(root)
    items = []
    for node_id, exports in sorted(node_assets.items()):
        downloaded = [item for item in exports if item.get("downloaded")]
        first = downloaded[0] if downloaded else (exports[0] if exports else {})
        node = lookup.get(node_id, {})
        screen = nearest_screen(node, screens)
        node_path = paths.get(node_id)
        primary_path = first.get("path")
        items.append({
            "nodeId": node_id,
            "nodeName": node_path.split(" / ")[-1] if node_path else node_id,
            "nodeType": node.get("type"),
            "nodePath": node_path,
            "role": classify_asset_role(node.get("name", ""), node_path or ""),
            "screen": screen.get("name") if screen else None,
            "rectInScreen": relative_rect(node, screen) if screen else None,
            "nodeSize": node_size(node),
            "formats": sorted(set(item.get("format") for item in exports if item.get("format"))),
            "paths": [item.get("path") for item in downloaded if item.get("path")],
            "downloaded": bool(downloaded),
            "primaryPath": primary_path,
            "imageDimensions": png_dimensions(primary_path) if primary_path else None,
        })
    return items


def render_asset_manifest_markdown(items):
    lines = ["# Figma Asset Manifest", ""]
    if not items:
        lines.append("No node assets exported.")
        lines.append("")
        return "\n".join(lines)

    for item in items:
        dims = item.get("imageDimensions") or {}
        lines.append(
            f"- `{item.get('nodeName')}` `{item.get('nodeId')}` "
            f"role=`{item.get('role')}` screen=`{item.get('screen')}` "
            f"image={dims.get('width')}x{dims.get('height')}"
        )
        if item.get("rectInScreen"):
            rect = item["rectInScreen"]
            lines.append(
                f"  - Rect: x={rect.get('x')} y={rect.get('y')} "
                f"w={rect.get('width')} h={rect.get('height')}"
            )
        if item.get("nodePath"):
            lines.append(f"  - Path: `{item.get('nodePath')}`")
        if item.get("paths"):
            for path in item["paths"]:
                lines.append(f"  - File: `{path}`")
        else:
            lines.append("  - File: not downloaded")
    lines.append("")
    return "\n".join(lines)


def fetch_figma_file(file_key, node_id, token):
    if node_id:
        data = request_json(
            f"{FIGMA_API_BASE}/files/{file_key}/nodes",
            token,
            params={"ids": node_id},
        )

        if node_id not in data.get("nodes", {}):
            raise ValueError(f"Không tìm thấy node id: {node_id}")

        document = data["nodes"][node_id]["document"]
        return {
            "document": document,
            "components": data.get("components"),
            "componentSets": data.get("componentSets"),
            "styles": data.get("styles"),
            "name": data.get("name"),
            "lastModified": data.get("lastModified"),
            "thumbnailUrl": data.get("thumbnailUrl"),
            "version": data.get("version"),
            "role": data.get("role"),
            "editorType": data.get("editorType"),
            "linkAccess": data.get("linkAccess"),
        }

    return request_json(f"{FIGMA_API_BASE}/files/{file_key}", token)


def fetch_file_images(file_key, token):
    data = request_json(f"{FIGMA_API_BASE}/files/{file_key}/images", token)
    return data.get("meta", {}).get("images", {})


def fetch_node_render_urls(file_key, token, node_ids, fmt, scale, request_delay):
    result = {}
    batches = list(chunked(node_ids, EXPORT_BATCH_SIZE))
    for batch_index, batch in enumerate(batches):
        params = {
            "ids": ",".join(batch),
            "format": fmt,
        }
        if fmt != "svg":
            params["scale"] = scale
        data = request_json(f"{FIGMA_API_BASE}/images/{file_key}", token, params=params)
        result.update(data.get("images", {}))
        if request_delay and batch_index < len(batches) - 1:
            time.sleep(request_delay)
    return result


def download_image_fills(file_key, token, root, assets_dir):
    refs = collect_image_refs(root)
    if not refs:
        return {}

    images = fetch_file_images(file_key, token)
    image_assets = {}
    image_dir = assets_dir / "image_fills"
    image_dir.mkdir(parents=True, exist_ok=True)

    for image_ref in sorted(refs):
        url = images.get(image_ref)
        if not url:
            image_assets[image_ref] = {"imageRef": image_ref, "downloaded": False}
            continue

        base_path = image_dir / f"{slugify(image_ref)[:80]}.bin"
        try:
            written_path, ext = download_url(url, base_path)
            final_path = written_path
            if written_path.suffix == ".bin" and ext != ".bin":
                renamed = ensure_unique_path(written_path.with_suffix(ext))
                written_path.rename(renamed)
                final_path = renamed
            image_assets[image_ref] = {
                "imageRef": image_ref,
                "path": str(final_path),
                "url": url,
                "downloaded": True,
            }
        except requests.RequestException as exc:
            image_assets[image_ref] = {
                "imageRef": image_ref,
                "url": url,
                "downloaded": False,
                "error": str(exc),
            }

    return image_assets


def export_node_assets(
    file_key,
    token,
    root,
    assets_dir,
    formats,
    scale,
    mode,
    request_delay,
    semantic_keywords,
    include_leaf_vectors,
):
    export_nodes = collect_export_nodes(
        root,
        mode,
        semantic_keywords=semantic_keywords,
        include_leaf_vectors=include_leaf_vectors,
    )
    node_assets = {}
    if not export_nodes:
        return node_assets

    node_by_id = {node["id"]: node for node in export_nodes}
    render_dir = assets_dir / "node_exports"
    render_dir.mkdir(parents=True, exist_ok=True)

    for fmt in formats:
        ids_for_format = list(node_by_id)
        if fmt == "svg":
            ids_for_format = [
                node_id for node_id, node in node_by_id.items()
                if is_vector_like_node(node) or node.get("exportSettings")
            ]
        if not ids_for_format:
            continue

        estimated_batches = math.ceil(len(ids_for_format) / EXPORT_BATCH_SIZE)
        print(f"  - {fmt}: {len(ids_for_format)} node(s), khoảng {estimated_batches} request render")
        urls = fetch_node_render_urls(file_key, token, ids_for_format, fmt, scale, request_delay)
        format_dir = render_dir / fmt
        format_dir.mkdir(parents=True, exist_ok=True)

        for node_id, url in urls.items():
            node = node_by_id.get(node_id)
            if not node:
                continue
            asset = {
                "format": fmt,
                "url": url,
                "downloaded": False,
            }
            if url:
                output_path = format_dir / build_asset_name(node, fmt)
                try:
                    written_path, _ = download_url(url, output_path)
                    asset["path"] = str(written_path)
                    asset["downloaded"] = True
                except requests.RequestException as exc:
                    asset["error"] = str(exc)
            else:
                asset["error"] = "Figma did not return a render URL for this node."

            node_assets.setdefault(node_id, []).append(asset)

    return node_assets


def estimate_render_requests(root, mode, formats, semantic_keywords, include_leaf_vectors):
    export_nodes = collect_export_nodes(
        root,
        mode,
        semantic_keywords=semantic_keywords,
        include_leaf_vectors=include_leaf_vectors,
    )
    estimates = []
    for fmt in formats:
        nodes_for_format = export_nodes
        if fmt == "svg":
            nodes_for_format = [
                node for node in export_nodes
                if is_vector_like_node(node) or node.get("exportSettings")
            ]
        estimates.append({
            "format": fmt,
            "nodeCount": len(nodes_for_format),
            "estimatedRequests": math.ceil(len(nodes_for_format) / EXPORT_BATCH_SIZE) if nodes_for_format else 0,
            "batchSize": EXPORT_BATCH_SIZE,
        })
    return clean_dict({
        "mode": mode,
        "totalNodeCount": len(export_nodes),
        "formats": estimates,
        "sampleNodes": [
            {"nodeId": node.get("id"), "name": node.get("name"), "type": node.get("type")}
            for node in export_nodes[:20]
        ],
    })


def node_to_markdown(node, depth=0):
    indent = "  " * depth
    lines = []

    title = f"{indent}- **{node.get('name')}** `({node.get('type')})`"
    lines.append(title)

    size = node.get("size", {})
    if size:
        lines.append(
            f"{indent}  - Size: {size.get('width')} × {size.get('height')}"
        )

    position = node.get("position", {})
    if position:
        lines.append(
            f"{indent}  - Position: x={position.get('x')}, y={position.get('y')}"
        )

    layout = node.get("layout", {})
    if layout:
        lines.append(f"{indent}  - Layout: `{layout.get('layoutMode')}`")
        if any(k in layout for k in ["paddingLeft", "paddingTop", "paddingRight", "paddingBottom"]):
            lines.append(
                f"{indent}  - Padding: "
                f"L={layout.get('paddingLeft')}, "
                f"T={layout.get('paddingTop')}, "
                f"R={layout.get('paddingRight')}, "
                f"B={layout.get('paddingBottom')}"
            )
        if "itemSpacing" in layout:
            lines.append(f"{indent}  - Gap: {layout.get('itemSpacing')}")
        if "primaryAxisAlignItems" in layout or "counterAxisAlignItems" in layout:
            lines.append(
                f"{indent}  - Align: primary={layout.get('primaryAxisAlignItems')}, "
                f"counter={layout.get('counterAxisAlignItems')}"
            )

    if "cornerRadius" in node:
        lines.append(f"{indent}  - Radius: {node.get('cornerRadius')}")
    if node.get("cornerSmoothing") not in (None, 0, 0.0):
        lines.append(f"{indent}  - Corner smoothing: {node.get('cornerSmoothing')}")

    fills = node.get("fills", [])
    if fills:
        fill_text = []
        for fill in fills:
            if fill.get("type") == "SOLID" and fill.get("color"):
                fill_text.append(f"{fill['color'].get('hex')} / {fill['color'].get('flutter')}")
            elif fill.get("type") == "IMAGE":
                asset = (fill.get("image") or {}).get("asset") or {}
                fill_text.append(f"IMAGE {asset.get('path', '')}".strip())
            else:
                fill_text.append(str(fill.get("type")))
        lines.append(f"{indent}  - Fill: {', '.join(fill_text)}")

    background_color = node.get("backgroundColor") or {}
    if background_color and background_color.get("alpha") not in (None, 0):
        fill_colors = [
            (fill.get("color") or {}).get("hex")
            for fill in fills
            if fill.get("type") == "SOLID" and fill.get("color")
        ]
        if not fills or background_color.get("hex") not in fill_colors:
            lines.append(
                f"{indent}  - Background color: "
                f"{background_color.get('hex')} / {background_color.get('flutter')}"
            )

    strokes = node.get("strokes", [])
    if strokes:
        stroke_text = []
        for stroke in strokes:
            if stroke.get("type") == "SOLID" and stroke.get("color"):
                stroke_text.append(f"{stroke['color'].get('hex')} / {stroke['color'].get('flutter')}")
            else:
                stroke_text.append(str(stroke.get("type")))
        stroke_details = [f"weight={node.get('strokeWeight')}"]
        if node.get("strokeAlign"):
            stroke_details.append(f"align={node.get('strokeAlign')}")
        if node.get("strokeDashes"):
            stroke_details.append(f"dashes={node.get('strokeDashes')}")
        if node.get("strokeCap"):
            stroke_details.append(f"cap={node.get('strokeCap')}")
        if node.get("strokeJoin"):
            stroke_details.append(f"join={node.get('strokeJoin')}")
        lines.append(f"{indent}  - Stroke: {', '.join(stroke_text)} {' '.join(stroke_details)}")

    arc_data = node.get("arcData")
    if arc_data and (
        arc_data.get("startingAngle") not in (None, 0, 0.0)
        or arc_data.get("endingAngle") not in (None, 6.28)
        or arc_data.get("innerRadius") not in (None, 0, 0.0)
    ):
        lines.append(
            f"{indent}  - Arc: start={arc_data.get('startingAngle')} "
            f"end={arc_data.get('endingAngle')} inner={arc_data.get('innerRadius')}"
        )

    effects = node.get("effects", [])
    if effects:
        for effect in effects:
            lines.append(
                f"{indent}  - Effect: {effect.get('type')} "
                f"offset={effect.get('offset')} radius={effect.get('radius')} "
                f"spread={effect.get('spread')} color={effect.get('color')}"
            )

    text = node.get("text")
    if text:
        style = text.get("style", {})
        chars = text.get("characters", "")
        if len(chars) > 50:
            chars = chars[:50] + "..."
        lines.append(f"{indent}  - Text: `{chars}`")
        lines.append(
            f"{indent}  - Font: {style.get('fontFamily')} "
            f"{style.get('fontSize')}px "
            f"weight={style.get('fontWeight')} "
            f"lineHeight={style.get('lineHeightPx')}"
        )
        overrides = text.get("characterStyleOverrides") or []
        override_table = text.get("styleOverrideTable") or {}
        original_chars = text.get("characters", "")
        if overrides and override_table and original_chars:
            runs = []
            run_start = 0
            for index in range(1, min(len(overrides), len(original_chars)) + 1):
                if index == len(overrides) or overrides[index] != overrides[run_start]:
                    override_id = str(overrides[run_start])
                    override = override_table.get(override_id) or {}
                    run_text = original_chars[run_start:index]
                    details = []
                    fills = override.get("fills") or []
                    if fills:
                        fill_details = []
                        for fill in fills:
                            if fill.get("type") == "SOLID" and fill.get("color"):
                                color = color_obj_to_hex(fill.get("color"))
                                fill_details.append((color or {}).get("hex"))
                            else:
                                fill_details.append(str(fill.get("type")))
                        details.append(f"fill={','.join(x for x in fill_details if x)}")
                    for key in ("fontSize", "fontWeight", "textDecoration", "textCase"):
                        if key in override:
                            details.append(f"{key}={round_num(override.get(key))}")
                    if details and run_text:
                        safe_text = run_text.replace("\n", "\\n")
                        runs.append(f"`{safe_text}` range={run_start}:{index} {' '.join(details)}")
                    run_start = index
            if runs:
                lines.append(f"{indent}  - Text overrides: {'; '.join(runs[:8])}")

    asset_exports = node.get("assetExports", [])
    if asset_exports:
        paths = [asset.get("path") for asset in asset_exports if asset.get("path")]
        if paths:
            lines.append(f"{indent}  - Asset exports: {', '.join(paths)}")

    interactions = node.get("interactions", [])
    if interactions:
        summaries = []
        for interaction in interactions[:4]:
            trigger = (interaction.get("trigger") or {}).get("type")
            actions = interaction.get("actions") or []
            action_bits = []
            for action in actions[:3]:
                transition = action.get("transition") or {}
                action_bits.append(
                    f"{action.get('type')} navigation={action.get('navigation')} "
                    f"destination={action.get('destinationId')} "
                    f"transition={transition.get('type')} duration={round_num(transition.get('duration'))}"
                )
            summaries.append(f"{trigger}: {'; '.join(action_bits)}")
        lines.append(f"{indent}  - Interactions: {' | '.join(summaries)}")

    for child in node.get("children", []):
        lines.extend(node_to_markdown(child, depth + 1))

    return lines


def parse_args():
    parser = argparse.ArgumentParser(
        description="Extract Figma specs, metadata, image fills, and rendered node assets."
    )
    parser.add_argument("figma_url", help="Figma file/frame URL")
    parser.add_argument("--out-dir", default=".", help="Output directory")
    parser.add_argument("--assets-dir", default="figma_assets", help="Asset directory inside --out-dir")
    parser.add_argument("--no-assets", action="store_true", help="Skip image/node asset downloads")
    parser.add_argument(
        "--asset-mode",
        choices=["semantic", "assets", "major", "all"],
        default="semantic",
        help=(
            "semantic=named/grouped assets, assets=legacy leaf icons/images/export settings, "
            "major=frames/groups too, all=every visible node"
        ),
    )
    parser.add_argument(
        "--formats",
        default="png,svg",
        help="Comma-separated node render formats. SVG is limited to vector/export-setting nodes.",
    )
    parser.add_argument("--scale", type=float, default=2, help="PNG/JPG render scale")
    parser.add_argument(
        "--request-delay",
        type=float,
        default=6,
        help="Seconds to wait between Figma image render batches.",
    )
    parser.add_argument(
        "--semantic-keywords",
        default=",".join(SEMANTIC_ASSET_KEYWORDS),
        help="Comma-separated lowercase name keywords for --asset-mode semantic.",
    )
    parser.add_argument(
        "--include-leaf-vectors",
        action="store_true",
        help="With --asset-mode semantic, also export unmatched vector leaf nodes.",
    )
    parser.add_argument("--skip-raw", action="store_true", help="Do not write figma_raw.json")
    return parser.parse_args()


def normalize_formats(formats_text):
    formats = [item.strip().lower() for item in formats_text.split(",") if item.strip()]
    return [fmt for fmt in formats if fmt in {"png", "jpg", "svg", "pdf"}]


def main():
    args = parse_args()

    token = os.getenv("FIGMA_TOKEN")
    if not token:
        print("Lỗi: chưa có FIGMA_TOKEN.")
        print("Chạy ví dụ:")
        print("export FIGMA_TOKEN='figd_xxx'")
        sys.exit(1)

    figma_url = args.figma_url
    file_key, node_id = parse_figma_url(figma_url)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    assets_dir = out_dir / args.assets_dir

    print(f"File key: {file_key}")
    print(f"Node ID: {node_id or 'FULL FILE'}")
    print("Đang gọi Figma API...")

    raw_file = fetch_figma_file(file_key, node_id, token)
    raw_node = raw_file["document"]

    if not args.skip_raw:
        with (out_dir / "figma_raw.json").open("w", encoding="utf-8") as f:
            json.dump(raw_file, f, ensure_ascii=False, indent=2)

    image_assets = {}
    node_assets = {}
    semantic_keywords = tuple()
    render_estimate = {}
    if not args.no_assets:
        formats = normalize_formats(args.formats)
        assets_dir.mkdir(parents=True, exist_ok=True)

        print("Đang tải image fills...")
        image_assets = download_image_fills(file_key, token, raw_node, assets_dir)

        semantic_keywords = tuple(
            item.strip().lower()
            for item in args.semantic_keywords.split(",")
            if item.strip()
        )
        render_estimate = estimate_render_requests(
            raw_node,
            args.asset_mode,
            formats,
            semantic_keywords,
            args.include_leaf_vectors,
        )
        print("Ước lượng quota render:")
        for item in render_estimate.get("formats", []):
            print(
                f"  - {item.get('format')}: {item.get('nodeCount')} node(s), "
                f"khoảng {item.get('estimatedRequests')} request"
            )
        print(f"Đang render/export node assets ({args.asset_mode}, formats={','.join(formats)})...")
        node_assets = export_node_assets(
            file_key=file_key,
            token=token,
            root=raw_node,
            assets_dir=assets_dir,
            formats=formats,
            scale=args.scale,
            mode=args.asset_mode,
            request_delay=args.request_delay,
            semantic_keywords=semantic_keywords,
            include_leaf_vectors=args.include_leaf_vectors,
        )

    extracted_document = extract_node(raw_node, image_assets=image_assets, node_assets=node_assets)
    specs = {
        "source": clean_dict({
            "fileKey": file_key,
            "nodeId": node_id,
            "url": figma_url,
            "name": raw_file.get("name"),
            "lastModified": raw_file.get("lastModified"),
            "thumbnailUrl": raw_file.get("thumbnailUrl"),
            "version": raw_file.get("version"),
            "role": raw_file.get("role"),
            "editorType": raw_file.get("editorType"),
            "linkAccess": raw_file.get("linkAccess"),
        }),
        "libraries": clean_dict({
            "components": raw_file.get("components"),
            "componentSets": raw_file.get("componentSets"),
            "styles": raw_file.get("styles"),
        }),
        "assets": clean_dict({
            "mode": None if args.no_assets else args.asset_mode,
            "semanticKeywords": None if args.no_assets else list(semantic_keywords if not args.no_assets else []),
            "renderEstimate": render_estimate,
            "imageFills": image_assets,
            "nodeExports": node_assets,
        }),
        "document": extracted_document,
    }
    specs["designTokens"] = collect_design_tokens(extracted_document)
    specs["screens"] = collect_screen_summaries(extracted_document)
    specs["screenBlueprints"] = collect_screen_blueprints(extracted_document)
    specs["textNodes"] = collect_text_nodes(extracted_document)
    specs = clean_dict(specs)

    with (out_dir / "figma_specs.json").open("w", encoding="utf-8") as f:
        json.dump(specs, f, ensure_ascii=False, indent=2)

    with (out_dir / "figma_ai_context.json").open("w", encoding="utf-8") as f:
        json.dump({
            "source": specs.get("source"),
            "designTokens": specs.get("designTokens"),
            "screens": specs.get("screens"),
        }, f, ensure_ascii=False, indent=2)

    with (out_dir / "figma_screen_blueprints.json").open("w", encoding="utf-8") as f:
        json.dump(specs.get("screenBlueprints", []), f, ensure_ascii=False, indent=2)

    with (out_dir / "figma_text_nodes.json").open("w", encoding="utf-8") as f:
        json.dump(specs.get("textNodes", []), f, ensure_ascii=False, indent=2)

    asset_manifest = render_asset_manifest(node_assets, raw_node)
    with (out_dir / "figma_asset_manifest.json").open("w", encoding="utf-8") as f:
        json.dump(asset_manifest, f, ensure_ascii=False, indent=2)

    md_lines = ["# Figma Specs", ""]
    md_lines.append(f"- File key: `{file_key}`")
    md_lines.append(f"- Node ID: `{node_id or 'FULL FILE'}`")
    if image_assets:
        downloaded = sum(1 for item in image_assets.values() if item.get("downloaded"))
        md_lines.append(f"- Image fills downloaded: {downloaded}/{len(image_assets)}")
    if node_assets:
        downloaded = sum(1 for items in node_assets.values() for item in items if item.get("downloaded"))
        md_lines.append(f"- Node exports downloaded: {downloaded}")
    md_lines.append("")
    md_lines.extend(node_to_markdown(specs["document"]))

    with (out_dir / "figma_specs.md").open("w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))

    with (out_dir / "figma_ai_context.md").open("w", encoding="utf-8") as f:
        f.write(render_ai_context(specs))

    with (out_dir / "figma_ai_build_packet.md").open("w", encoding="utf-8") as f:
        f.write(render_ai_build_packet(specs))

    with (out_dir / "figma_asset_manifest.md").open("w", encoding="utf-8") as f:
        f.write(render_asset_manifest_markdown(asset_manifest))

    with (out_dir / "figma_screen_blueprints.md").open("w", encoding="utf-8") as f:
        f.write(render_screen_blueprints_markdown(specs.get("screenBlueprints", [])))

    print("Xong.")
    print("Đã tạo:")
    if not args.skip_raw:
        print(f"- {out_dir / 'figma_raw.json'}")
    print(f"- {out_dir / 'figma_specs.json'}")
    print(f"- {out_dir / 'figma_specs.md'}")
    print(f"- {out_dir / 'figma_ai_context.json'}")
    print(f"- {out_dir / 'figma_ai_context.md'}")
    print(f"- {out_dir / 'figma_ai_build_packet.md'}")
    print(f"- {out_dir / 'figma_asset_manifest.json'}")
    print(f"- {out_dir / 'figma_asset_manifest.md'}")
    print(f"- {out_dir / 'figma_screen_blueprints.json'}")
    print(f"- {out_dir / 'figma_screen_blueprints.md'}")
    print(f"- {out_dir / 'figma_text_nodes.json'}")
    if not args.no_assets:
        print(f"- {assets_dir}/")


if __name__ == "__main__":
    main()
