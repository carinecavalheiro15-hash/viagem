package com.example.gerenciamentodeviagens.ui.screen

// ==================== IMPORTAÇÕES ====================

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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

/*
 * Informa que esta tela utiliza recursos experimentais do Material3.
 */
@OptIn(ExperimentalMaterial3Api::class)

/*
 * Tela principal responsável por listar as viagens do usuário.
 */
@Composable
fun MinhasViagensScreenV2(
    viewModel: ViagemViewModel,
    userId: Int,
    onVoltar: () -> Unit,
    onEditarViagem: (Int) -> Unit
) {

    // Observa continuamente a lista de viagens armazenada no ViewModel.
    val viagens by viewModel.viagens.collectAsState(initial = emptyList())

    // Guarda temporariamente a viagem que será excluída.
    var viagemParaExcluir by remember { mutableStateOf<Viagem?>(null) }

    /*
     * Executa apenas quando o userId mudar.
     * Define no ViewModel qual usuário terá suas viagens carregadas.
     */
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    /*
     * Scaffold organiza a estrutura principal da tela,
     * contendo TopBar e conteúdo principal.
     */
    Scaffold(

        topBar = {
            TopAppBar(

                title = {
                    Text(
                        "Minhas Aventuras",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Voltar",
                            tint = Color.White
                        )
                    }
                },

                // Cor da barra superior
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF673AB7)
                )
            )
        },

        // Cor de fundo da tela
        containerColor = Color(0xFFF5F5F5)

    ) { paddingValues ->


        if (viagens.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Icon(
                        Icons.Default.ExploreOff,
                        null,
                        Modifier.size(64.dp),
                        tint = Color.LightGray
                    )

                    Text(
                        "Nenhuma viagem planejada ainda.",
                        color = Color.Gray
                    )
                }
            }

        } else {


            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),

                contentPadding = PaddingValues(20.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {

                /*
                 * Percorre toda a lista de viagens.
                 * Para cada item cria um cartão.
                 */
                items(viagens) { viagem ->

                    ViagemCardV2(

                        viagem = viagem,

                        // Abre tela de edição
                        onEditClick = {
                            onEditarViagem(viagem.id)
                        },

                        // Define qual viagem será removida
                        onDeleteClick = {
                            viagemParaExcluir = viagem
                        }
                    )
                }
            }
        }

        /*
         * Exibe caixa de confirmação antes da exclusão.
         */
        if (viagemParaExcluir != null) {

            AlertDialog(

                // Fecha a janela sem excluir
                onDismissRequest = {
                    viagemParaExcluir = null
                },

                // Título
                title = {
                    Text("Excluir Plano?")
                },

                // Mensagem
                text = {
                    Text(
                        "Você realmente deseja remover sua viagem para ${viagemParaExcluir?.destino}?"
                    )
                },

                // Botão confirmar
                confirmButton = {

                    Button(

                        onClick = {

                            // Remove a viagem
                            viagemParaExcluir?.let {
                                viewModel.excluirViagem(it)
                            }

                            // Fecha o diálogo
                            viagemParaExcluir = null
                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )

                    ) {
                        Text("Excluir")
                    }
                },

                // Botão cancelar
                dismissButton = {

                    TextButton(
                        onClick = {
                            viagemParaExcluir = null
                        }
                    ) {
                        Text("Manter")
                    }
                },

                // Bordas arredondadas da caixa
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

/*
 * Utiliza recursos experimentais do Foundation.
 */
@OptIn(ExperimentalFoundationApi::class)

/*
 * Componente responsável por desenhar um cartão de viagem.
 */
@Composable
fun ViagemCardV2(

    viagem: Viagem,

    onEditClick: () -> Unit,

    onDeleteClick: () -> Unit

) {

    /*
     * Formata as datas para o padrão dia/mês.
     */
    val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())

    /*
     * Cartão principal.
     */
    Card(

        modifier = Modifier

            .fillMaxWidth()

            // Permite editar clicando no cartão inteiro
            .clickable {
                onEditClick()
            },

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )

    ) {

        /*
         * Organiza os elementos horizontalmente.
         */
        Row(

            modifier = Modifier.padding(16.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            /*
             * Área circular contendo o ícone da viagem.
             */
            Surface(

                shape = CircleShape,

                color =
                    if (viagem.tipo == "Lazer")
                        Color(0xFFFFEB3B).copy(alpha = 0.2f)
                    else
                        Color(0xFF2196F3).copy(alpha = 0.2f),

                modifier = Modifier.size(56.dp)

            ) {

                /*
                 * Escolhe automaticamente o ícone conforme
                 * o tipo da viagem.
                 */
                Icon(

                    imageVector =
                        if (viagem.tipo == "Lazer")
                            Icons.Default.BeachAccess
                        else
                            Icons.Default.BusinessCenter,

                    contentDescription = null,

                    modifier = Modifier.padding(12.dp),

                    tint =
                        if (viagem.tipo == "Lazer")
                            Color(0xFFFBC02D)
                        else
                            Color(0xFF1976D2)
                )
            }

            // Espaçamento entre ícone e informações
            Spacer(modifier = Modifier.width(16.dp))

            /*
             * Coluna contendo todas as informações da viagem.
             */
            Column(modifier = Modifier.weight(1f)) {

                // Destino
                Text(
                    viagem.destino,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                // Período da viagem
                Text(
                    "${dateFormatter.format(Date(viagem.dataInicio))} - ${dateFormatter.format(Date(viagem.dataFim))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Valor do orçamento
                Text(
                    "R$ ${
                        String.format(
                            Locale.getDefault(),
                            "%.2f",
                            viagem.orcamento
                        )
                    }",

                    fontWeight = FontWeight.ExtraBold,

                    color = Color(0xFF4CAF50),

                    fontSize = 14.sp
                )
            }

            /*
             * Botões de ação.
             */
            Row {

                // Botão editar
                IconButton(onClick = onEditClick) {

                    Icon(
                        Icons.Default.Edit,
                        "Editar",
                        tint = Color(0xFF673AB7)
                    )
                }

                // Botão excluir
                IconButton(onClick = onDeleteClick) {

                    Icon(
                        Icons.Default.DeleteOutline,
                        "Remover",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}