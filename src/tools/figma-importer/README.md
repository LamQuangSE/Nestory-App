# Figma Importer for Nestory

This tool imports Figma-exported assets and design tokens into the Android Jetpack Compose app.

For practical UI implementation notes, spacing rules, asset pitfalls, font handling, overflow rules,
and animation guidance, read [`UI_VIBE_GUIDE.md`](./UI_VIBE_GUIDE.md) before starting a new UI pass.

## Workflow

1. Generate a Figma token with these scopes:

   - `file_content:read`
   - `file_metadata:read`
   - Optional: `library_assets:read`, `library_content:read`, `team_library_content:read`, `projects:read`

2. Extract the Figma file:

   ```bash
   cd /home/tiephua/Documents/school/nmcnpm/nestory-project/src
   export FIGMA_TOKEN='figd_xxx'
   python tools/figma-importer/figma_extract_full.py 'https://www.figma.com/design/FILE_KEY/FILE_NAME' \
     --out-dir build/figma/Detailed_Design_UI \
     --asset-mode semantic \
     --formats png \
     --scale 2
   ```

   Use `--no-assets` if you only need specs/tokens and want to avoid the Figma image render rate limit.
   Use `--asset-mode semantic` for normal UI work. It exports named/grouped assets such as logo,
   illustration, fingerprint, vault, notification, and nav icons, then writes:

   - `figma_asset_manifest.json`
   - `figma_asset_manifest.md`
   - `figma_ai_build_packet.md`
   - `figma_screen_blueprints.json`
   - `figma_screen_blueprints.md`
   - `figma_text_nodes.json`

   Use `--asset-mode assets` only for the old behavior that exports many vector leaf nodes.
   Avoid `--asset-mode all` unless you have enough Figma API quota; it renders every visible node and can hit
   the Tier 1 `GET image` limit quickly.

   The screen blueprints include each screen's direct children, relative positions, sizes, text nodes, and
   asset nodes. Use them when implementing Compose layouts so spacing, typography, and screen structure are
   not guessed from the summary.

   Read the generated files by fidelity level:

   - `figma_ai_build_packet.md`: primary AI handoff for building all extracted screens in one pass; read this first.
   - `figma_ai_context.md`: quick orientation only.
   - `figma_screen_blueprints.md`: screen structure, direct children, relative positions, text, and assets.
   - `figma_specs.md`: human-readable detail for sizes, fills, strokes, radius, layout, text, and assets.
   - `figma_specs.json`: source of truth for implementation; verify exact fields such as `strokeDashes`,
     `strokeAlign`, `strokeCap`, `strokeJoin`, image fills, and nested node attributes here.
   - `figma_raw.json`: untouched Figma API response; use it to audit suspected missing details or extractor bugs.

   Do not implement from screenshots, `figma_ai_context.md`, or full-file skimming alone. Use
   `figma_ai_build_packet.md` for the all-screen plan and risk report, then inspect matching nodes in
   `figma_specs.json` or `figma_raw.json` before choosing a fallback.

   Minimum no-missing-details checklist before implementing a screen:

   1. Pick the exact screen/state in `figma_screen_blueprints.md`.
   2. Read its contract and risk nodes in `figma_ai_build_packet.md`.
   3. Use direct children and text nodes from the blueprint to identify the relevant node names/positions.
   4. Open each important node by `id` in `figma_specs.json`.
   5. Verify geometry, layout, fills, strokes, radius, text, assets, effects, children order, and interactions.
   6. For detail-critical fields, explicitly check `strokeDashes`, `strokeAlign`, `strokeCap`, `strokeJoin`,
      `strokeWeight`, `fills`, `strokes`, `opacity`, `blendMode`, `cornerRadius`, `rectangleCornerRadii`,
      `assetExports`, image fills, `componentProperties`, and `reactions`.
   7. If preview/markdown/spec disagree, inspect the same node `id` in `figma_raw.json`.

   Useful audit commands:

   ```bash
   jq '.. | objects | select(.id? == "NODE_ID")' build/figma/Detailed_Design_UI/figma_specs.json
   jq '.. | objects | select(.id? == "NODE_ID")' build/figma/Detailed_Design_UI/figma_raw.json
   jq '[.. | objects | select(.strokeDashes? != null) | {id,name,type,strokeDashes}]' build/figma/Detailed_Design_UI/figma_raw.json
   ```

   The asset manifest includes semantic metadata for each exported node:

   - `role` such as `logo`, `feature_icon`, `nav_icon`, `notification`, `fingerprint_icon`, `loading`, `chevron`
   - owning `screen`
   - `rectInScreen`
   - rendered PNG dimensions
   - source node path

3. Import assets and generated Compose tokens:

   ```bash
   python tools/figma-importer/import_figma_to_android.py \
     build/figma/Detailed_Design_UI/figma_specs.json \
     --config tools/figma-importer/import_config.json
   ```

4. Validate:

   ```bash
   ./gradlew assembleDebug
   ```

## Rules

- UI assets go to `app/src/main/res/drawable/`.
- Launcher icons only go to `app/src/main/res/mipmap-*`.
- Android resource names must be lowercase snake case.
- Icons use `ic_`.
- Images/illustrations use `img_`.
- Backgrounds use `bg_`.
- Simple SVG can become VectorDrawable XML.
- SVG with `filter`, `mask`, `clipPath`, `foreignObject`, `radialGradient`, `pattern`, or blur is treated as risky and should use bitmap fallback.
- Generated Compose files are added separately instead of overwriting hand-written theme files.

## Generated Files

- `app/src/main/res/drawable/*`
- `app/src/main/res/values/figma_colors.xml`
- `app/src/main/java/com/example/nestory/ui/theme/GeneratedColor.kt`
- `app/src/main/java/com/example/nestory/ui/theme/GeneratedType.kt`
- `app/src/main/java/com/example/nestory/ui/theme/Spacing.kt`
- `app/src/main/java/com/example/nestory/ui/theme/Shape.kt`
- `app/src/main/java/com/example/nestory/ui/assets/AppIcons.kt`
- `app/src/main/java/com/example/nestory/ui/assets/AppImages.kt`
