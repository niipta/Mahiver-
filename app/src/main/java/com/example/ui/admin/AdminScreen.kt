package com.example.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.noAdminSet -> {
                // No admin set yet — show "Become Admin" button
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(64.dp), tint = MahirColors.gold())
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Admin Set", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You can become the admin of this app. Once set, only you can manage users and settings.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.becomeAdmin() },
                        colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold())
                    ) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Become Admin", color = MahirColors.goldForeground(), fontWeight = FontWeight.Bold)
                    }
                    state.message?.let { msg ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(msg, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            !state.isAdmin -> {
                // Admin is someone else
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Access Denied", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Only the admin can access this panel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            else -> {
                // Admin dashboard
                AdminContent(state, viewModel, padding)
            }
        }
    }
}

@Composable
private fun AdminContent(
    state: AdminUiState,
    viewModel: AdminViewModel,
    padding: PaddingValues
) {
    var contactEmail by remember(state.config.contactEmail) { mutableStateOf(state.config.contactEmail) }
    var contactPhone by remember(state.config.contactPhone) { mutableStateOf(state.config.contactPhone) }
    var contactInstagram by remember(state.config.contactInstagram) { mutableStateOf(state.config.contactInstagram) }
    var upiId by remember(state.config.upiId) { mutableStateOf(state.config.upiId) }
    var upiName by remember(state.config.upiName) { mutableStateOf(state.config.upiName) }
    var subPrice by remember(state.config.subscriptionPrice) { mutableStateOf(state.config.subscriptionPrice) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
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

        item { Text("Contact & Payment Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

        item { OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("Contact Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Contact Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = contactInstagram, onValueChange = { contactInstagram = it }, label = { Text("Instagram ID") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("UPI ID") }, placeholder = { Text("yourname@upi") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = upiName, onValueChange = { upiName = it }, label = { Text("UPI Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(value = subPrice, onValueChange = { subPrice = it }, label = { Text("Subscription Price") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }

        item {
            Button(
                onClick = {
                    viewModel.saveConfig(state.config.copy(
                        contactEmail = contactEmail, contactPhone = contactPhone,
                        contactInstagram = contactInstagram, upiId = upiId,
                        upiName = upiName, subscriptionPrice = subPrice
                    ))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Settings") }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("User Management (${state.users.size} users)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(state.users, key = { it.uid }) { user ->
            UserManagementCard(user, onToggleSub = { viewModel.toggleSubscription(user.uid, !user.isSubscribed) }, onToggleBlock = { viewModel.toggleBlockUser(user.uid, !user.isBlocked) })
        }
    }
}

@Composable
private fun UserManagementCard(user: UserProfile, onToggleSub: () -> Unit, onToggleBlock: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
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
                OutlinedButton(onClick = onToggleSub, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = if (user.isSubscribed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)) {
                    Text(if (user.isSubscribed) "Subscribed" else "Not Subscribed", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onToggleBlock, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = if (user.isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)) {
                    Text(if (user.isBlocked) "Blocked" else "Active", fontSize = 12.sp)
                }
            }
        }
    }
}
