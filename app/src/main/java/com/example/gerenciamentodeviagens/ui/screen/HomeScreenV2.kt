package com.example.gerenciamentodeviagens.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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

    // Estados para as fotos
    var showPhotoDialog by remember { mutableStateOf(false) }

    // Launcher para abrir a Galeria (Permite ver a pasta Downloads)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Foto anexada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para abrir a Câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Foto capturada!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para permissão da Câmera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
    }

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

    BackHandler(enabled = drawerState.isClosed) { (context as? ComponentActivity)?.finish() }

    val purpleGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF673AB7), Color(0xFF512DA8))
    )

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Adicionar Foto") },
            text = { Text("Deseja tirar uma foto agora ou anexar um arquivo do seu computador?") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text("Tirar Foto") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Anexar Arquivo") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White, drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(purpleGradient), contentAlignment = Alignment.CenterStart) {
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
                NavigationDrawerItem(icon = { Icon(Icons.Default.AddCircleOutline, null) }, label = { Text("Nova Viagem") }, selected = false, onClick = { scope.launch { drawerState.close() }; onNovaViagem() }, modifier = Modifier.padding(horizontal = 12.dp))
                NavigationDrawerItem(icon = { Icon(Icons.Default.Map, null) }, label = { Text("Meus Roteiros") }, selected = false, onClick = { scope.launch { drawerState.close() }; onMinhasViagens() }, modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(modifier = Modifier.padding(24.dp), color = Color.LightGray.copy(alpha = 0.5f))
                NavigationDrawerItem(icon = { Icon(Icons.Default.Info, null) }, label = { Text("Sobre BoraViajar") }, selected = false, onClick = { scope.launch { drawerState.close() }; onSobre() }, modifier = Modifier.padding(horizontal = 12.dp))
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }, label = { Text("Sair", color = Color.Red) }, selected = false, onClick = { scope.launch { drawerState.close() }; onSair() }, modifier = Modifier.padding(12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "Bem-vinda, $nomeUsuario!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF673AB7)) },
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
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val viagemParaMostrar = viagensAtuais.firstOrNull()

                if (viagemParaMostrar != null) {
                    // 1. MAPA NO TOPO
                    Text("LOCALIZAÇÃO NO MAPA", modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                    Card(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE3F2FD))) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 1.5f
                                val street = Color.White
                                for (i in 1..10) {
                                    drawLine(street, Offset(size.width * (i * 0.1f), 0f), Offset(size.width * (i * 0.1f), size.height), strokeWidth = stroke)
                                    drawLine(street, Offset(0f, size.height * (i * 0.1f)), Offset(size.width, size.height * (i * 0.1f)), strokeWidth = stroke)
                                }
                            }
                            Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.size(32.dp).align(Alignment.Center).padding(bottom = 20.dp))
                            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xFF4285F4)).padding(8.dp)) {
                                Column {
                                    Text(text = cidadeAtual ?: "Bombinhas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Localização em tempo real", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. VIAGEM ABAIXO DO MAPA
                    Text("VIAGEM EM CURSO", modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF673AB7), letterSpacing = 1.sp)
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7)), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (viagemParaMostrar.tipo == "Lazer") Icons.Default.BeachAccess else Icons.Default.BusinessCenter, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(viagemParaMostrar.destino, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TripBlockV2("Partida", dateFormatter.format(Date(viagemParaMostrar.dataInicio)))
                                TripBlockV2("Retorno", dateFormatter.format(Date(viagemParaMostrar.dataFim)))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TripBlockV2("Orçamento", "R$ " + String.format(Locale.getDefault(), "%.2f", viagemParaMostrar.orcamento))
                                TripBlockV2("Gastos Atuais", "R$ " + String.format(Locale.getDefault(), "%.2f", viagemParaMostrar.totalGastos))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. BOTÃO DE FOTOS
                    Button(onClick = { showPhotoDialog = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("FOTOS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.White, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                            Icon(Icons.AutoMirrored.Filled.EventNote, null, modifier = Modifier.size(48.dp), tint = Color.LightGray.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tudo organizado!", style = MaterialTheme.typography.titleMedium, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Nenhum roteiro para hoje em\n${cidadeAtual ?: "sua região"}", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripBlockV2(label: String, value: String) {
    Column {
        Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
    }
}
