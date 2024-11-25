package com.example.lastfm.ui.theme.view


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lastfm.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditProfile(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var nome by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currentUser = auth.currentUser
    var changePassword by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            db.collection("usuarios").document(it.uid).get()
                .addOnSuccessListener { document ->
                    nome = document.getString("nome") ?: ""
                }
                .addOnFailureListener { e ->
                    errorMessage = e.message
                }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(navController.context, it, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }

    fun updateUserProfile() {
        if (nome.isEmpty()) {
            errorMessage = "Todos os campos são obrigatórios"
            return
        }

        if (password.isNotEmpty() && password != confirmPassword) {
            errorMessage = "As senhas não coincidem"
            return
        }

        loading = true

        currentUser?.let { user ->
            val updateTasks = mutableListOf<Task<Void>>()

            if (password.isNotEmpty()) {
                updateTasks.add(user.updatePassword(password))
            }

            Tasks.whenAllComplete(updateTasks).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection("usuarios").document(user.uid)
                        .update("nome", nome)
                        .addOnCompleteListener { firestoreTask ->
                            loading = false
                            if (firestoreTask.isSuccessful) {
                                Toast.makeText(
                                    navController.context,
                                    "Perfil atualizado com sucesso!",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.popBackStack()
                            } else {
                                errorMessage = firestoreTask.exception?.message
                            }
                        }
                } else {
                    loading = false
                    errorMessage = task.exception?.message
                }
            }
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
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = "Edição de Perfil",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            // Campo de nome
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Nome"
                    )
                }
            )

            // Opção para alterar senha
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = changePassword,
                    onCheckedChange = { changePassword = it }
                )
                Text(text = "Alterar senha")
            }

            // Campos de senha, caso habilitado
            if (changePassword) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nova Senha (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Toggle senha"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar Nova Senha") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            // Botão para salvar alterações
            Button(
                onClick = { updateUserProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9E0505),
                    contentColor = Color.White
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "Salvar Alterações")
                }
            }
        }
    }
}

