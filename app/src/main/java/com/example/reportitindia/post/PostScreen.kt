package com.example.reportitindia.post

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reportitindia.ai.GeminiRepository
import kotlinx.coroutines.launch

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
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var aiError by remember { mutableStateOf("") }
    val geminiRepository = remember { GeminiRepository() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(1f) }
    var isAnonymous by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(postState) {
        if (postState is PostState.Success) {
            showSuccess = true
        }
    }

    // AI analysis helper
    suspend fun analyzeImage(bitmap: Bitmap) {
        isAnalyzing = true
        aiError = ""
        val result = geminiRepository.analyzeComplaintImage(bitmap)
        if (result.isSuccess) {
            val analysis = result.getOrNull()!!
            selectedCategory = analysis.category
            description = analysis.description
            severity = when (analysis.severity.lowercase()) {
                "low" -> 1f
                "high" -> 3f
                else -> 2f
            }
        } else {
            aiError = "AI analysis failed. Please fill in details manually."
        }
        isAnalyzing = false
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            capturedImage = it
            scope.launch { analyzeImage(it) }
        }
    }

    // Gallery launcher
    val context = androidx.compose.ui.platform.LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                @Suppress("DEPRECATION")
                val bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    selectedUri
                )
                capturedImage = bitmap
                scope.launch { analyzeImage(bitmap) }
            } catch (e: Exception) {
                aiError = "Could not load image. Try taking a photo instead."
            }
        }
    }

    val isFormValid = title.isNotBlank()
            && description.isNotBlank()
            && location.isNotBlank()
            && selectedCategory.isNotBlank()

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
            text = { Text("Your complaint has been submitted successfully. You can track its status in the feed.") },
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
                title = { Text("Report an Issue", fontWeight = FontWeight.Bold) },
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

            // ── SECTION 0: Photo + AI ──
            SectionLabel(text = "Add Photo (AI will auto-fill details)")

            if (capturedImage != null) {
                androidx.compose.foundation.Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add a photo for AI analysis",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera")
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery")
                }
            }

            if (isAnalyzing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text(
                            text = "AI is analyzing your photo...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (aiError.isNotEmpty()) {
                Text(text = aiError, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            }

            if (capturedImage != null && !isAnalyzing && aiError.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AI has filled in the details. Review and edit if needed.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // ── SECTION 1: Title ──
            SectionLabel(text = "Issue Title *")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Large pothole on MG Road") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
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
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
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
                modifier = Modifier.fillMaxWidth().height(120.dp),
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
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
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
                        Text(text = "Low", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
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
                        Text(text = "High", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Report Anonymously", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            text = "Your name won't be shown publicly",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                }
            }

            // ── SECTION 7: Submit Button ──
            Button(
                onClick = {
                    viewModel.submitComplaint(
                        title = title,
                        description = description,
                        location = location,
                        category = selectedCategory,
                        severity = severity,
                        isAnonymous = isAnonymous
                    )
                },
                enabled = isFormValid && postState !is PostState.Loading && !isAnalyzing,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (postState is PostState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Submit Complaint", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (postState is PostState.Error) {
                Text(
                    text = (postState as PostState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "* Required fields",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}