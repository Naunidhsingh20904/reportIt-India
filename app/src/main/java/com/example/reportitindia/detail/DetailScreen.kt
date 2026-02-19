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

// The possible statuses a complaint can be in
enum class ComplaintStatus {
    SUBMITTED,
    FORWARDED,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED
}

// Each step in the timeline
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
    onBackClick: () -> Unit = {}
) {
    // Fake complaint data based on id
    // Later we'll fetch this from Firebase using the id
    val complaint = getFakeComplaint(complaintId)
    val statusSteps = getFakeStatusSteps()

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

        // verticalScroll lets the whole screen scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── SECTION 1: Category + Title ──
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

            Text(
                text = complaint.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // ── SECTION 2: Meta info ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Location
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

                // Votes
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

            // ── SECTION 3: Description ──
            Text(
                text = "About this complaint",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = complaint.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            HorizontalDivider()

            // ── SECTION 4: Status Timeline ──
            Text(
                text = "Status Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Loop through each status step
            statusSteps.forEachIndexed { index, step ->
                StatusTimelineItem(
                    step = step,
                    isLast = index == statusSteps.lastIndex
                )
            }

            HorizontalDivider()

            // ── SECTION 5: Support Button ──
            var hasSupported by remember { mutableStateOf(false) }

            Button(
                onClick = { hasSupported = !hasSupported },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
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

            // ── SECTION 6: Reported by ──
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
                    // Avatar circle
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                // First letter of author name as avatar
                                text = complaint.authorName.first().toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Reported by",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = complaint.authorName,
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

// ── STATUS TIMELINE ITEM ──
@Composable
fun StatusTimelineItem(
    step: StatusStep,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {

        // Left side - circle + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Circle indicator
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

            // Vertical line connecting steps
            // Don't draw line after last item
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .padding(vertical = 2.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side - text
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
            Text(
                text = step.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── FAKE DATA FUNCTIONS ──

// Returns a fake complaint based on id
// When we connect Firebase, we'll replace this with a real fetch
fun getFakeComplaint(id: String) = when (id) {
    "1" -> FakeComplaint(
        title = "Large pothole on MG Road",
        description = "Dangerous pothole causing accidents near the main junction. Has been there for 3 months with no action from authorities despite multiple complaints.",
        location = "MG Road, Bangalore",
        category = "Roads",
        votes = 142,
        authorName = "Rahul M"
    )
    "2" -> FakeComplaint(
        title = "Garbage not collected for 2 weeks",
        description = "The garbage truck has not come to our street for 2 weeks. The smell is unbearable and residents are very concerned about health hazards.",
        location = "Koramangala, Bangalore",
        category = "Sanitation",
        votes = 89,
        authorName = "Priya S"
    )
    else -> FakeComplaint(
        title = "Complaint #$id",
        description = "Details for complaint $id",
        location = "Bangalore",
        category = "Other",
        votes = 10,
        authorName = "User"
    )
}

data class FakeComplaint(
    val title: String,
    val description: String,
    val location: String,
    val category: String,
    val votes: Int,
    val authorName: String
)

// Returns fake status steps
// Complaint 1 is "In Progress", others are just "Submitted"
fun getFakeStatusSteps() = listOf(
    StatusStep(
        status = ComplaintStatus.SUBMITTED,
        label = "Submitted",
        description = "Complaint received successfully",
        isCompleted = true,
        isActive = false
    ),
    StatusStep(
        status = ComplaintStatus.FORWARDED,
        label = "Forwarded to Ward Office",
        description = "Sent to BBMP Ward 42",
        isCompleted = true,
        isActive = false
    ),
    StatusStep(
        status = ComplaintStatus.ASSIGNED,
        label = "Officer Assigned",
        description = "Mr. Suresh Kumar assigned",
        isCompleted = false,
        isActive = true
    ),
    StatusStep(
        status = ComplaintStatus.IN_PROGRESS,
        label = "Work Started",
        description = "Pending",
        isCompleted = false,
        isActive = false
    ),
    StatusStep(
        status = ComplaintStatus.RESOLVED,
        label = "Resolved",
        description = "Pending",
        isCompleted = false,
        isActive = false
    )
)