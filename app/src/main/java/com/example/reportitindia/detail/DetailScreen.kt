package com.example.reportitindia.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

enum class ComplaintStatus {
    SUBMITTED, FORWARDED, ASSIGNED, IN_PROGRESS, RESOLVED
}

data class StatusStep(
    val status: ComplaintStatus,
    val label: String,
    val description: String,
    val isCompleted: Boolean,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    complaintId: String,
    onBackClick: () -> Unit = {},
    viewModel: DetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load complaint when screen opens
    LaunchedEffect(complaintId) {
        viewModel.loadComplaint(complaintId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        when (val currentState = state) {
            is DetailState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is DetailState.Success -> {
                val complaint = currentState.complaint
                val statusSteps = getStatusSteps(complaint.status)
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                var hasSupported by remember {
                    mutableStateOf(complaint.upvotedBy.contains(currentUserId))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── Category chip ──
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = complaint.category,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // ── Title ──
                    Text(
                        text = complaint.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // ── Meta info ──
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = complaint.location,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${complaint.votes} supporters",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider()

                    // ── Description ──
                    Text(text = "About this complaint", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = complaint.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    HorizontalDivider()

                    // ── Status Timeline ──
                    Text(text = "Status Timeline", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    statusSteps.forEachIndexed { index, step ->
                        StatusTimelineItem(
                            step = step,
                            isLast = index == statusSteps.lastIndex
                        )
                    }

                    HorizontalDivider()

                    // ── Support Button ──
                    Button(
                        onClick ={
                            hasSupported = !hasSupported
                            if (hasSupported) {
                                viewModel.upvoteComplaint(complaintId)
                            } else {
                                viewModel.downvoteComplaint(complaintId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasSupported)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (hasSupported) Icons.Default.Check else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasSupported) "You supported this" else "Support this complaint",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ── Author card ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (complaint.authorName.isNotEmpty())
                                            complaint.authorName.first().toString()
                                        else "U",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Reported by", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (complaint.isAnonymous) "Anonymous" else complaint.authorName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// Builds status steps based on current status string from Firestore
fun getStatusSteps(currentStatus: String): List<StatusStep> {
    val statusOrder = listOf("SUBMITTED", "FORWARDED", "ASSIGNED", "IN_PROGRESS", "RESOLVED")
    val currentIndex = statusOrder.indexOfFirst {
        it.equals(currentStatus.trim(), ignoreCase = true)
    }.coerceAtLeast(0)

    return listOf(
        StatusStep(ComplaintStatus.SUBMITTED, "Submitted", "Complaint received", currentIndex > 0, currentIndex == 0),
        StatusStep(ComplaintStatus.FORWARDED, "Forwarded to Ward Office", "Sent to local authority", currentIndex > 1, currentIndex == 1),
        StatusStep(ComplaintStatus.ASSIGNED, "Officer Assigned", "Officer assigned to complaint", currentIndex > 2, currentIndex == 2),
        StatusStep(ComplaintStatus.IN_PROGRESS, "Work Started", "Issue being addressed", currentIndex > 3, currentIndex == 3),
        StatusStep(ComplaintStatus.RESOLVED, "Resolved", "Issue has been resolved", currentIndex > 4, currentIndex == 4)
    )
}

@Composable
fun StatusTimelineItem(step: StatusStep, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = when {
                    step.isCompleted -> MaterialTheme.colorScheme.primary
                    step.isActive -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (step.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(40.dp).padding(vertical = 2.dp)) {
                    HorizontalDivider(modifier = Modifier.fillMaxHeight().width(2.dp))
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 8.dp)) {
            Text(
                text = step.label,
                fontSize = 14.sp,
                fontWeight = if (step.isActive) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    step.isActive -> MaterialTheme.colorScheme.primary
                    step.isCompleted -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(text = step.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}