package com.example.gerenciamentodeviagens.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gerenciamentodeviagens.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RoteiroUiState {
    object Idle : RoteiroUiState()
    object Loading : RoteiroUiState()
    data class Sucesso(val roteiro: String) : RoteiroUiState()
    data class Erro(val mensagem: String) : RoteiroUiState()
}

class RoteiroViewModel(private val repository: GeminiRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RoteiroUiState>(RoteiroUiState.Idle)
    val uiState: StateFlow<RoteiroUiState> = _uiState

    // Chave fornecida pelo usuário
    private val API_KEY = "CHAVE_REMOVIDA"

    fun gerarRoteiro(
        destino: String,
        dias: Int,
        tipoViagem: String,
        epoca: String,
        orcamentoPretendido: String,
        interesses: String
    ) {
        if (API_KEY.isBlank()) {
            _uiState.value = RoteiroUiState.Erro("API Key não configurada.")
            return
        }

        viewModelScope.launch {
            _uiState.value = RoteiroUiState.Loading
            val resultado = repository.gerarRoteiro(
                apiKey = API_KEY,
                destino = destino,
                dias = dias,
                tipoViagem = tipoViagem,
                epoca = epoca,
                orcamentoPretendido = orcamentoPretendido,
                interesses = interesses
            )
            if (resultado != null) {
                _uiState.value = RoteiroUiState.Sucesso(resultado)
            } else {
                _uiState.value = RoteiroUiState.Erro("Falha ao gerar roteiro.")
            }
        }
    }

    fun resetarEstado() {
        _uiState.value = RoteiroUiState.Idle
    }

    class Factory(private val repository: GeminiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RoteiroViewModel(repository) as T
    }
}
