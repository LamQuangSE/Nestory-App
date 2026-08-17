package com.example.nestory.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.AttachmentRepositoryImpl
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentKitRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl

@Composable
fun HomeDashboardRoute(
    onOpenDocuments: () -> Unit,
    onOpenKits: () -> Unit,
    onKitClick: (Long) -> Unit,
    onAddDocument: () -> Unit,
    onRecentDocumentClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val factory = remember {
        HomeDashboardViewModelFactory(
            documentRepository = DocumentRepositoryImpl(db.documentDao()),
            containerRepository = ContainerRepositoryImpl(db.containerDao()),
            categoryRepository = com.example.nestory.data.repository.CategoryRepositoryImpl(db.categoryDao()),
            attachmentRepository = AttachmentRepositoryImpl(db.attachmentDao()),
            documentKitRepository = DocumentKitRepositoryImpl(db.documentKitDao()),
        )
    }
    val viewModel: HomeDashboardViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    HomeDashboardScreen(
        uiState = uiState,
        onOpenDocuments = onOpenDocuments,
        onOpenKits = onOpenKits,
        onKitClick = onKitClick,
        onAddDocument = onAddDocument,
        onRecentDocumentClick = onRecentDocumentClick,
    )
}
