package com.example.myapplication.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    // Each toggle has its own state
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailUpdates by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var locationEnabled by remember { mutableStateOf(true) }
    var anonymousByDefault by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── SECTION 1: Notifications ──
            SettingsSectionHeader(title = "Notifications")

            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Get notified about your complaint updates",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            SettingsToggleItem(
                icon = Icons.Default.Email,
                title = "Email Updates",
                subtitle = "Receive updates via email",
                checked = emailUpdates,
                onCheckedChange = { emailUpdates = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── SECTION 2: Appearance ──
            SettingsSectionHeader(title = "Appearance")

            SettingsToggleItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Switch to dark theme",
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── SECTION 3: Privacy ──
            SettingsSectionHeader(title = "Privacy")

            SettingsToggleItem(
                icon = Icons.Default.LocationOn,
                title = "Location Access",
                subtitle = "Auto detect location when reporting",
                checked = locationEnabled,
                onCheckedChange = { locationEnabled = it }
            )

            SettingsToggleItem(
                icon = Icons.Default.VisibilityOff,
                title = "Anonymous by Default",
                subtitle = "Always report anonymously",
                checked = anonymousByDefault,
                onCheckedChange = { anonymousByDefault = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── SECTION 4: About ──
            SettingsSectionHeader(title = "About")

            SettingsClickItem(
                icon = Icons.Default.Info,
                title = "About ReportIt India",
                subtitle = "Version 1.0.0"
            )

            SettingsClickItem(
                icon = Icons.Default.Shield,
                title = "Privacy Policy",
                subtitle = "How we handle your data"
            )

            SettingsClickItem(
                icon = Icons.Default.Description,
                title = "Terms of Service",
                subtitle = "Rules and guidelines"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── SECTION 5: Danger Zone ──
            SettingsSectionHeader(title = "Account")

            // Delete account button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Delete Account",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Permanently delete all your data",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── SECTION HEADER ──
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

// ── TOGGLE ITEM ──
@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

// ── CLICK ITEM (no toggle) ──
@Composable
fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}