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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
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

    // Estados para o Mapa Real
    var currentLatLng by remember { mutableStateOf(LatLng(-26.9166, -49.0717)) } // Padrão Blumenau
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }

    var showPhotoDialog by remember { mutableStateOf(false) }

    // Launchers de Câmera/Galeria
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) Toast.makeText(context, "Foto anexada!", Toast.LENGTH_SHORT).show()
    }
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

    // Lógica de localização atualizada para atualizar o Google Maps
    DisposableEffect(context, userId) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    currentLatLng = newLatLng

                    // Move a câmera para a nova posição com proteção contra NPE
                    scope.launch {
                        try {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(newLatLng))
                        } catch (e: Exception) {
                            // Caso o Maps ainda não tenha inicializado o CameraUpdateFactory
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(newLatLng, 15f)
                        }
                    }

                    scope.launch {
                        val cidadeEncontrada = withContext(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val addr = addresses?.getOrNull(0)
                                addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
                            } catch (e: Exception) { null }
                        }
                        cidadeEncontrada?.let {
                            val cidadeBusca = it.split("-")[0].split(",")[0].split(" ")[0].trim()
                            viewModel.buscarViagensPelaCidade(userId, cidadeBusca)
                        }
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
                        NavigationBarItem(icon = { Icon(Icons.Default.ListAlt, null) }, label = { Text("Roteiro") }, selected = false, onClick = { onVerRoteiro(viagem.id) })
                        NavigationBarItem(icon = { Icon(Icons.Default.PhotoLibrary, null) }, label = { Text("Fotos") }, selected = false, onClick = { onVerFotos(viagem.id) })
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
                    // --- MAPA GOOGLE REAL ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column {
                            Box(modifier = Modifier.weight(1f)) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                                ) {
                                    Marker(
                                        state = MarkerState(position = currentLatLng),
                                        title = "Você está aqui"
                                    )
                                }
                            }
                            // Barra Azul com o Nome da Cidade
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF6495ED))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cidadeAtual ?: "Localizando...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card da Viagem Ativa (Roxo) com todas as informações requeridas
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(viagem.destino, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Tipo: ${viagem.tipo}", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Partida", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text(dateFormatter.format(Date(viagem.dataInicio)), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column {
                                    Text("Retorno", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text(dateFormatter.format(Date(viagem.dataFim)), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Orçamento", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text("R$ ${String.format(Locale.getDefault(), "%.2f", viagem.orcamento)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column {
                                    Text("Total de Gastos", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text("R$ ${String.format(Locale.getDefault(), "%.2f", viagem.totalGastos)}", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showPhotoDialog = true },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(horizontal = 24.dp)
                            ) {
                                Text("Adicionar Foto", color = Color.White)
                            }
                        }
                    }
                } else {
                    // ESTADO VAZIO (Prancheta)
                    Spacer(Modifier.height(40.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
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
