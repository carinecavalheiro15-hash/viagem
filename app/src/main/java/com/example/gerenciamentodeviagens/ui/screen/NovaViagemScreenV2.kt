package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciamentodeviagens.model.Viagem
import com.example.gerenciamentodeviagens.ui.viewmodel.ViagemViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaViagemScreenV2(
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

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF6200EE), Color(0xFF3700B3))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viagemId == 0) "Nova Viagem" else "Editar Viagem", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6200EE))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = destino,
                        onValueChange = { destino = it },
                        label = { Text("Para onde você vai?") },
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = Color(0xFF6200EE)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Tipo de Viagem", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = tipo == "Lazer",
                            onClick = { tipo = "Lazer" },
                            label = { Text("🏖️ Lazer") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = tipo == "Negócios",
                            onClick = { tipo = "Negócios" },
                            label = { Text("💼 Negócios") }
                        )
                    }

                    OutlinedButton(
                        onClick = { showDatePickerInicio = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Início: ${dateFormatter.format(Date(dataInicio))}")
                    }

                    OutlinedButton(
                        onClick = { showDatePickerFim = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Retorno: ${dateFormatter.format(Date(dataFim))}")
                    }

                    OutlinedTextField(
                        value = orcamento,
                        onValueChange = { orcamento = it },
                        label = { Text("Orçamento Estimado") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("CONFIRMAR VIAGEM", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            ) { DatePicker(state = datePickerState) }
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
            ) { DatePicker(state = datePickerState) }
        }
    }
}
