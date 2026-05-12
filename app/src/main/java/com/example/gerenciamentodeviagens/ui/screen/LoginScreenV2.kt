package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciamentodeviagens.model.Usuario
import com.example.gerenciamentodeviagens.viewmodel.LoginViewModel

@Composable
fun LoginScreenV2(
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

    // Degradê de fundo
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF6200EE), Color(0xFF3700B3))
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TravelManager",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Sua próxima aventura começa aqui",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Bem-vinda de volta!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail") },
                            leadingIcon = { Icon(Icons.Default.Mail, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = senha,
                            onValueChange = { senha = it },
                            label = { Text("Senha") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                                    Icon(if (mostrarSenha) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.efetuarLogin(email, senha) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Text("ENTRAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        TextButton(onClick = onEsqueciSenha) {
                            Text("Esqueceu sua senha?", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onIrParaRegistro) {
                    Text("Não tem conta? ", color = Color.White.copy(alpha = 0.8f))
                    Text("Cadastre-se", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
