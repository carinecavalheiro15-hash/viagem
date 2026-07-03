package com.example.gerenciamentodeviagens.ui.screen

// ==================== IMPORTAÇÕES ====================

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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

/*
 * Habilita recursos experimentais do Material3.
 */
@OptIn(ExperimentalMaterial3Api::class)

/*
 * Tela responsável por coletar informações do usuário
 * e solicitar à IA a geração de um roteiro personalizado.
 */
@Composable
fun RoteiroScreen(

    // Id da viagem selecionada
    viagemId: Int,

    // Objeto contendo os dados completos da viagem
    viagem: Viagem?,

    // ViewModel responsável pela comunicação com a IA
    viewModel: RoteiroViewModel,

    // Função utilizada para voltar à tela anterior
    onVoltar: () -> Unit

) {

    /*
     * Estados responsáveis por armazenar
     * os valores digitados pelo usuário.
     */

    // Tipo da viagem (Lazer ou Trabalho)
    var tipoViagem by remember { mutableStateOf("Lazer") }

    // Época da viagem
    var epocaViagem by remember { mutableStateOf("") }

    // Orçamento pretendido
    var orcamentoPretendido by remember { mutableStateOf("") }

    // Interesses do usuário
    var interesses by remember { mutableStateOf("") }

    /*
     * Observa continuamente o estado retornado pelo ViewModel.
     * Pode representar carregamento, sucesso ou erro.
     */
    val uiState by viewModel.uiState.collectAsState()

    /*
     * Estrutura principal da tela.
     */
    Scaffold(

        /*
         * Barra superior da aplicação.
         */
        topBar = {

            CenterAlignedTopAppBar(

                // Título
                title = {
                    Text(
                        "IA - Planejador de Roteiro",
                        fontWeight = FontWeight.Bold
                    )
                },

                // Botão voltar
                navigationIcon = {

                    IconButton(onClick = onVoltar) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Voltar"
                        )
                    }
                },

                // Personalização das cores da barra
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

            /*
             * Só exibe o formulário quando a viagem estiver carregada.
             */
            if (viagem != null) {

                /*
                 * Card com resumo das informações da viagem.
                 */
                Card(

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(16.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEDE7F6)
                    )

                ) {

                    Column(modifier = Modifier.padding(16.dp)) {

                        // Destino da viagem
                        Text(
                            "Destino: ${viagem.destino}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        /*
                         * Calcula automaticamente a quantidade
                         * de dias entre as datas.
                         */
                        val diff = viagem.dataFim - viagem.dataInicio

                        val dias = TimeUnit.MILLISECONDS
                            .toDays(diff)
                            .toInt()
                            .coerceAtLeast(1)

                        Text(
                            "Duração estimada: $dias dias",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                /*
                 * Campo para selecionar o objetivo da viagem.
                 */
                Text(
                    "Qual o objetivo da viagem?",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    // Opção Lazer
                    RadioButton(
                        selected = tipoViagem == "Lazer",
                        onClick = {
                            tipoViagem = "Lazer"
                        }
                    )

                    Text("Lazer")

                    Spacer(Modifier.width(16.dp))

                    // Opção Trabalho
                    RadioButton(
                        selected = tipoViagem == "Trabalho",
                        onClick = {
                            tipoViagem = "Trabalho"
                        }
                    )

                    Text("Trabalho")
                }

                Spacer(modifier = Modifier.height(8.dp))

                /*
                 * Campo para informar a época da viagem.
                 */
                OutlinedTextField(

                    value = epocaViagem,

                    onValueChange = {
                        epocaViagem = it
                    },

                    label = {
                        Text("Época (ex: Inverno, Dezembro, Feriado)")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                /*
                 * Campo para informar o orçamento desejado.
                 */
                OutlinedTextField(

                    value = orcamentoPretendido,

                    onValueChange = {
                        orcamentoPretendido = it
                    },

                    label = {
                        Text("Quanto pretende gastar? (ex: Econômico, R$ 300/dia)")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                /*
                 * Campo para informar os interesses do usuário.
                 */
                OutlinedTextField(

                    value = interesses,

                    onValueChange = {
                        interesses = it
                    },

                    label = {
                        Text("O que você gosta? (ex: Museus, Gastronomia, Trilhas)")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    minLines = 2,

                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                /*
                 * Botão responsável por solicitar
                 * à IA a geração do roteiro.
                 */
                Button(

                    onClick = {

                        /*
                         * Calcula novamente a duração da viagem
                         * para enviar junto ao prompt.
                         */
                        val diff = viagem.dataFim - viagem.dataInicio

                        val dias = TimeUnit.MILLISECONDS
                            .toDays(diff)
                            .toInt()
                            .coerceAtLeast(1)

                        /*
                         * Envia todas as informações ao ViewModel,
                         * que fará a chamada para a IA.
                         */
                        viewModel.gerarRoteiro(

                            destino = viagem.destino,

                            dias = dias,

                            tipoViagem = tipoViagem,

                            epoca = epocaViagem,

                            orcamentoPretendido = orcamentoPretendido,

                            interesses = interesses
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape = RoundedCornerShape(12.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF673AB7)
                    ),

                    // Desabilita enquanto a IA gera o roteiro
                    enabled = uiState !is RoteiroUiState.Loading

                ) {

                    Icon(Icons.Default.AutoAwesome, null)

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "GERAR ROTEIRO COM IA",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                /*
                 * Exibe o resultado conforme o estado retornado
                 * pelo ViewModel.
                 */
                when (val state = uiState) {

                    /*
                     * Enquanto a IA está processando.
                     */
                    is RoteiroUiState.Loading -> {

                        CircularProgressIndicator(
                            color = Color(0xFF673AB7)
                        )

                        Text(
                            "A IA está criando seu roteiro perfeito...",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    /*
                     * Exibe o roteiro gerado com sucesso.
                     */
                    is RoteiroUiState.Sucesso -> {

                        Card(

                            modifier = Modifier.fillMaxWidth(),

                            shape = RoundedCornerShape(16.dp),

                            elevation = CardDefaults.cardElevation(4.dp),

                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )

                        ) {

                            Text(

                                text = state.roteiro,

                                modifier = Modifier.padding(16.dp),

                                fontSize = 16.sp,

                                lineHeight = 24.sp
                            )
                        }
                    }

                    /*
                     * Exibe uma mensagem de erro caso
                     * a geração falhe.
                     */
                    is RoteiroUiState.Erro -> {

                        Text(
                            state.mensagem,
                            color = Color.Red,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    /*
                     * Estado inicial.
                     */
                    else -> {}
                }

            } else {

                /*
                 * Exibido enquanto os dados da viagem
                 * ainda estão sendo carregados.
                 */
                Text("Carregando detalhes da viagem...")
            }
        }
    }
}