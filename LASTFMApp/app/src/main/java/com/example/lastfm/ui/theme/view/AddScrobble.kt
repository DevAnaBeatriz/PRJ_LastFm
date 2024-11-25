package com.example.lastfm.ui.theme.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lastfm.data.Scrobble
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddScrobble(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("Carregando...") }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: "Unknown User"

    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                userName = document.getString("nome") ?: "Usuário Desconhecido"
            }
            .addOnFailureListener {
                userName = "Erro ao carregar nome"
            }
    }

    fun savescrobbleToFirestore(
        scrobble: Scrobble,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("scrobbles")
            .add(scrobble)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                    .addOnSuccessListener {
                        onSuccess(documentReference.id)
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(20.dp)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Adicionar Scrobble",
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
                    .height(100.dp) // Altura reduzida
                    .verticalScroll(rememberScrollState()),
                singleLine = false
            )

            Button(
                onClick = {
                    if (title.isBlank() || genero.isBlank() || info.isBlank()) {
                        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    val newscrobble = Scrobble(
                        title = title,
                        userId = userId,
                        user = userName,
                        genero = genero,
                        info = info
                    )

                    savescrobbleToFirestore(
                        scrobble = newscrobble,
                        onSuccess = { documentId ->
                            Toast.makeText(context, "Scrobble adicionada com sucesso!", Toast.LENGTH_SHORT)
                                .show()

                            title = ""
                            genero = ""
                            info = ""
                            navController.popBackStack()
                        },
                        onFailure = { exception ->
                            Toast.makeText(
                                context,
                                "Erro ao salvar: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
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
                Text("Adicionar Scrobble")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = "Voltar para a lista de Músicas",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

