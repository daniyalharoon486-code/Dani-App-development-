package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ReceiptSize
import com.example.data.model.ShopSettingsEntity
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.DukanViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Local form state
    var shopName by remember(settings) { mutableStateOf(settings?.shopName ?: "Bismillah Mart & Khata") }
    var shopNameUrdu by remember(settings) { mutableStateOf(settings?.shopNameUrdu ?: "بسم اللہ مارٹ و جنرل اسٹور") }
    var phone by remember(settings) { mutableStateOf(settings?.phone ?: "0300-1234567") }
    var address by remember(settings) { mutableStateOf(settings?.address ?: "Main Commercial Market, Lahore") }
    var invoicePrefix by remember(settings) { mutableStateOf(settings?.invoicePrefix ?: "INV-") }
    var receiptFooter by remember(settings) { mutableStateOf(settings?.receiptFooter ?: "Thank you for your visit! جزاکم اللہ خیرا") }
    var selectedReceiptSize by remember(settings) { mutableStateOf(settings?.receiptSize ?: ReceiptSize.THERMAL_80MM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isUrdu) "سسٹم و دکان کی ترتیبات" else "App & Shop Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Offline & Cloud Sync Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(if (isUrdu) "آف لائن لوکل ڈیٹا بیس" else "Offline-First Engine (Room DB)", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Last Synced: ${Formatters.formatDateTime(lastSyncTime)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.triggerCloudSync() },
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Syncing...")
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isUrdu) "مطابقت" else "Sync Now")
                        }
                    }
                }
            }
        }

        // Appearance & Language Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (isUrdu) "زبان اور تھیم (Language & Theme)" else "Language & Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isUrdu) "ایپ کی زبان (Language)" else "Application Language", fontWeight = FontWeight.SemiBold)
                        Text(if (isUrdu) "اردو فعال ہے" else "English is active", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(onClick = { viewModel.toggleLanguage() }) {
                        Text(if (isUrdu) "Switch to English" else "اردو میں دیکھیں")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isUrdu) "نائٹ / ڈارک موڈ" else "Dark Mode", fontWeight = FontWeight.SemiBold)
                        Text(if (isDarkMode) "Enabled" else "Disabled", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }
            }
        }

        // Shop Profile & Bill Information Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (isUrdu) "دکان اور بل پرنٹنگ کی تفصیل" else "Shop Details & Bill Header", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Divider()

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shopNameUrdu,
                    onValueChange = { shopNameUrdu = it },
                    label = { Text("دکان کا نام (Urdu)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = invoicePrefix,
                    onValueChange = { invoicePrefix = it },
                    label = { Text("Invoice Prefix (e.g. INV-)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Default Thermal Receipt Paper Size:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReceiptSize.values().forEach { sz ->
                        FilterChip(
                            selected = selectedReceiptSize == sz,
                            onClick = { selectedReceiptSize = sz },
                            label = { Text(sz.title) }
                        )
                    }
                }

                OutlinedTextField(
                    value = receiptFooter,
                    onValueChange = { receiptFooter = it },
                    label = { Text("Receipt Footer Note / Greeting") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val cur = settings ?: ShopSettingsEntity()
                            viewModel.repository.updateSettings(
                                cur.copy(
                                    shopName = shopName,
                                    shopNameUrdu = shopNameUrdu,
                                    phone = phone,
                                    address = address,
                                    invoicePrefix = invoicePrefix,
                                    receiptSize = selectedReceiptSize,
                                    receiptFooter = receiptFooter
                                )
                            )
                            viewModel.showToast("Shop settings saved successfully!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isUrdu) "ترتیبات محفوظ کریں" else "Save Shop Settings")
                }
            }
        }

        // Backup & Data Export Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (isUrdu) "ڈیٹا کا بیک اپ اور ایکسپورٹ" else "Database Backup & Export", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Divider()

                Text("Create an offline snapshot backup of all products, customers, and transactions in JSON format.", fontSize = 12.sp, color = Color.Gray)

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val jsonBackup = viewModel.repository.exportDatabaseBackup()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_TEXT, jsonBackup)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Database Backup JSON"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isUrdu) "ڈیٹا بیک اپ ایکسپورٹ کریں" else "Export Database Backup (JSON)")
                }
            }
        }
    }
}
