package com.example.nestory.ui.screen.documentkit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun DocumentKitItemListScreen(
    items: List<KitItemEntity>,
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onCreateItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
            .statusBarsPadding()
            .padding(horizontal = NestorySpacing.S12)
            .padding(vertical = 7.dp)
    ) {
        KitTopBar(
            title = "Danh sách mục yêu cầu",
            onBack = onBackClick,
            trailingContent = {
                IconButton(onClick = onCreateItemClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = AppIcons.IcPlus),
                        contentDescription = "Tạo Item",
                        modifier = Modifier.size(20.dp),
                        tint = GeneratedColor.Figma000000
                    )
                }
            }
        )

        if (items.isEmpty()) {
            ItemListEmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    KitItemListCard(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ItemListEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = AppImages.ImgEmptyCategory),
            contentDescription = null,
            modifier = Modifier.size(width = 282.dp, height = 210.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Danh sách hiện đang rỗng\nhãy thêm item mới",
            style = NestoryTextStyles.Body14Medium,
            color = GeneratedColor.Figma919191,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun KitItemListCard(
    item: KitItemEntity,
    onClick: () -> Unit,
) {
    val visual = kitStatusVisual(item.status)
    val description = item.description.orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(14.dp))
            .padding(horizontal = NestorySpacing.S10)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
    ) {
        Icon(
            painter = painterResource(id = visual.iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = visual.iconTint
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
        ) {
            Text(
                text = item.name?.ifBlank { "Item chưa đặt tên" } ?: "Item chưa đặt tên",
                style = NestoryTextStyles.Body15Semi,
                color = GeneratedColor.Figma000000,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = NestoryTextStyles.Body12Semi,
                    color = GeneratedColor.Figma000000,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .background(visual.bgColor, RoundedCornerShape(5.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = visual.label,
                    style = NestoryTextStyles.Body12Semi,
                    color = visual.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            painter = painterResource(id = AppIcons.LsiconRightFilled),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = GeneratedColor.Figma000000
        )
    }
}
