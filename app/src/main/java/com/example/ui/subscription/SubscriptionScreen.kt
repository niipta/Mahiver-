package com.example.ui.subscription

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.admin.AdminRepository
import com.example.data.admin.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor() : ViewModel() {

    private val repo = AdminRepository()
    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            _config.value = repo.getAppConfig()
            _loading.value = false
        }
    }
}

@Composable
fun SubscriptionScreen(
    navController: androidx.navigation.NavController,
    viewModel: SubscriptionViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Subscribe to MahirVerse", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(config.subscriptionPrice, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))

                // UPI Payment info
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pay via UPI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("UPI ID:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(config.upiId.ifBlank { "Not set by admin" }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Name: ${config.upiName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Open UPI app button
                        if (config.upiId.isNotBlank()) {
                            Button(
                                onClick = {
                                    val upiUri = "upi://pay?pa=${config.upiId}&pn=${config.upiName}&am=99&cu=INR&tn=MahirVerse Subscription"
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(upiUri)))
                                    } catch (e: Exception) {
                                        // No UPI app — copy UPI ID
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("UPI ID", config.upiId))
                                        android.widget.Toast.makeText(context, "UPI ID copied: ${config.upiId}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pay Now via UPI")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("After payment, contact admin to activate your subscription:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Contact buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (config.contactEmail.isNotBlank()) {
                        OutlinedButton(onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${config.contactEmail}"))
                            context.startActivity(Intent.createChooser(intent, "Email"))
                        }) { Text("Email", fontSize = 12.sp) }
                    }
                    if (config.contactPhone.isNotBlank()) {
                        OutlinedButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${config.contactPhone}")))
                        }) { Text("Call", fontSize = 12.sp) }
                    }
                    if (config.contactInstagram.isNotBlank()) {
                        OutlinedButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/${config.contactInstagram}")))
                        }) { Text("Instagram", fontSize = 12.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Maybe later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
