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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    val headerGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF673AB7), Color(0xFF512DA8))
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp).background(headerGradient),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(60.dp)) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(12.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Bem-vinda,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text(nomeUsuario, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AddCircleOutline, null) },
                    label = { Text("Nova Viagem") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNovaViagem() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Map, null) },
                    label = { Text("Minhas Viagens") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onMinhasViagens() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                HorizontalDivider(modifier = Modifier.padding(24.dp), color = Color.LightGray.copy(alpha = 0.5f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text("Sobre BoraViajar") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onSobre() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    label = { Text("Sair", color = Color.Red) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onSair() },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("BoraViajar", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, fontSize = 20.sp) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF8F9FA)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card de Localização
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFF673AB7).copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                            Icon(Icons.Default.MyLocation, null, modifier = Modifier.padding(10.dp), tint = Color(0xFF673AB7))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Sua localização atual", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text(cidadeAtual ?: "Buscando GPS...", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF333333))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Lógica de exibir apenas 1 unidade de viagem
                val viagemParaMostrar = viagensAtuais.firstOrNull()

                if (viagemParaMostrar != null) {
                    Text(
                        "VIAGEM EM CURSO",
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF673AB7),
                        letterSpacing = 1.sp
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (viagemParaMostrar.tipo == "Lazer") Icons.Default.BeachAccess else Icons.Default.Work,
                                    null, tint = Color.White, modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(viagemParaMostrar.destino, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                InfoBlock("Partida", dateFormatter.format(Date(viagemParaMostrar.dataInicio)))
                                InfoBlock("Retorno", dateFormatter.format(Date(viagemParaMostrar.dataFim)))
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                // Exibindo o Orçamento cadastrado corretamente
                                InfoBlock("Orçamento", "R$ " + String.format(Locale.getDefault(), "%.2f", viagemParaMostrar.orcamento))
                                InfoBlock("Gastos Atuais", "R$ " + String.format(Locale.getDefault(), "%.2f", viagemParaMostrar.totalGastos))
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(240.dp).background(Color.White, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.EventNote, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Tudo organizado!\nNenhum roteiro para hoje em\n${cidadeAtual ?: "sua região"}",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBlock(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
    }
}
