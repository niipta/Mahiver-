package com.example.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.admin.UserProfile
import com.example.ui.theme.MahirColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: AdminViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.isAdmin) {
        // Not admin — show access denied
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Panel") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Access Denied", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("You are not an admin", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    var contactEmail by remember(state.config.contactEmail) { mutableStateOf(state.config.contactEmail) }
    var contactPhone by remember(state.config.contactPhone) { mutableStateOf(state.config.contactPhone) }
    var contactInstagram by remember(state.config.contactInstagram) { mutableStateOf(state.config.contactInstagram) }
    var upiId by remember(state.config.upiId) { mutableStateOf(state.config.upiId) }
    var upiName by remember(state.config.upiName) { mutableStateOf(state.config.upiName) }
    var subPrice by remember(state.config.subscriptionPrice) { mutableStateOf(state.config.subscriptionPrice) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Message snackbar
            state.message?.let { msg ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(msg, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") }
                        }
                    }
                }
            }

            // Contact & Payment Settings
            item {
                Text("Contact & Payment Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("Contact Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Contact Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = contactInstagram, onValueChange = { contactInstagram = it }, label = { Text("Instagram ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("UPI ID") }, placeholder = { Text("yourname@upi") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = upiName, onValueChange = { upiName = it }, label = { Text("UPI Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = subPrice, onValueChange = { subPrice = it }, label = { Text("Subscription Price") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            item {
                Button(
                    onClick = {
                        viewModel.saveConfig(state.config.copy(
                            contactEmail = contactEmail,
                            contactPhone = contactPhone,
                            contactInstagram = contactInstagram,
                            upiId = upiId,
                            upiName = upiName,
                            subscriptionPrice = subPrice
                        ))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Settings") }
            }

            // User Management
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("User Management (${state.users.size} users)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(state.users, key = { it.uid }) { user ->
                UserManagementCard(
                    user = user,
                    onToggleSub = { viewModel.toggleSubscription(user.uid, !user.isSubscribed) },
                    onToggleBlock = { viewModel.toggleBlockUser(user.uid, !user.isBlocked) }
                )
            }
        }
    }
}

@Composable
private fun UserManagementCard(
    user: UserProfile,
    onToggleSub: () -> Unit,
    onToggleBlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isBlocked) MaterialTheme.colorScheme.errorContainer
                            else if (user.isSubscribed) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name.ifBlank { "Unknown" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${user.points} pts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MahirColors.gold())
                    Text("${user.streak} day streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Subscription toggle
                OutlinedButton(
                    onClick = onToggleSub,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (user.isSubscribed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (user.isSubscribed) "Subscribed ✓" else "Not Subscribed", fontSize = 12.sp)
                }
                // Block toggle
                OutlinedButton(
                    onClick = onToggleBlock,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (user.isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(if (user.isBlocked) "Blocked" else "Active", fontSize = 12.sp)
                }
            }
        }
    }
}
