package com.example.nestory.ui.screen.documentkit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.relation.KitWithItems
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun DocumentKitListScreen(
    kits: List<KitWithItems>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onKitClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onCreateKitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = if (searchQuery.isBlank()) {
        kits
    } else {
        val query = searchQuery.trim().lowercase()
        kits.filter { kitWithItems ->
            val kit = kitWithItems.kit
            kit.name.lowercase().contains(query) ||
                kit.category?.lowercase()?.contains(query) == true ||
                kit.note?.lowercase()?.contains(query) == true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
            .statusBarsPadding()
            .padding(start = NestorySpacing.S24, end = NestorySpacing.S24, bottom = NestorySpacing.S16)
    ) {
        Spacer(modifier = Modifier.height(NestorySpacing.S40))
        KitSearchHeader(
            query = searchQuery,
            onQueryChange = onSearchQueryChange
        )
        Spacer(modifier = Modifier.height(NestorySpacing.S20))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(filtered, key = { it.kit.id }) { kitWithItems ->
                KitListCard(
                    kit = kitWithItems.kit,
                    items = kitWithItems.items,
                    onKitClick = { onKitClick(kitWithItems.kit.id) },
                    onToggleFavorite = { onToggleFavorite(kitWithItems.kit.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            item {
                KitCreateDashedButton(onClick = onCreateKitClick)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun KitSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Tìm theo tên, danh mục hoặc ghi chú",
                    style = NestoryTextStyles.Body11Semi,
                    color = GeneratedColor.Figma919191
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = AppIcons.IcSearch),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = GeneratedColor.Figma919191
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(45.dp),
            shape = RoundedCornerShape(NestorySpacing.S10),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GeneratedColor.FigmaFfffff,
                unfocusedContainerColor = GeneratedColor.FigmaFfffff,
                focusedIndicatorColor = GeneratedColor.FigmaEeeeee,
                unfocusedIndicatorColor = GeneratedColor.FigmaEeeeee
            ),
            textStyle = NestoryTextStyles.Body13Medium,
            singleLine = true
        )
        Box(
            modifier = Modifier
                .size(45.dp)
                .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(NestorySpacing.S10))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = AppIcons.DocumentConfig),
                contentDescription = "Cài đặt",
                modifier = Modifier.size(20.dp),
                tint = GeneratedColor.Figma919191
            )
        }
    }
}

@Composable
private fun KitListCard(
    kit: DocumentKitEntity,
    items: List<com.example.nestory.data.local.entity.KitItemEntity>,
    onKitClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val categoryVisual = kitCategoryVisual(kit.category)
    val (total, remaining) = kitItemCounts(items)
    val percent = kitProgressPercent(items)
    val remainingText = if (remaining == 0) "Đã hoàn thành"
    else "Còn $remaining mục chưa hoàn thành"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(14.dp))
            .padding(horizontal = NestorySpacing.S10)
            .clickable(onClick = onKitClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 77.dp, height = 100.dp)
                .background(categoryVisual.boxColor, RoundedCornerShape(NestorySpacing.S10))
                .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(NestorySpacing.S10)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = categoryVisual.iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = categoryVisual.iconTint
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = NestorySpacing.S10),
            verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
        ) {
            Text(
                text = kit.name,
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "$total mục yêu cầu",
                    style = NestoryTextStyles.Body12Semi,
                    color = GeneratedColor.Figma919191,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = remainingText,
                    style = NestoryTextStyles.Body12Semi,
                    color = GeneratedColor.Figma919191,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier
                .width(83.dp)
                .height(110.dp)
                .padding(vertical = NestorySpacing.S10),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(
                    id = if (kit.isFavorite) AppIcons.KitStarred else AppIcons.KitUnstarred
                ),
                contentDescription = if (kit.isFavorite) "Bỏ đánh dấu yêu thích" else "Đánh dấu yêu thích",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onToggleFavorite),
                tint = if (kit.isFavorite) GeneratedColor.Figma1a60e2 else GeneratedColor.Figma919191
            )
            KitProgressRing(percent = percent)
        }
    }
}

@Composable
private fun KitCreateDashedButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(1.dp, GeneratedColor.Figma808080, RoundedCornerShape(NestorySpacing.S10))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = AppIcons.IcPlus),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = GeneratedColor.Figma000000
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Tạo kit document mới",
            style = NestoryTextStyles.Body15Medium,
            color = GeneratedColor.Figma000000,
            textAlign = TextAlign.Center
        )
    }
}
