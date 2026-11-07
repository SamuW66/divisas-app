package com.example.divisas.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.divisas.ui.components.AppTopBar
import com.example.divisas.ui.components.TransactionItem

@Composable
fun ProfileScreen(onBack: () -> Unit, onLogout: () -> Unit, vm: ProfileViewModel = viewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.uploadPhoto(context, it) }
    }

    Scaffold(topBar = {
        AppTopBar("Mi perfil") {
            TextButton(onClick = onBack) { Text("Volver") }
            TextButton(onClick = onLogout) { Text("Salir") }
        }
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                val url = s.profile?.photoUrl.orEmpty()
                Box(Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center) {
                    if (url.isNotBlank()) {
                        AsyncImage(model = url, contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else Text("Sin foto")
                }
            }
            item {
                if (s.uploading) CircularProgressIndicator()
                else Button(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Cambiar foto (Cloudinary)") }
            }
            item {
                Text(s.profile?.name.orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(s.profile?.email.orEmpty())
                Text("Rol: ${s.profile?.role.orEmpty()}")
            }
            s.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            item {
                Text("Mis operaciones", fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth())
            }
            if (!s.loading && s.history.isEmpty()) item { Text("Aun no tienes operaciones") }
            items(s.history) { tx -> TransactionItem(tx) }
        }
    }
}
