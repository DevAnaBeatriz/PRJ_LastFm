package com.example.lastfm.ui.theme.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.example.lastfm.data.Scrobble
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EditScrobble(navController: NavController, scrobbleId: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var title by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    val userId = auth.currentUser?.uid ?: ""
    var genero by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }

    LaunchedEffect(scrobbleId) {
        db.collection("scrobbles").document(scrobbleId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val scrobble = document.toObject<Scrobble>()
                    scrobble?.let {
                        title = it.title
                        user = it.user
                        genero = it.genero
                        info = it.info
                    }
                } else {
                    Log.d("EditScrobbleScreen", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("EditScrobbleScreen", "Error getting document: ", exception)
            }
    }

    fun saveUpdatedScrobbleToFirestore(
        updatedScrobble: Scrobble,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("scrobbles").document(scrobbleId)
            .set(updatedScrobble)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Editar Scrobble",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF9E0505),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da Scrobble") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = genero,
                onValueChange = { genero = it },
                label = { Text("Gênero") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = info,
                onValueChange = { info = it },
                label = { Text("Descrição") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(100.dp),
                singleLine = false
            )

            Button(
                onClick = {
                    if (title.isBlank() || genero.isBlank() || info.isBlank()) {
                        Toast.makeText(navController.context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updatedScrobble = Scrobble(
                        id = scrobbleId,
                        title = title,
                        user = user,
                        userId = userId,
                        genero = genero,
                        info = info
                    )

                    saveUpdatedScrobbleToFirestore(
                        updatedScrobble = updatedScrobble,
                        onSuccess = {
                            Toast.makeText(navController.context, "Scrobble atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onFailure = { exception ->
                            Toast.makeText(navController.context, "Erro ao salvar: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E0505),
                    contentColor = Color.White
                ),
            ) {
                Text("Salvar Scrobble")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = "Voltar para a lista de Scrobbles",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

