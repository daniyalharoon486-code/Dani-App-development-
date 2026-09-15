package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.common.AppStrings
import com.example.ui.common.Formatters
import com.example.ui.viewmodel.DukanViewModel
import kotlinx.coroutines.launch

@Composable
fun StaffUsersScreen(
    viewModel: DukanViewModel
) {
    val isUrdu by viewModel.isUrdu.collectAsState()
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("staff_users_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active User Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isUrdu) "موجودہ فعال صارف (لاگ ان):" else "Current Active Cashier / User:", fontSize = 12.sp)
                        Text(
                            text = "${currentUser.name} (${currentUser.role.name})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(onClick = { showAddUserDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isUrdu) "+ نیا عملہ" else "+ Add Staff")
                    }
                }
            }
        }

        // Staff List with 1-tap switch
        item {
            Text(
                text = if (isUrdu) "دکان کے عملے کی فہرست (سوئچ کریں)" else "Shop Staff List (Tap to Switch Active User)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(users, key = { it.id }) { user ->
            val isActive = user.id == currentUser.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("ACTIVE", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                        Text("Username: @${user.username} • Role: ${user.role.name} • Phone: ${user.phone}", fontSize = 11.sp, color = Color.Gray)
                    }

                    if (!isActive) {
                        OutlinedButton(
                            onClick = { viewModel.switchUser(user) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Switch")
                        }
                    }
                }
            }
        }

        // Audit Logs
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isUrdu) "سسٹم آڈٹ لاگ (حالیہ سرگرمیاں)" else "System Audit & Security Activity Log",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(auditLogs.take(20), key = { it.id }) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(Formatters.formatDateTime(log.timestamp), fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(log.details, fontSize = 12.sp)
                    Text("By: ${log.userName}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            isUrdu = isUrdu,
            onDismiss = { showAddUserDialog = false },
            onSave = { name, username, pin, role, phone ->
                coroutineScope.launch {
                    viewModel.repository.insertUser(
                        UserEntity(
                            name = name,
                            username = username,
                            pin = pin,
                            role = role,
                            phone = phone
                        )
                    )
                    viewModel.showToast("Staff user $name created successfully!")
                }
                showAddUserDialog = false
            }
        )
    }
}

@Composable
private fun AddUserDialog(
    isUrdu: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, username: String, pin: String, role: UserRole, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("1234") }
    var role by remember { mutableStateOf(UserRole.CASHIER) }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrdu) "نیا عملہ / کیشئر شامل کریں" else "Add New Staff Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, singleLine = true)
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username *") }, singleLine = true)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Quick PIN Code *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
                Text("Role:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(UserRole.CASHIER, UserRole.MANAGER, UserRole.ADMIN).forEach { r ->
                        FilterChip(selected = role == r, onClick = { role = r }, label = { Text(r.name, fontSize = 11.sp) })
                    }
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && username.isNotBlank()) {
                    onSave(name.trim(), username.trim(), pin.trim(), role, phone.trim())
                }
            }) {
                Text(if (isUrdu) "محفوظ کریں" else "Save Staff")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
