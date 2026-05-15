package com.example.gerenciamentodeviagens.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gerenciamentodeviagens.data.repository.ViagemRepository
import com.example.gerenciamentodeviagens.model.Viagem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ViagemViewModel(private val repository: ViagemRepository) : ViewModel() {

    private val _userId = MutableStateFlow<Int?>(null)
    
    val viagens = _userId.flatMapLatest { id ->
        if (id != null) repository.listarViagens(id)
        else flowOf(emptyList())
    }

    private val _viagensAtuais = MutableStateFlow<List<Viagem>>(emptyList())
    val viagensAtuais: StateFlow<List<Viagem>> = _viagensAtuais

    private val _cidadeAtual = MutableStateFlow<String?>(null)
    val cidadeAtual: StateFlow<String?> = _cidadeAtual

    fun setUserId(id: Int) {
        _userId.value = id
    }

    fun buscarViagensPelaCidade(userId: Int, cidade: String) {
        _cidadeAtual.value = cidade
        viewModelScope.launch {
            val hoje = System.currentTimeMillis()
            // Busca a lista de todas as viagens que batem com a cidade e data
            _viagensAtuais.value = repository.buscarViagensAtuais(userId, cidade, hoje)
        }
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
