package com.example.nestory.ui.screen.container

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.ui.assets.AppIcons
import com.example.nestory.ui.assets.AppImages
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.example.nestory.ui.theme.GeneratedColor
import com.example.nestory.ui.theme.NestorySpacing
import com.example.nestory.ui.theme.NestoryTextStyles

@Composable
fun ContainerSelectionScreen(
    uiState: ContainerUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectContainer: (Long) -> Unit,
    onToggleContainer: (Long) -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onCloseBreadcrumb: () -> Unit,
    onBackClick: () -> Unit,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    selectionOnly: Boolean = false,
    allowCreate: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isEmpty = uiState.rootContainers.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeneratedColor.FigmaFfffff)
            .padding(horizontal = NestorySpacing.S20),
        verticalArrangement = Arrangement.spacedBy(NestorySpacing.S15)
    ) {
        Spacer(modifier = Modifier.height(NestorySpacing.S10))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = AppIcons.GlyphsArrowBold),
                    contentDescription = "Back",
                    modifier = Modifier.size(26.dp),
                    tint = GeneratedColor.Figma000000
                )
            }
            Text(
                text = "Chọn vị trí lưu trữ",
                style = NestoryTextStyles.Body20Bold,
                color = GeneratedColor.Figma000000
            )
        }

        if (!isEmpty) {
            ContainerSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange
            )
        }

        if (!isEmpty) {
            ContainerBreadcrumb(
                pathSegments = uiState.containerPath.map { it.name },
                onClose = onCloseBreadcrumb
            )
        }

        if (errorMessage != null) {
            ContainerErrorBanner(
                message = errorMessage,
                onDismiss = onDismissError
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Transparent)
        ) {
            if (isEmpty) {
                ContainerEmptyState()
            } else {
                ExpandableContainerList(
                    uiState = uiState,
                    onSelectContainer = onSelectContainer,
                    onToggleContainer = onToggleContainer,
                    onDeleteClick = onDeleteClick,
                    selectionOnly = selectionOnly
                )
            }
        }

        if (!selectionOnly) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = NestorySpacing.S10),
                horizontalArrangement = Arrangement.spacedBy(NestorySpacing.S10)
            ) {
                ContainerActionButton(
                    text = "Tạo container mới",
                    onClick = onCreateClick,
                    isPrimary = false,
                    isDashed = true,
                    modifier = Modifier.weight(1f)
                )
                // Management actions only become available after the user explicitly
                // selects/clicked a specific Container.
                if (uiState.selectedContainerId != null) {
                    ContainerActionButton(
                        text = "Chỉnh sửa container",
                        onClick = onEditClick,
                        isPrimary = false,
                        isDashed = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else if (allowCreate) {
            ContainerActionButton(
                text = "Tạo container mới",
                onClick = onCreateClick,
                isPrimary = false,
                isDashed = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NestorySpacing.S10)
            )
        }

        // The confirm action only has a purpose inside a picker flow (e.g. choosing
        // the storage location for a Document). On the main Container management
        // screen it is redundant because no explicit selection is required to leave.
        if (!isEmpty && selectionOnly) {
            ContainerActionButton(
                text = "Xác nhận vị trí",
                onClick = onConfirmClick,
                isPrimary = true,
                textStyle = NestoryTextStyles.Body16Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(NestorySpacing.S20))
    }
}

@Composable
fun ExpandableContainerList(
    uiState: ContainerUiState,
    onSelectContainer: (Long) -> Unit,
    onToggleContainer: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    selectionOnly: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(NestorySpacing.S10))
            .clip(RoundedCornerShape(NestorySpacing.S10))
            .verticalScroll(rememberScrollState())
    ) {
        uiState.rootContainers.forEach { container ->
            ContainerGroup(
                container = container,
                uiState = uiState,
                onSelectContainer = onSelectContainer,
                onToggleContainer = onToggleContainer,
                onDeleteClick = onDeleteClick,
                selectionOnly = selectionOnly
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = GeneratedColor.FigmaE5e7eb
            )
        }
    }
}

@Composable
fun ContainerGroup(
    container: ContainerEntity,
    uiState: ContainerUiState,
    onSelectContainer: (Long) -> Unit,
    onToggleContainer: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    selectionOnly: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EmitContainerRows(
            container = container,
            level = 0,
            uiState = uiState,
            onSelectContainer = onSelectContainer,
            onToggleContainer = onToggleContainer,
            onDeleteClick = onDeleteClick,
            selectionOnly = selectionOnly
        )
    }
}

@Composable
private fun EmitContainerRows(
    container: ContainerEntity,
    level: Int,
    uiState: ContainerUiState,
    onSelectContainer: (Long) -> Unit,
    onToggleContainer: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    selectionOnly: Boolean = false
) {
    val hasChildren = uiState.getChildren(container.id).isNotEmpty()
    val isExpanded = uiState.isExpanded(container.id)

    ContainerItem(
        name = container.name,
        isSelected = container.id == uiState.selectedContainerId,
        isExpanded = isExpanded,
        hasChildren = hasChildren,
        level = level,
        showDelete = !selectionOnly && container.id == uiState.selectedContainerId,
        onToggle = { onToggleContainer(container.id) },
        onItemClick = { onSelectContainer(container.id) },
        onDeleteClick = if (selectionOnly) null else ({ onDeleteClick(container.id) })
    )

    if (isExpanded) {
        val children = uiState.getChildren(container.id)
        children.forEachIndexed { index, child ->
            EmitContainerRows(
                container = child,
                level = level + 1,
                uiState = uiState,
                onSelectContainer = onSelectContainer,
                onToggleContainer = onToggleContainer,
                onDeleteClick = onDeleteClick,
                selectionOnly = selectionOnly
            )
        }
    }
}

@Composable
fun ContainerEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, GeneratedColor.FigmaE5e7eb, RoundedCornerShape(NestorySpacing.S10))
            .clip(RoundedCornerShape(NestorySpacing.S10)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(NestorySpacing.S20),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = AppImages.ContainerEmptyState),
                contentDescription = null,
                modifier = Modifier.size(width = 254.dp, height = 199.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S20))
            Text(
                text = "Chưa có container nào",
                style = NestoryTextStyles.Title22Semi,
                color = GeneratedColor.Figma000000,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(NestorySpacing.S10))
            Text(
                text = "Thêm container đầu tiên để bắt đầu quản lý trong Nestory",
                style = NestoryTextStyles.Body13Semi,
                color = GeneratedColor.Figma919191,
                textAlign = TextAlign.Center
            )
        }
    }
}
