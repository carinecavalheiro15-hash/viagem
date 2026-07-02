package com.example.gerenciamentodeviagens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gerenciamentodeviagens.data.repository.UsuarioRepository
import com.example.gerenciamentodeviagens.ui.viewmodel.RegistroViewModel

class RegistroViewModelFactory(
    private val repository: UsuarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
