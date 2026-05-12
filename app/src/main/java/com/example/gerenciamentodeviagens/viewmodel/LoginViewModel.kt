package com.example.gerenciamentodeviagens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gerenciamentodeviagens.data.repository.UsuarioRepository
import com.example.gerenciamentodeviagens.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UsuarioRepository) : ViewModel() {

    private val _usuarioLogado = MutableStateFlow<Usuario?>(null)
    val usuarioLogado: StateFlow<Usuario?> = _usuarioLogado

    private val _erroMensagem = MutableStateFlow<String?>(null)
    val erroMensagem: StateFlow<String?> = _erroMensagem

    fun efetuarLogin(email: String, senha: String) {
        viewModelScope.launch {
            _erroMensagem.value = null
            val result = repository.login(email, senha)
            result.onSuccess {
                _usuarioLogado.value = it
            }.onFailure {
                _usuarioLogado.value = null
                _erroMensagem.value = it.message ?: "E-mail ou senha incorretos"
            }
        }
    }

    fun resetarErro() {
        _erroMensagem.value = null
    }
}