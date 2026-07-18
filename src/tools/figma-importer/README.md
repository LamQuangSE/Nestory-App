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
   - `figma_screen_blueprints.json`
   - `figma_screen_blueprints.md`
   - `figma_text_nodes.json`

   Use `--asset-mode assets` only for the old behavior that exports many vector leaf nodes.
   Avoid `--asset-mode all` unless you have enough Figma API quota; it renders every visible node and can hit
   the Tier 1 `GET image` limit quickly.

   The screen blueprints include each screen's direct children, relative positions, sizes, text nodes, and
   asset nodes. Use them when implementing Compose layouts so spacing, typography, and screen structure are
   not guessed from the summary.

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
