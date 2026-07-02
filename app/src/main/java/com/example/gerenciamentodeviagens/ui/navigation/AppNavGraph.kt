package com.example.gerenciamentodeviagens.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gerenciamentodeviagens.data.AppDatabase
import com.example.gerenciamentodeviagens.data.repository.FotoRepository
import com.example.gerenciamentodeviagens.data.repository.GeminiRepository
import com.example.gerenciamentodeviagens.data.repository.UsuarioRepository
import com.example.gerenciamentodeviagens.data.repository.ViagemRepository
import com.example.gerenciamentodeviagens.model.Usuario
import com.example.gerenciamentodeviagens.ui.screen.*
import com.example.gerenciamentodeviagens.ui.viewmodel.FotoViewModel
import com.example.gerenciamentodeviagens.ui.viewmodel.RoteiroViewModel
import com.example.gerenciamentodeviagens.ui.viewmodel.ViagemViewModel
import com.example.gerenciamentodeviagens.viewmodel.LoginViewModel
import com.example.gerenciamentodeviagens.viewmodel.LoginViewModelFactory
import com.example.gerenciamentodeviagens.viewmodel.RegistroViewModelFactory
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val usuarioRepository = UsuarioRepository(db.userDao())
    val viagemRepository = ViagemRepository(db.viagemDao())
    val fotoRepository = FotoRepository(db.fotoDao())
    val geminiRepository = GeminiRepository()

    var usuarioLogado by remember { mutableStateOf<Usuario?>(null) }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val loginVM: LoginViewModel = viewModel(factory = LoginViewModelFactory(usuarioRepository))
            LoginScreenV2(
                viewModel = loginVM,
                onLoginSucesso = { usuario ->
                    usuarioLogado = usuario
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIrParaRegistro = { navController.navigate("registro") },
                onEsqueciSenha = { navController.navigate("esqueci") }
            )
        }

        composable("registro") {
            val registroVM: com.example.gerenciamentodeviagens.ui.viewmodel.RegistroViewModel = viewModel(factory = RegistroViewModelFactory(usuarioRepository))
            RegistroScreenV2(
                viewModel = registroVM,
                onRegistroSucesso = { navController.popBackStack() },
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("home") {
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))

            HomeScreenV2(
                nomeUsuario = usuarioLogado?.nome ?: "Usuário",
                userId = usuarioLogado?.id ?: 0,
                viewModel = viagemVM,
                onSair = {
                    usuarioLogado = null
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNovaViagem = { navController.navigate("nova_viagem") },
                onMinhasViagens = { navController.navigate("minhas_viagens") },
                onSobre = { navController.navigate("sobre") },
                onVerFotos = { viagemId ->
                    navController.navigate("fotos/$viagemId")
                },
                onVerRoteiro = { viagemId ->
                    navController.navigate("roteiro/$viagemId")
                }
            )
        }

        composable("nova_viagem") {
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            NovaViagemScreenV2(
                viewModel = viagemVM,
                userId = usuarioLogado?.id ?: 0,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("minhas_viagens") {
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            MinhasViagensScreenV2(
                viewModel = viagemVM,
                userId = usuarioLogado?.id ?: 0,
                onVoltar = { navController.popBackStack() },
                onEditarViagem = { viagemId ->
                    navController.navigate("editar_viagem/$viagemId")
                }
            )
        }

        composable(
            route = "fotos/{viagemId}",
            arguments = listOf(navArgument("viagemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("viagemId") ?: 0
            val fotoVM: FotoViewModel = viewModel(factory = FotoViewModel.Factory(fotoRepository))
            FotosScreen(
                viagemId = id,
                viewModel = fotoVM,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable(
            route = "roteiro/{viagemId}",
            arguments = listOf(navArgument("viagemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("viagemId") ?: 0
            val roteiroVM: RoteiroViewModel = viewModel(factory = RoteiroViewModel.Factory(geminiRepository))
            
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            LaunchedEffect(usuarioLogado) {
                usuarioLogado?.id?.let { viagemVM.setUserId(it) }
            }
            
            val viagens by viagemVM.viagens.collectAsState(initial = emptyList())
            val viagem = remember(viagens, id) { viagens.find { it.id == id } }

            RoteiroScreen(
                viagemId = id,
                viagem = viagem,
                viewModel = roteiroVM,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("sobre") {
            SobreScreen(onVoltar = { navController.popBackStack() })
        }

        composable("esqueci") {
            EsqueciSenhaScreen(onVoltar = { navController.popBackStack() })
        }
        
        // ROTA DE EDIÇÃO CORRIGIDA - Agora abre a tela real de edição
        composable(
            route = "editar_viagem/{viagemId}",
            arguments = listOf(navArgument("viagemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("viagemId") ?: 0
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            
            NovaViagemScreenV2(
                viewModel = viagemVM,
                userId = usuarioLogado?.id ?: 0,
                viagemId = id,
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}
