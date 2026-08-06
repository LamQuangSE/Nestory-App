package com.example.nestory.ui.screen.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nestory.domain.repository.ContainerRepository

/**
 * Factory for creating [ContainerViewModel] instances.
 */
class ContainerViewModelFactory(
    private val containerRepository: ContainerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContainerViewModel::class.java)) {
            return ContainerViewModel(containerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}