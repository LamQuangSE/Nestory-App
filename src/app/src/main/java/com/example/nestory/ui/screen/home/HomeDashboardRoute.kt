package com.example.nestory.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.repository.ContainerRepositoryImpl
import com.example.nestory.data.repository.DocumentRepositoryImpl

@Composable
fun HomeDashboardRoute(
    onOpenAll: () -> Unit,
    onAddDocument: () -> Unit,
    onRecentDocumentClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val factory = remember {
        HomeDashboardViewModelFactory(
            documentRepository = DocumentRepositoryImpl(db.documentDao()),
            containerRepository = ContainerRepositoryImpl(db.containerDao()),
        )
    }
    val viewModel: HomeDashboardViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    HomeDashboardScreen(
        uiState = uiState,
        onOpenAll = onOpenAll,
        onAddDocument = onAddDocument,
        onRecentDocumentClick = onRecentDocumentClick,
    )
}