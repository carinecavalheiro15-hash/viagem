package com.example.gerenciamentodeviagens.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.gerenciamentodeviagens.ui.screen.*
import com.example.gerenciamentodeviagens.viewmodel.LoginViewModel
import com.example.gerenciamentodeviagens.viewmodel.LoginViewModelFactory
import com.example.gerenciamentodeviagens.viewmodel.RegistroViewModelFactory
import com.example.gerenciamentodeviagens.ui.viewmodel.RegistroViewModel
import com.example.gerenciamentodeviagens.ui.viewmodel.ViagemViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.gerenciamentodeviagens.data.AppDatabase
import com.example.gerenciamentodeviagens.data.repository.UsuarioRepository
import com.example.gerenciamentodeviagens.data.repository.ViagemRepository
import com.example.gerenciamentodeviagens.model.Usuario

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    
    val usuarioRepository = UsuarioRepository(db.userDao())
    val viagemRepository = ViagemRepository(db.viagemDao())

    var usuarioLogado by remember { mutableStateOf<Usuario?>(null) }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val loginVM: LoginViewModel = viewModel(factory = LoginViewModelFactory(usuarioRepository))

            // Retornando ao layout original
            LoginScreen(
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
            val registroVM: RegistroViewModel = viewModel(factory = RegistroViewModelFactory(usuarioRepository))

            // Retornando ao layout original
            RegistroScreen(
                viewModel = registroVM,
                onRegistroSucesso = { navController.popBackStack() }
            )
        }

        composable("home") {
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            
            // Retornando ao layout original (com lógica de localização inclusa)
            HomeScreen(
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
                onSobre = { navController.navigate("sobre") }
            )
        }

        composable("nova_viagem") {
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            
            // Retornando ao layout original
            NovaViagemScreen(
                viewModel = viagemVM,
                userId = usuarioLogado?.id ?: 0,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("minhas_viagens") {
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            
            // Retornando ao layout original
            MinhasViagensScreen(
                viewModel = viagemVM,
                userId = usuarioLogado?.id ?: 0,
                onVoltar = { navController.popBackStack() },
                onEditarViagem = { viagemId ->
                    navController.navigate("editar_viagem/$viagemId")
                }
            )
        }
        
        composable(
            route = "editar_viagem/{viagemId}",
            arguments = listOf(navArgument("viagemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val viagemId = backStackEntry.arguments?.getInt("viagemId") ?: 0
            val viagemVM: ViagemViewModel = viewModel(factory = ViagemViewModel.Factory(viagemRepository))
            
            // Retornando ao layout original para edição
            NovaViagemScreen(
                viewModel = viagemVM,
                userId = usuarioLogado?.id ?: 0,
                viagemId = viagemId,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("sobre") {
            SobreScreen(onVoltar = { navController.popBackStack() })
        }
        
        composable("esqueci") {
            EsqueciSenhaScreen(onVoltar = { navController.popBackStack() })
        }
    }
}
