package com.example.gerenciamentodeviagens.ui.screen

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gerenciamentodeviagens.ui.viewmodel.FotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotosScreen(
    viagemId: Int,
    viewModel: FotoViewModel,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    val fotos by viewModel.fotos.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viagemId) {
        viewModel.setViagemId(viagemId)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.adicionarFoto(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Salvamento de foto da câmera em implementação", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Galeria de Fotos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Adicionar Foto")
            }
        }
    ) { padding ->
        if (fotos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhuma foto encontrada.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(fotos) { foto ->
                    Box(modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))) {
                        AsyncImage(
                            model = foto.caminho,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.excluirFoto(foto) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    RoundedCornerShape(bottomStart = 8.dp)
                                )
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nova Foto") },
                text = { Text("Como deseja adicionar a foto?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) { Text("Câmera") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        galleryLauncher.launch("image/*")
                    }) { Text("Galeria") }
                }
            )
        }
    }
}
