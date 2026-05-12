package com.example.gerenciamentodeviagens.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gerenciamentodeviagens.data.repository.ViagemRepository
import com.example.gerenciamentodeviagens.model.Viagem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ViagemViewModel(private val repository: ViagemRepository) : ViewModel() {

    private val _userId = MutableStateFlow<Int?>(null)
    
    val viagens = _userId.flatMapLatest { id ->
        if (id != null) repository.listarViagens(id)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }

    fun setUserId(id: Int) {
        _userId.value = id
    }

    fun salvarViagem(viagem: Viagem) {
        viewModelScope.launch {
            if (viagem.id == 0) repository.inserir(viagem)
            else repository.atualizar(viagem)
        }
    }

    fun excluirViagem(viagem: Viagem) {
        viewModelScope.launch {
            repository.excluir(viagem)
        }
    }

    class Factory(private val r: ViagemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ViagemViewModel(r) as T
    }
}
