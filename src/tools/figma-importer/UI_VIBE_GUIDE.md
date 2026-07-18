# Nestory UI Vibe Guide

Guide này ghi lại các kinh nghiệm từ lần dựng UI Nestory bằng Figma output local. Mục tiêu là lần vibe tiếp theo ra UI ổn hơn, ít gọi Figma API dư, ít lệch spacing/font/asset trên device.

## 1. Dùng Figma Tool Vừa Đủ

### Scope token cần bật

Chỉ cần:

- `file_content:read`
- `file_metadata:read`

Không cần bật write/webhook/project/library nếu chỉ đọc design và export asset cho UI prototype.

### Command khuyến nghị

Dùng `semantic` cho UI work bình thường:

```bash
export FIGMA_TOKEN='figd_xxx'

python tools/figma-importer/figma_extract_full.py \
  'https://www.figma.com/design/FILE_KEY/FILE_NAME?t=TOKEN' \
  --out-dir build/figma/Nestory_semantic_vN \
  --asset-mode semantic \
  --formats png \
  --scale 2 \
  --request-delay 15
```

Lý do:

- `semantic` chỉ render các node có ý nghĩa như logo, illustration, icons, nav, loading, chevron.
- Ít request render hơn, tránh Figma API 429.
- Có thêm blueprint/manifest để code không phải đoán.

### Khi nào dùng mode khác

- `--no-assets`: dùng để lấy text/spec/tokens rất nhanh, gần như không tốn render quota.
- `--asset-mode semantic`: dùng mặc định cho vibe UI.
- `--asset-mode assets`: chỉ dùng khi thiếu nhiều icon leaf/vector nhỏ.
- `--asset-mode major` hoặc `all`: hạn chế dùng. Dễ đụng rate limit vì render nhiều node.

### Khi bị 429

Không revoke token ngay. 429 là quota/rate limit endpoint render, không phải token hỏng.

Nếu lỗi có `Retry-After` rất lớn, dừng render asset và chạy:

```bash
python tools/figma-importer/figma_extract_full.py \
  'FIGMA_URL' \
  --out-dir build/figma/Some_specs_only \
  --no-assets
```

Sau đó chờ quota hồi hoặc dùng `semantic` với file/node nhỏ hơn.

## 2. Output Nào Phải Đọc Trước Khi Code

Luôn đọc các file này trước:

- `figma_screen_blueprints.md`
- `figma_asset_manifest.md`
- `figma_ai_context.md`
- `figma_specs.md`

Ưu tiên theo thứ tự:

1. `figma_screen_blueprints.md`: tọa độ screen, direct children, text node, asset node.
2. `figma_asset_manifest.md`: asset nào dùng cho màn nào, role gì, kích thước render.
3. `figma_ai_context.md`: overview cho AI.
4. `figma_specs.md`: token màu/type/spacing tổng quan.

Không chỉ đọc specs rồi code. Specs thiếu quan hệ cụm, thứ tự layout và một số asset context.

## 3. Quy Tắc Dựng Compose Từ Figma

### Không biến cả màn thành PNG

Chỉ dùng PNG cho:

- logo
- illustration lớn
- icon khó vẽ/tint
- loading/icon đặc thù Figma

Layout vẫn phải dựng bằng Compose:

- `Column`
- `Row`
- `Box`
- `Card`
- `Surface`
- `Text`
- `Button`

### Group spacing quan trọng hơn spacing đều

Không đặt spacer đều 10/14dp cho mọi chỗ. UI cần nhịp theo cụm:

- Logo + app name: gần nhau.
- Title + subtitle: gần nhau.
- Description -> feature list/form: cách rõ hơn.
- Content group -> action button: cách rõ hơn.
- Trong list item: compact và đều.

Ví dụ Start screen:

- top -> logo: vừa phải
- logo/name -> title: medium
- title -> subtitle: small
- subtitle -> features: large
- giữa feature rows: medium
- features -> button: large/medium

### Tránh height cứng nếu content không cần

Height cứng dễ tạo khoảng trắng dư:

- Card option trong Create Vault không nên cố định `268.dp` nếu content chỉ cần wrap.
- Recent card có thể height cố định vì format lặp lại, nhưng text phải `maxLines`.
- Bottom nav nên height ổn định.

Quy tắc:

- Dùng fixed size cho component format cố định: nav, keypad, recent card, icon box.
- Dùng wrap content cho card/form nếu số item có thể thay đổi.

### Dùng system bar padding đúng chỗ

Figma frame thường không tính status bar thật của Android. Trên device, top content dễ bị đè.

- Màn có nút Back/top control: bật `statusBarsPadding`.
- Màn đã chủ động chừa top lớn hoặc cần bám hero Figma: kiểm tra bằng screenshot trước khi bật.

## 4. Typography Và Font

### Inter variable phải map weight axis

Nếu dùng Inter variable font, khai báo cùng một file với `FontWeight.W900` chưa chắc Android render đúng weight. Cần dùng:

```kotlin
FontVariation.Settings(FontVariation.weight(weight.weight))
```

Nếu chữ vẫn khác Figma:

- Android rasterize text khác Figma, đặc biệt ở emulator.
- Không tăng toàn bộ lên quá cao. Dễ bị rỗ/gắt.
- Tốt nhất tune theo mức:
  - heading/title: khoảng `W700`
  - section/card title: `W600`
  - body/subtitle: `W500` hoặc `W600`
  - tiny text: `W500` hoặc `W600`

### Không override fontWeight lung tung

Nếu `Text(style = NestoryTextStyles.X, fontWeight = ...)`, override đó có thể làm style trung tâm mất tác dụng.

Ưu tiên:

```kotlin
Text(style = NestoryTextStyles.Body13Semi)
```

Chỉ override khi thật sự cần khác style.

## 5. Asset Và Icon

### Luôn kiểm tra asset có đúng màu không

Một số asset Figma export có foreground trắng/trong suốt. Nếu đặt lên nền trắng sẽ như biến mất.

Ví dụ đã gặp:

- fingerprint icon export ra màu trắng, cần tint xanh khi render.

Checklist asset:

- Mở file PNG bằng viewer nếu icon không thấy.
- Kiểm tra kích thước thật bằng `file` hoặc `identify`.
- Nếu asset là cả frame có nền + icon, dùng trực tiếp.
- Nếu asset chỉ là glyph trắng, cần `ColorFilter.tint(...)` hoặc dùng asset khác.

### Dùng đúng semantic asset thay vì tự ghép

Nếu Figma có `Icon Frame` đã export, ưu tiên dùng nguyên frame đó.

Ví dụ container row:

- Sai: tự vẽ box màu rồi nhét icon khác vào.
- Đúng: dùng `Icon_Frame_1-107.png`, `Icon_Frame_1-119.png` từ semantic output.

## 6. Text Overflow

Mọi text nằm trong card/list nhỏ phải có rule overflow.

Recommended:

```kotlin
Text(
    text = subtitle,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
)
```

Áp dụng cho:

- recent document card title/subtitle
- container path
- bottom nav label nếu dài
- alert row title/status nếu dùng data thật

Không tự cắt string thủ công nếu Compose có `TextOverflow`.

## 7. Responsive Trên Nhiều Dòng Android

Những thứ cần tránh:

- `Spacer(weight = 1f)` trong màn cần match Figma, vì nó kéo giãn khác nhau theo device height.
- hardcode quá nhiều tọa độ tuyệt đối.
- card nhỏ có text nhiều dòng nhưng không set max line.
- horizontal scroll khi yêu cầu hiển thị đủ N item trên màn.

Những thứ nên dùng:

- `Modifier.weight(1f)` cho 4 card cần chia đều width.
- `Arrangement.spacedBy(...)` cho gap cố định.
- `maxLines` + `TextOverflow.Ellipsis`.
- fixed button/keypad/icon dimensions cho component format cố định.
- `verticalScroll` cho màn Home hoặc màn có thể dài.

## 8. Animation Nên Có

Prototype không nên chuyển màn cứng bằng `when` trần.

Nên có:

- Route transition: slide nhẹ + fade.
- Waiting screen: artwork pulse nhẹ, checklist hiện dần.
- Success screen: loading ring xoay thật.
- Fingerprint: pulse khi đang xác minh, delay ngắn trước success.
- PIN: dots animate khi nhập, delay ngắn trước success.

Không nên lạm dụng:

- animation quá nhanh gây nhấp nháy
- bounce mạnh
- delay quá lâu làm test flow khó chịu
- animation thay đổi layout size quá nhiều gây giật

Thông số hợp lý:

- route transition: 180-260ms
- press/dot feedback: 120-180ms
- mock auth delay: 300-600ms
- loading flow: 1.2-2.5s

## 9. Checklist Trước Khi Nói UI Xong

1. Chạy `./gradlew assembleDebug`.
2. Install lên emulator/device.
3. Chụp hoặc nhìn thật trên device, không chỉ dựa Figma.
4. Check các màn:
   - Start
   - Create Vault
   - Waiting
   - Unlock Choice
   - Fingerprint modal
   - PIN
   - Success/loading
   - Home
5. Check lỗi thường gặp:
   - topbar/status bar đè content
   - text quá mỏng/quá rỗ
   - card border quá đậm
   - icon sai màu hoặc biến mất
   - card/list text tràn
   - gap giữa cards quá xa
   - card có height cứng gây dư đáy
   - loading đứng yên
   - chuyển màn cắt cảnh

## 10. Khi Cần Vibe Lần Sau

Prompt ngắn nên gồm:

```text
Dựa vào build/figma/Nestory_semantic_vN, hãy chỉnh UI Compose theo Figma.
Đọc figma_screen_blueprints.md và figma_asset_manifest.md trước.
Chỉ dùng local output, không gọi Figma API.
Không biến màn thành PNG.
Ưu tiên group spacing, overflow text, safe-area, đúng semantic assets.
Verify bằng ./gradlew assembleDebug và install lên device/emulator.
```

Nếu cần pixel-match hơn, gửi thêm screenshot Figma của từng screen và screenshot device hiện tại. Khi so sánh, luôn tách rõ:

- lỗi do thiếu asset/spec
- lỗi do Compose layout/spacing
- lỗi do Android system bars
- lỗi do font rasterization
- lỗi do responsive/device width
