package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.gerenciamentodeviagens.model.Viagem
import com.example.gerenciamentodeviagens.ui.viewmodel.ViagemViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinhasViagensScreen(
    viewModel: ViagemViewModel,
    userId: Int,
    onVoltar: () -> Unit,
    onEditarViagem: (Int) -> Unit
) {
    val viagens by viewModel.viagens.collectAsState(initial = emptyList())
    var viagemParaExcluir by remember { mutableStateOf<Viagem?>(null) }

    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Viagens") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (viagens.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nenhuma viagem cadastrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viagens) { viagem ->
                    ViagemCard(
                        viagem = viagem,
                        onLongClick = { onEditarViagem(viagem.id) },
                        onDeleteClick = { viagemParaExcluir = viagem }
                    )
                }
            }
        }

        if (viagemParaExcluir != null) {
            AlertDialog(
                onDismissRequest = { viagemParaExcluir = null },
                title = { Text("Excluir Viagem") },
                text = { Text("Tem certeza que deseja excluir a viagem para ${viagemParaExcluir?.destino}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viagemParaExcluir?.let { viewModel.excluirViagem(it) }
                        viagemParaExcluir = null
                    }) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { viagemParaExcluir = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViagemCard(
    viagem: Viagem,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (viagem.tipo == "Lazer") Icons.Default.BeachAccess else Icons.Default.BusinessCenter,
                contentDescription = viagem.tipo,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(viagem.destino, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${dateFormatter.format(Date(viagem.dataInicio))} - ${dateFormatter.format(Date(viagem.dataFim))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("Orçamento: R$ ${String.format("%.2f", viagem.orcamento)}", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
