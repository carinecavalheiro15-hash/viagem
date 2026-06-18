package com.example.gerenciamentodeviagens.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
            val registroVM: RegistroViewModel = viewModel(factory = RegistroViewModelFactory(usuarioRepository))
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

        // --- ROTAS PARA FOTOS E ROTEIRO ---
        composable(
            route = "fotos/{viagemId}",
            arguments = listOf(navArgument("viagemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("viagemId")
            // Placeholder temporário para não dar erro de compilação
            Text("Tela de Fotos da Viagem ID: $id (Em desenvolvimento)")
        }

        composable(
            route = "roteiro/{viagemId}",
            arguments = listOf(navArgument("viagemId") { type = NavType.IntType })
        ) {
            Text("Tela de Roteiro em desenvolvimento")
        }

        composable("sobre") {
            SobreScreen(onVoltar = { navController.popBackStack() })
        }

        composable("esqueci") {
            EsqueciSenhaScreen(onVoltar = { navController.popBackStack() })
        }
    }
}