package com.example.lastfm.ui.theme.view



import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lastfm.data.Scrobble
import com.example.tudohorrorosoapp.ui.components.content
import com.example.tudohorrorosoapp.ui.components.scrobbleCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scrobbles(navController: NavController) {
    val scrobbles = remember { mutableStateOf<List<Scrobble>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val source = "my_scrobbles"
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        if (currentUserId != null) {
            firestore.collection("scrobbles")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { result ->
                    val fetchedscrobbles = result.mapNotNull { document ->
                        val scrobble = document.toObject(Scrobble::class.java)
                        scrobble.copy(id = document.id)
                    }
                    scrobbles.value = fetchedscrobbles
                    isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    isLoading.value = false
                }
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                content(navController = navController, isDrawerOpen = drawerState.isOpen)
            },
            content = {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Minhas scrobbles") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir Menu")
                                }
                            }
                        )
                    },
                    content = { paddingValues ->
                        if (isLoading.value) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "Carregando scrobbles...", style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = paddingValues,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                items(scrobbles.value) { scrobble ->
                                    scrobbleCard(scrobble = scrobble, navController = navController,source = source)
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { navController.navigate("add_scrobble") },
                            icon = { Icon(Icons.Filled.Add, contentDescription = "Adicionar Scrobble") },
                            text = { Text("") }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.End
                )
            }
        )
    }
}
