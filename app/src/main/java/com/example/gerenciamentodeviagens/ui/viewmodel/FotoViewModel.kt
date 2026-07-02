package com.example.gerenciamentodeviagens.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gerenciamentodeviagens.data.repository.FotoRepository
import com.example.gerenciamentodeviagens.model.Foto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FotoViewModel(private val repository: FotoRepository) : ViewModel() {

    private val _viagemId = MutableStateFlow<Int?>(null)

    val fotos: Flow<List<Foto>> = _viagemId.flatMapLatest { id ->
        if (id != null) repository.listarFotos(id)
        else flowOf(emptyList())
    }

    fun setViagemId(id: Int) {
        _viagemId.value = id
    }

    fun adicionarFoto(caminho: String) {
        val currentId = _viagemId.value ?: return
        viewModelScope.launch {
            repository.inserir(Foto(viagemId = currentId, caminho = caminho))
        }
    }

    fun excluirFoto(foto: Foto) {
        viewModelScope.launch {
            repository.excluir(foto)
        }
    }

    class Factory(private val repository: FotoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FotoViewModel(repository) as T
    }
}
