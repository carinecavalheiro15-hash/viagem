package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.gerenciamentodeviagens.viewmodel.LoginViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.gerenciamentodeviagens.model.Usuario

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSucesso: (Usuario) -> Unit,
    onIrParaRegistro: () -> Unit,
    onEsqueciSenha: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }

    val usuarioLogado by viewModel.usuarioLogado.collectAsState()
    val erroMensagem by viewModel.erroMensagem.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(usuarioLogado) {
        usuarioLogado?.let { onLoginSucesso(it) }
    }

    LaunchedEffect(erroMensagem) {
        erroMensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetarErro()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Login", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                        Icon(
                            imageVector = if (mostrarSenha) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar senha"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.efetuarLogin(email, senha) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }

            TextButton(onClick = onIrParaRegistro) {
                Text("Novo usuário")
            }

            TextButton(onClick = onEsqueciSenha) {
                Text("Esqueci a senha")
            }
        }
    }
}