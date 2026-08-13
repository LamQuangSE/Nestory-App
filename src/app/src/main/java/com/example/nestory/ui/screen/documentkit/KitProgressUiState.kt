package com.example.nestory.ui.screen.documentkit

import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.relation.KitWithItems
import com.example.nestory.ui.screen.document.parseExpirationDate
import java.time.LocalDate
import kotlin.math.roundToInt

data class KitProgressItem(
    val id: Long,
    val name: String?,
    val description: String?,
    val status: String,
    val linkedDocumentId: Long?,
    val linkedDocumentTitle: String?,
    val linkedDocumentExpirationDate: String?,
)

data class KitProgressUiState(
    val isLoading: Boolean = true,
    val kit: KitWithItems? = null,
    val items: List<KitProgressItem> = emptyList(),
    val totalItems: Int = 0,
    val readyCount: Int = 0,
    val remainingCount: Int = 0,
    val progressPercent: Int = 0,
    val statusDistribution: List<Pair<String, Int>> = emptyList(),
    val error: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && kit != null && totalItems == 0
}

/**
 * Derives the kit progress / status-distribution state from the raw kit + its items
 * and the linked documents. This is a pure function fed by the SAME sources that
 * drive the item chips (DocumentKitViewModel.uiState) so the distribution can never
 * drift out of sync with the item statuses.
 */
fun buildKitProgressUiState(
    kit: KitWithItems?,
    documentsById: Map<Long, DocumentEntity>,
    isLoading: Boolean = false,
    error: String? = null,
): KitProgressUiState {
    val items = kit?.items.orEmpty().map { item ->
        val document = item.linkedDocumentId?.let(documentsById::get)
        toKitProgressItem(item, document)
    }

    val total = items.size
    val ready = items.count { it.status == KitItemStatus.READY }
    val remaining = total - ready
    val percent = if (total == 0) 0 else ((total - remaining).toFloat() / total * 100).roundToInt()

    val distribution = listOf(
        KitItemStatus.READY,
        KitItemStatus.NEED_REVIEW,
        KitItemStatus.MISSING,
        KitItemStatus.EXPIRED,
    ).map { status -> status to items.count { it.status == status } }

    return KitProgressUiState(
        isLoading = isLoading,
        kit = kit,
        items = items,
        totalItems = total,
        readyCount = ready,
        remainingCount = remaining,
        progressPercent = percent,
        statusDistribution = distribution,
        error = error,
    )
}

fun toKitProgressItem(
    item: KitItemEntity,
    document: DocumentEntity?,
): KitProgressItem = KitProgressItem(
    id = item.id,
    name = item.name,
    description = item.description,
    status = resolveKitItemStatus(item, document),
    linkedDocumentId = item.linkedDocumentId,
    linkedDocumentTitle = document?.title,
    linkedDocumentExpirationDate = document?.expirationDate,
)

private fun resolveKitItemStatus(
    item: KitItemEntity,
    document: DocumentEntity?,
): String {
    val linkedDocExpired = document?.expirationDate
        ?.let(::parseExpirationDate)
        ?.isBefore(LocalDate.now()) == true
    if (linkedDocExpired) {
        return KitItemStatus.EXPIRED
    }
    return when (item.status) {
        KitItemStatus.READY -> KitItemStatus.READY
        KitItemStatus.NEED_REVIEW -> KitItemStatus.NEED_REVIEW
        KitItemStatus.EXPIRED -> KitItemStatus.EXPIRED
        else -> KitItemStatus.MISSING
    }
}
