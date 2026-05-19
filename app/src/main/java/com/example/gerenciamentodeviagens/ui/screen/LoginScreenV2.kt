package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF673AB7), Color(0xFF311B92))
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
                // ILUSTRAÇÃO DE VIAGEM EM COMPOSE
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "BoraViajar",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Organize seu próximo destino",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF673AB7)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = senha,
                            onValueChange = { senha = it },
                            label = { Text("Senha") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF673AB7)) },
                            visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                                    Icon(if (mostrarSenha) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.efetuarLogin(email, senha) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                        ) {
                            Text("ENTRAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        TextButton(onClick = onEsqueciSenha) {
                            Text("Esqueceu a senha?", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onIrParaRegistro) {
                    Row {
                        Text("Novo por aqui? ", color = Color.White.copy(alpha = 0.8f))
                        Text("Crie sua conta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
