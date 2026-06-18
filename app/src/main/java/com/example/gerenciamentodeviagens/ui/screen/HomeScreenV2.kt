package com.example.gerenciamentodeviagens.ui.screen

import android.Manifest
import android.location.Geocoder
import android.os.Looper
import android.widget.Toast
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
    onSobre: () -> Unit,
    onVerFotos: (Int) -> Unit,
    onVerRoteiro: (Int) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val viagensAtuais by viewModel.viagensAtuais.collectAsState()
    val cidadeAtual by viewModel.cidadeAtual.collectAsState()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var showPhotoDialog by remember { mutableStateOf(false) }

    // Launcher para Galeria
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) Toast.makeText(context, "Foto anexada!", Toast.LENGTH_SHORT).show()
    }

    // Launcher para Câmera
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) Toast.makeText(context, "Foto capturada!", Toast.LENGTH_SHORT).show()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Lógica de localização (GPS)
    DisposableEffect(context, userId) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    scope.launch {
                        val cidade = withContext(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                addresses?.getOrNull(0)?.locality
                            } catch (e: Exception) { null }
                        }
                        cidade?.let { viewModel.buscarViagensPelaCidade(userId, it) }
                    }
                }
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {}
        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    BackHandler(enabled = drawerState.isClosed) { (context as? ComponentActivity)?.finish() }

    val purpleGradient = Brush.verticalGradient(listOf(Color(0xFF673AB7), Color(0xFF512DA8)))

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Adicionar Foto") },
            text = { Text("Deseja tirar uma foto ou escolher um arquivo?") },
            confirmButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text("Câmera") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Galeria") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(purpleGradient), contentAlignment = Alignment.CenterStart) {
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
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AddCircleOutline, null) },
                    label = { Text("Nova Viagem") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNovaViagem() }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Map, null) },
                    label = { Text("Meus Roteiros") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onMinhasViagens() }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text("Sobre") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onSobre() }
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    label = { Text("Sair") },
                    selected = false,
                    onClick = { onSair() }
                )
            }
        }
    ) {
        val viagem = viagensAtuais.firstOrNull()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Bem-vinda, $nomeUsuario!", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7)) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null) } }
                )
            },
            bottomBar = {
                if (viagem != null) {
                    NavigationBar(containerColor = Color.White) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.ListAlt, null) },
                            label = { Text("Roteiro") },
                            selected = false,
                            onClick = { onVerRoteiro(viagem.id) }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.PhotoLibrary, null) },
                            label = { Text("Fotos") },
                            selected = false,
                            onClick = { onVerFotos(viagem.id) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())) {
                if (viagem != null) {
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 16.dp)) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE3F2FD))) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier
                                .align(Alignment.Center)
                                .size(40.dp))
                            Text(cidadeAtual ?: "Localizando...", modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color(0xFF4285F4).copy(alpha = 0.8f))
                                .padding(8.dp), color = Color.White, textAlign = TextAlign.Center)
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(viagem.destino, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TripBlockV2("Partida", dateFormatter.format(Date(viagem.dataInicio)))
                                TripBlockV2("Retorno", dateFormatter.format(Date(viagem.dataFim)))
                            }
                            Button(onClick = { showPhotoDialog = true }, modifier = Modifier.padding(top = 10.dp)) {
                                Text("Adicionar Foto")
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(40.dp))
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Assignment, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(Modifier.height(16.dp))
                            Text("Tudo organizado!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Nenhum roteiro para hoje em ${cidadeAtual ?: "sua cidade"}", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripBlockV2(label: String, date: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(date, color = Color.White, fontWeight = FontWeight.Bold)
    }
}