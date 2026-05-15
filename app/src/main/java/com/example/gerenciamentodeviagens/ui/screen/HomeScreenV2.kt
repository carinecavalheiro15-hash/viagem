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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciamentodeviagens.ui.viewmodel.ViagemViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenV2(
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
    
    // Observa a lista de viagens encontradas
    val viagensAtuais by viewModel.viagensAtuais.collectAsState()
    val cidadeAtual by viewModel.cidadeAtual.collectAsState()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Monitoramento da Localização em Tempo Real (Updates Contínuos)
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
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    BackHandler(enabled = drawerState.isClosed) {
        (context as? ComponentActivity)?.finish()
    }

    val purpleGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF6200EE), Color(0xFF3700B3))
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp).background(purpleGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp)) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(nomeUsuario, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AddCircle, null) },
                    label = { Text("Nova Viagem") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNovaViagem() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Luggage, null) },
                    label = { Text("Minhas Viagens") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onMinhasViagens() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text("Sobre o App") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onSobre() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    label = { Text("Sair da Conta", color = Color.Red) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onSair() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("TRAVEL MANAGER", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 2.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF6200EE))
                )
            },
            containerColor = Color(0xFFF5F5F5)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Olá, $nomeUsuario!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                
                Card(
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFF6200EE).copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.padding(8.dp), tint = Color(0xFF6200EE))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sua localização", fontSize = 12.sp, color = Color.Gray)
                            Text(cidadeAtual ?: "Buscando...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                if (viagensAtuais.isNotEmpty()) {
                    Text(
                        "VIAGENS EM CURSO NESTA REGIÃO",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    viagensAtuais.forEach { viagem ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (viagem.tipo == "Lazer") Icons.Default.BeachAccess else Icons.Default.BusinessCenter,
                                        null, tint = Color.White, modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(viagem.destino, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    InfoColumnV2("Início", dateFormatter.format(Date(viagem.dataInicio)))
                                    InfoColumnV2("Fim", dateFormatter.format(Date(viagem.dataFim)))
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    InfoColumnV2("Orçamento", "R$ ${String.format(Locale.getDefault(), "%.2f", viagem.orcamento)}")
                                    InfoColumnV2("Gastos", "R$ ${String.format(Locale.getDefault(), "%.2f", viagem.totalGastos)}")
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.White, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Nenhuma viagem ativa para\n${cidadeAtual ?: "esta localização"}", textAlign = TextAlign.Center, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoColumnV2(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
