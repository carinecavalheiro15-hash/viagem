package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gerenciamentodeviagens.model.Viagem
import com.example.gerenciamentodeviagens.ui.viewmodel.ViagemViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaViagemScreen(
    viewModel: ViagemViewModel,
    userId: Int,
    viagemId: Int = 0,
    onVoltar: () -> Unit
) {
    var destino by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Lazer") }
    var dataInicio by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var dataFim by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var orcamento by remember { mutableStateOf("") }
    
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerFim by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viagemId == 0) "Nova Viagem" else "Editar Viagem") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = destino,
                onValueChange = { destino = it },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de Viagem", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = tipo == "Lazer", onClick = { tipo = "Lazer" })
                Text("Lazer")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = tipo == "Negócios", onClick = { tipo = "Negócios" })
                Text("Negócios")
            }

            OutlinedButton(
                onClick = { showDatePickerInicio = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Data Início: ${dateFormatter.format(Date(dataInicio))}")
            }

            OutlinedButton(
                onClick = { showDatePickerFim = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Data Fim: ${dateFormatter.format(Date(dataFim))}")
            }

            OutlinedTextField(
                value = orcamento,
                onValueChange = { orcamento = it },
                label = { Text("Orçamento") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("R$ ") }
            )

            Button(
                onClick = {
                    if (destino.isNotBlank() && orcamento.isNotBlank()) {
                        viewModel.salvarViagem(
                            Viagem(
                                id = viagemId,
                                destino = destino,
                                tipo = tipo,
                                dataInicio = dataInicio,
                                dataFim = dataFim,
                                orcamento = orcamento.toDoubleOrNull() ?: 0.0,
                                userId = userId
                            )
                        )
                        onVoltar()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Salvar Viagem")
            }
        }

        if (showDatePickerInicio) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dataInicio)
            DatePickerDialog(
                onDismissRequest = { showDatePickerInicio = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dataInicio = it }
                        showDatePickerInicio = false
                    }) { Text("OK") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showDatePickerFim) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dataFim)
            DatePickerDialog(
                onDismissRequest = { showDatePickerFim = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dataFim = it }
                        showDatePickerFim = false
                    }) { Text("OK") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
