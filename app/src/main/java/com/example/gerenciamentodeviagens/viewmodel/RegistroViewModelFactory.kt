package com.example.gerenciamentodeviagens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gerenciamentodeviagens.data.repository.UsuarioRepository

class RegistroViewModelFactory(
    private val repository: UsuarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Usando o caminho completo para evitar qualquer ambiguidade
        if (modelClass.isAssignableFrom(com.example.gerenciamentodeviagens.ui.viewmodel.RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.example.gerenciamentodeviagens.ui.viewmodel.RegistroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
