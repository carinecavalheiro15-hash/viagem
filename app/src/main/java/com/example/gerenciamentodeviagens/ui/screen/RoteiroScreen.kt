package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciamentodeviagens.model.Viagem
import com.example.gerenciamentodeviagens.ui.viewmodel.RoteiroUiState
import com.example.gerenciamentodeviagens.ui.viewmodel.RoteiroViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoteiroScreen(
    viagemId: Int,
    viagem: Viagem?,
    viewModel: RoteiroViewModel,
    onVoltar: () -> Unit
) {
    // Estados para os campos solicitados
    var tipoViagem by remember { mutableStateOf("Lazer") }
    var epocaViagem by remember { mutableStateOf("") }
    var orcamentoPretendido by remember { mutableStateOf("") }
    var interesses by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("IA - Planejador de Roteiro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF673AB7),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viagem != null) {
                // Card de Resumo do Destino
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Destino: ${viagem.destino}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        val diff = viagem.dataFim - viagem.dataInicio
                        val dias = TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(1)
                        Text("Duração estimada: $dias dias", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo: Objetivo da Viagem
                Text("Qual o objetivo da viagem?", fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Start))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipoViagem == "Lazer", onClick = { tipoViagem = "Lazer" })
                    Text("Lazer")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = tipoViagem == "Trabalho", onClick = { tipoViagem = "Trabalho" })
                    Text("Trabalho")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo: Época
                OutlinedTextField(
                    value = epocaViagem,
                    onValueChange = { epocaViagem = it },
                    label = { Text("Época (ex: Inverno, Dezembro, Feriado)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo: Orçamento
                OutlinedTextField(
                    value = orcamentoPretendido,
                    onValueChange = { orcamentoPretendido = it },
                    label = { Text("Quanto pretende gastar? (ex: Econômico, R$ 300/dia)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo: Interesses
                OutlinedTextField(
                    value = interesses,
                    onValueChange = { interesses = it },
                    label = { Text("O que você gosta? (ex: Museus, Gastronomia, Trilhas)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botão para gerar roteiro enviando todos os campos para a IA
                Button(
                    onClick = {
                        val diff = viagem.dataFim - viagem.dataInicio
                        val dias = TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(1)
                        viewModel.gerarRoteiro(
                            destino = viagem.destino,
                            dias = dias,
                            tipoViagem = tipoViagem,
                            epoca = epocaViagem,
                            orcamentoPretendido = orcamentoPretendido,
                            interesses = interesses
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                    enabled = uiState !is RoteiroUiState.Loading
                ) {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("GERAR ROTEIRO COM IA", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Exibição do Resultado gerado pelo Gemini
                when (val state = uiState) {
                    is RoteiroUiState.Loading -> {
                        CircularProgressIndicator(color = Color(0xFF673AB7))
                        Text("A IA está criando seu roteiro perfeito...", modifier = Modifier.padding(top = 8.dp))
                    }
                    is RoteiroUiState.Sucesso -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = state.roteiro,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        }
                    }
                    is RoteiroUiState.Erro -> {
                        Text(state.mensagem, color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    else -> {}
                }
            } else {
                Text("Carregando detalhes da viagem...")
            }
        }
    }
}
