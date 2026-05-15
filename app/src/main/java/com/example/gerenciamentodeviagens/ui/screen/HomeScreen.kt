package com.example.gerenciamentodeviagens.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gerenciamentodeviagens.ui.viewmodel.ViagemViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nomeUsuario: String,
    userId: Int,
    viewModel: ViagemViewModel,
    onSair: () -> Unit,
    onNovaViagem: () -> Unit,
    onMinhasViagens: () -> Unit,
    onSobre: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val viagensAtuais by viewModel.viagensAtuais.collectAsState()
    val cidadeAtual by viewModel.cidadeAtual.collectAsState()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(context, userId) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    scope.launch {
                        val cidade = withContext(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                addresses?.getOrNull(0)?.let { addr ->
                                    addr.locality ?: addr.subAdminArea ?: addr.adminArea
                                }
                            } catch (e: Exception) { null }
                        }
                        cidade?.let { viewModel.buscarViagensPelaCidade(userId, it) }
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) { e.printStackTrace() }

        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    BackHandler(enabled = drawerState.isClosed) {
        (context as? ComponentActivity)?.finish()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(icon = { Icon(Icons.Default.Add, null) }, label = { Text("Nova Viagem") }, selected = false, onClick = { scope.launch { drawerState.close() }; onNovaViagem() })
                NavigationDrawerItem(icon = { Icon(Icons.Default.CardTravel, null) }, label = { Text("Minhas Viagens") }, selected = false, onClick = { scope.launch { drawerState.close() }; onMinhasViagens() })
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(icon = { Icon(Icons.Default.Info, null) }, label = { Text("Sobre") }, selected = false, onClick = { scope.launch { drawerState.close() }; onSobre() })
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }, label = { Text("Sair") }, selected = false, onClick = { scope.launch { drawerState.close() }; onSair() })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Gerenciador de Viagens") }, navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Menu") } })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bem-vinda, $nomeUsuario!", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Localização: ${cidadeAtual ?: "Buscando..."}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (viagensAtuais.isNotEmpty()) {
                    Text("Viagens em Andamento nesta região:", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), style = MaterialTheme.typography.titleMedium)
                    viagensAtuais.forEach { viagem ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📍 Destino: ${viagem.destino}", fontWeight = FontWeight.Bold)
                                Text("🏖️ Tipo: ${viagem.tipo}")
                                Text("📅 Período: ${dateFormatter.format(Date(viagem.dataInicio))} - ${dateFormatter.format(Date(viagem.dataFim))}")
                                Text("💰 Orçamento: R$ ${String.format(Locale.getDefault(), "%.2f", viagem.orcamento)}")
                                Text("📊 Total de Gastos: R$ ${String.format(Locale.getDefault(), "%.2f", viagem.totalGastos)}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text(text = if (cidadeAtual == null) "Aguardando sinal do GPS..." else "Nenhuma viagem em andamento para $cidadeAtual nesta data.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 20.dp))
                }
            }
        }
    }
}
