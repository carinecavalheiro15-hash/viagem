package com.example.gerenciamentodeviagens.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun MinhasViagensScreenV2(
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
                title = { Text("Minhas Aventuras", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6200EE))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        if (viagens.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ExploreOff, null, size(64.dp), tint = Color.LightGray)
                    Text("Nenhuma viagem planejada ainda.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viagens) { viagem ->
                    ViagemCardV2(
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
                title = { Text("Excluir Plano?") },
                text = { Text("Você realmente deseja remover sua viagem para ${viagemParaExcluir?.destino}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viagemParaExcluir?.let { viewModel.excluirViagem(it) }
                            viagemParaExcluir = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { viagemParaExcluir = null }) { Text("Manter") }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

private fun size(dp: androidx.compose.ui.unit.Dp) = Modifier.size(dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViagemCardV2(
    viagem: Viagem,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (viagem.tipo == "Lazer") Color(0xFFFFEB3B).copy(alpha = 0.2f) else Color(0xFF2196F3).copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (viagem.tipo == "Lazer") Icons.Default.BeachAccess else Icons.Default.BusinessCenter,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = if (viagem.tipo == "Lazer") Color(0xFFFBC02D) else Color(0xFF1976D2)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(viagem.destino, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(
                    "${dateFormatter.format(Date(viagem.dataInicio))} - ${dateFormatter.format(Date(viagem.dataFim))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "R$ ${String.format(Locale.getDefault(), "%.2f", viagem.orcamento)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.DeleteOutline, "Remover", tint = Color.LightGray)
            }
        }
    }
}
