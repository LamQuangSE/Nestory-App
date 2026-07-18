#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import shutil
from collections import Counter
from pathlib import Path
from typing import Any


RISKY_SVG_MARKERS = (
    "filter",
    "mask",
    "clipPath",
    "foreignObject",
    "radialGradient",
    "pattern",
    "feGaussianBlur",
)


MATERIAL_TYPE_BUCKETS = [
    ("displayLarge", 57),
    ("displayMedium", 45),
    ("displaySmall", 36),
    ("headlineLarge", 32),
    ("headlineMedium", 28),
    ("headlineSmall", 24),
    ("titleLarge", 22),
    ("titleMedium", 16),
    ("titleSmall", 14),
    ("bodyLarge", 16),
    ("bodyMedium", 14),
    ("bodySmall", 12),
    ("labelLarge", 14),
    ("labelMedium", 12),
    ("labelSmall", 11),
]


def load_config(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def normalize_specs_root(payload: dict[str, Any]) -> dict[str, Any]:
    if isinstance(payload.get("document"), dict):
        return payload["document"]
    return payload


def walk(node: dict[str, Any]):
    yield node
    for child in node.get("children", []) or []:
        if isinstance(child, dict):
            yield from walk(child)


def snake_case(value: str) -> str:
    value = re.sub(r"[^a-zA-Z0-9]+", "_", value.strip()).strip("_").lower()
    value = re.sub(r"_+", "_", value)
    if not value:
        value = "asset"
    if value[0].isdigit():
        value = f"r_{value}"
    return value


def pascal_case(value: str) -> str:
    return "".join(part.capitalize() for part in snake_case(value).split("_") if part) or "Asset"


def classify_name(name: str, node_type: str, path: Path | None = None) -> str:
    lower = name.lower()
    suffix = path.suffix.lower() if path else ""
    if any(word in lower for word in ("bg", "background", "header")):
        return "background"
    if node_type in {"VECTOR", "BOOLEAN_OPERATION", "LINE", "ELLIPSE", "POLYGON", "STAR"}:
        return "icon"
    if suffix == ".svg" and any(word in lower for word in ("icon", "ic_", "nav", "tab")):
        return "icon"
    return "image"


def prefixed_resource_name(name: str, kind: str, config: dict[str, Any]) -> str:
    base = snake_case(name)
    prefix = {
        "icon": config["icon_prefix"],
        "background": config["background_prefix"],
        "image": config["image_prefix"],
    }[kind]
    if not base.startswith(prefix):
        base = f"{prefix}{base}"
    return base


def unique_name(name: str, used: Counter[str]) -> str:
    used[name] += 1
    if used[name] == 1:
        return name
    return f"{name}_{used[name]}"


def svg_has_risky_features(path: Path) -> bool:
    text = path.read_text(encoding="utf-8", errors="ignore")
    return any(marker in text for marker in RISKY_SVG_MARKERS)


def parse_color_hex(value: str) -> str | None:
    if not value:
        return None
    value = value.strip().upper()
    if re.fullmatch(r"#[0-9A-F]{6}", value):
        return value
    return None


def color_to_android(hex_color: str, alpha: float | None) -> str:
    hex_body = hex_color.replace("#", "").upper()
    if alpha is None or alpha >= 1:
        return f"#{hex_body}"
    alpha_hex = f"{max(0, min(255, round(alpha * 255))):02X}"
    return f"#{alpha_hex}{hex_body}"


def collect_colors(root: dict[str, Any]) -> Counter[tuple[str, float]]:
    colors: Counter[tuple[str, float]] = Counter()
    for node in walk(root):
        for paint_key in ("fills", "strokes"):
            paints = node.get(paint_key)
            if not isinstance(paints, list):
                continue
            for paint in paints:
                color = (paint.get("color") or {}) if isinstance(paint, dict) else {}
                hex_color = parse_color_hex(str(color.get("hex", "")))
                if hex_color:
                    colors[(hex_color, float(color.get("alpha", 1)))] += 1
    return colors


def collect_text_styles(root: dict[str, Any]) -> dict[str, dict[str, Any]]:
    selected: dict[str, dict[str, Any]] = {}
    for node in walk(root):
        if node.get("type") != "TEXT":
            continue
        style = ((node.get("text") or {}).get("style") or {})
        size = style.get("fontSize")
        if not isinstance(size, (int, float)):
            continue
        bucket = nearest_type_bucket(size)
        if bucket not in selected:
            selected[bucket] = style
    return selected


def nearest_type_bucket(size: float) -> str:
    return min(MATERIAL_TYPE_BUCKETS, key=lambda item: abs(item[1] - size))[0]


def collect_spacing_and_radius(root: dict[str, Any]) -> tuple[list[float], list[float]]:
    spacing = set()
    radius = set()
    for node in walk(root):
        layout = node.get("layout") or {}
        for key in ("itemSpacing", "paddingLeft", "paddingRight", "paddingTop", "paddingBottom"):
            value = layout.get(key)
            if isinstance(value, (int, float)) and value >= 0:
                spacing.add(float(value))
        corner = node.get("cornerRadius")
        if isinstance(corner, (int, float)) and corner >= 0:
            radius.add(float(corner))
    return sorted(spacing), sorted(radius)


def collect_asset_exports(root: dict[str, Any]) -> list[dict[str, Any]]:
    assets = []
    for node in walk(root):
        for asset in node.get("assetExports", []) or []:
            path = asset.get("path")
            if path:
                assets.append({
                    "node_id": node.get("id"),
                    "node_name": node.get("name", "asset"),
                    "node_type": node.get("type", "UNKNOWN"),
                    "format": asset.get("format"),
                    "path": Path(path),
                })
    return assets


def choose_android_assets(assets: list[dict[str, Any]]) -> list[dict[str, Any]]:
    grouped: dict[str, list[dict[str, Any]]] = {}
    for item in assets:
        key = item.get("node_id") or str(item.get("path"))
        grouped.setdefault(key, []).append(item)

    selected = []
    for items in grouped.values():
        by_suffix = {item["path"].suffix.lower(): item for item in items}
        for suffix in (".xml", ".webp", ".png", ".jpg", ".jpeg"):
            if suffix in by_suffix:
                selected.append(by_suffix[suffix])
                break
        else:
            selected.append(items[0])
    return selected


def write_text_if_changed(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists() and path.read_text(encoding="utf-8") == content:
        return
    path.write_text(content, encoding="utf-8")


def copy_assets(assets: list[dict[str, Any]], drawable_dir: Path, config: dict[str, Any]) -> tuple[list[str], list[str], list[str]]:
    drawable_dir.mkdir(parents=True, exist_ok=True)
    icons = []
    images = []
    warnings = []
    used: Counter[str] = Counter()

    for item in choose_android_assets(assets):
        source = item["path"]
        if not source.exists():
            warnings.append(f"Missing asset: {source}")
            continue

        kind = classify_name(item["node_name"], item["node_type"], source)
        resource = unique_name(prefixed_resource_name(item["node_name"], kind, config), used)
        suffix = source.suffix.lower()

        if suffix == ".svg":
            risk = "risky" if svg_has_risky_features(source) else "simple"
            warnings.append(
                f"Skipped {risk} SVG because Android drawable cannot consume raw SVG: {source}. "
                "Use the PNG/WebP export or convert to VectorDrawable XML."
            )
            continue
        elif suffix in {".png", ".jpg", ".jpeg", ".webp", ".xml"}:
            target = drawable_dir / f"{resource}{'.jpg' if suffix == '.jpeg' else suffix}"
        else:
            warnings.append(f"Unsupported asset type skipped: {source}")
            continue

        shutil.copy2(source, target)
        if kind == "icon":
            icons.append(resource)
        else:
            images.append(resource)

    return sorted(set(icons)), sorted(set(images)), warnings


def render_figma_colors_xml(colors: Counter[tuple[str, float]]) -> str:
    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']
    used: Counter[str] = Counter()
    for (hex_color, alpha), _count in colors.most_common():
        base = snake_case(f"figma_{hex_color.replace('#', '')}")
        if alpha != 1:
            base = f"{base}_a{round(alpha * 100)}"
        name = unique_name(base, used)
        lines.append(f'    <color name="{name}">{color_to_android(hex_color, alpha)}</color>')
    lines.append("</resources>")
    lines.append("")
    return "\n".join(lines)


def render_generated_color_kt(colors: Counter[tuple[str, float]], package_name: str) -> str:
    lines = [
        f"package {package_name}",
        "",
        "import androidx.compose.ui.graphics.Color",
        "",
        "object GeneratedColor {",
    ]
    used: Counter[str] = Counter()
    for (hex_color, alpha), _count in colors.most_common():
        base = pascal_case(f"figma_{hex_color.replace('#', '')}")
        if alpha != 1:
            base = f"{base}A{round(alpha * 100)}"
        name = unique_name(base, used)
        argb = color_to_android(hex_color, alpha).replace("#", "")
        if len(argb) == 6:
            argb = f"FF{argb}"
        lines.append(f"    val {name} = Color(0x{argb})")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def render_generated_type_kt(styles: dict[str, dict[str, Any]], package_name: str) -> str:
    lines = [
        f"package {package_name}",
        "",
        "import androidx.compose.material3.Typography",
        "import androidx.compose.ui.text.TextStyle",
        "import androidx.compose.ui.text.font.FontFamily",
        "import androidx.compose.ui.text.font.FontWeight",
        "import androidx.compose.ui.unit.sp",
        "",
        "val GeneratedTypography = Typography(",
    ]
    entries = []
    for bucket, style in sorted(styles.items()):
        size = style.get("fontSize", 14)
        line_height = style.get("lineHeightPx", round(float(size) * 1.4, 2))
        weight = int(style.get("fontWeight", 400) or 400)
        letter_spacing = style.get("letterSpacing", 0)
        entries.append(
            f"    {bucket} = TextStyle(\n"
            f"        fontFamily = FontFamily.Default,\n"
            f"        fontWeight = FontWeight.W{weight},\n"
            f"        fontSize = {float(size):g}.sp,\n"
            f"        lineHeight = {float(line_height):g}.sp,\n"
            f"        letterSpacing = {float(letter_spacing):g}.sp\n"
            f"    )"
        )
    lines.append(",\n".join(entries))
    lines.append(")")
    lines.append("")
    return "\n".join(lines)


def render_spacing_kt(spacing: list[float], package_name: str) -> str:
    lines = [
        f"package {package_name}",
        "",
        "import androidx.compose.ui.unit.dp",
        "",
        "object NestorySpacing {",
    ]
    for value in spacing:
        label = str(int(value)) if value.is_integer() else str(value).replace(".", "_")
        lines.append(f"    val S{label} = {value:g}.dp")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def render_shape_kt(radius: list[float], package_name: str) -> str:
    lines = [
        f"package {package_name}",
        "",
        "import androidx.compose.foundation.shape.RoundedCornerShape",
        "import androidx.compose.ui.unit.dp",
        "",
        "object NestoryRadius {",
    ]
    for value in radius:
        label = str(int(value)) if value.is_integer() else str(value).replace(".", "_")
        lines.append(f"    val R{label} = RoundedCornerShape({value:g}.dp)")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def render_registry(name: str, resources: list[str], package_name: str) -> str:
    lines = [
        f"package {package_name}.ui.assets",
        "",
        f"import {package_name}.R",
        "",
        f"object {name} {{",
    ]
    for resource in resources:
        lines.append(f"    val {pascal_case(resource)} = R.drawable.{resource}")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="Import Figma specs/assets into the Nestory Android app.")
    parser.add_argument("figma_specs", type=Path, help="Path to figma_specs.json")
    parser.add_argument("--config", type=Path, default=Path("tools/figma-importer/import_config.json"))
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    config = load_config(args.config)
    tool_dir = args.config.parent.resolve()
    app_root = (tool_dir / config["app_root"]).resolve()

    payload = json.loads(args.figma_specs.read_text(encoding="utf-8"))
    root = normalize_specs_root(payload)

    drawable_dir = app_root / config["drawable_dir"]
    values_dir = app_root / config["values_dir"]
    theme_dir = app_root / config["theme_dir"]
    asset_registry_dir = app_root / config["asset_registry_dir"]

    colors = collect_colors(root)
    text_styles = collect_text_styles(root)
    spacing, radius = collect_spacing_and_radius(root)
    assets = collect_asset_exports(root)

    icons, images, warnings = copy_assets(assets, drawable_dir, config) if not args.dry_run else ([], [], [])

    outputs = {
        values_dir / "figma_colors.xml": render_figma_colors_xml(colors),
        theme_dir / "GeneratedColor.kt": render_generated_color_kt(colors, config["theme_package"]),
        theme_dir / "GeneratedType.kt": render_generated_type_kt(text_styles, config["theme_package"]),
        theme_dir / "Spacing.kt": render_spacing_kt(spacing, config["theme_package"]),
        theme_dir / "Shape.kt": render_shape_kt(radius, config["theme_package"]),
        asset_registry_dir / "AppIcons.kt": render_registry("AppIcons", icons, config["package_name"]),
        asset_registry_dir / "AppImages.kt": render_registry("AppImages", images, config["package_name"]),
    }

    if not args.dry_run:
        for path, content in outputs.items():
            write_text_if_changed(path, content)

    print(f"Colors: {len(colors)}")
    print(f"Text styles: {len(text_styles)}")
    print(f"Spacing tokens: {len(spacing)}")
    print(f"Radius tokens: {len(radius)}")
    print(f"Asset exports found: {len(assets)}")
    print(f"Icons imported: {len(icons)}")
    print(f"Images imported: {len(images)}")
    for warning in warnings:
        print(f"WARNING: {warning}")


if __name__ == "__main__":
    main()
