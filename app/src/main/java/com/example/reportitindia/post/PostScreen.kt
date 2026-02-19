package com.example.reportitindia.post

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reportitindia.feed.Complaint
import com.example.reportitindia.feed.ComplaintRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// All the categories a complaint can belong to
val complaintCategories = listOf(
    "Roads", "Sanitation", "Water", "Electricity",
    "Parks", "Street Lights", "Drainage", "Other"
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
    viewModel: PostViewModel = viewModel()
) {
    val postState by viewModel.state.collectAsStateWithLifecycle()

    // Each variable here is one form field
    // mutableStateOf stores the current value
    // remember keeps it alive across redraws
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(1f) }
    var isAnonymous by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(postState) {
        if(postState is PostState.Success) {
            showSuccess = true
        }

    }
    // Form is valid only when required fields are filled
    val isFormValid = title.isNotBlank()
            && description.isNotBlank()
            && location.isNotBlank()
            && selectedCategory.isNotBlank()

    // Show success dialog
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Complaint Submitted!") },
            text = {
                Text("Your complaint has been submitted successfully. You can track its status in the feed.")
            },
            confirmButton = {
                Button(onClick = {
                    showSuccess = false
                    onSubmitSuccess()
                }) {
                    Text("View Feed")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Report an Issue",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── SECTION 1: Title ──
            SectionLabel(text = "Issue Title *")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Large pothole on MG Road") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )

            // ── SECTION 2: Category ──
            SectionLabel(text = "Category *")
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.ifBlank { "Select a category" },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    complaintCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // ── SECTION 3: Description ──
            SectionLabel(text = "Description *")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Describe the issue in detail...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            // ── SECTION 4: Location ──
            SectionLabel(text = "Location *")
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("e.g. MG Road near Junction, Bangalore") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                }
            )

            // ── SECTION 5: Severity ──
            SectionLabel(text = "Severity")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Low",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            // Show severity label based on slider value
                            text = when {
                                severity < 1.5f -> "Low"
                                severity < 2.5f -> "Medium"
                                else -> "High"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                severity < 1.5f -> MaterialTheme.colorScheme.primary
                                severity < 2.5f -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = "High",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Slider(
                        value = severity,
                        onValueChange = { severity = it },
                        valueRange = 1f..3f,
                        steps = 1
                    )
                }
            }

            // ── SECTION 6: Anonymous toggle ──
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
                    Column {
                        Text(
                            text = "Report Anonymously",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Your name won't be shown publicly",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it }
                    )
                }
            }

            // ── SECTION 7: Submit Button ──
            Button(
                onClick = {
                    isSubmitting = true
                    // Fake submission delay
                    // When we add Firebase, real upload happens here
                    showSuccess = true
                    isSubmitting = false
                },
                enabled = isFormValid && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Complaint",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Required fields note
            Text(
                text = "* Required fields",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Reusable section label composable
// Instead of repeating the same Text style everywhere
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}