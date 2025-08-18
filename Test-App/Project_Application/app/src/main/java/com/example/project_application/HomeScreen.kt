// FILE: HomeScreen.kt
package com.example.project_application

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_application.R

@Composable
fun HomeScreen(
    onCreateWorkflow: () -> Unit,
    onZoneWorkflow: () -> Unit,
    onCreateImageWorkflow: () -> Unit,
    onCamera: () -> Unit,
    onCreateTasks: () -> Unit,
    onSettings: () -> Unit,
    onWorkflowHistory: () -> Unit,
    onTalkWithFreely: () -> Unit
)
{

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // App Logo
        Image(
            painter = painterResource(id = R.drawable.logo2), // Replace with your logo file
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Options
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HomeMenuCard("Create Workflow", onCreateWorkflow)
                HomeMenuCard("Zone Workflow", onZoneWorkflow)
                HomeMenuCard("Smart Image Workflow", onCreateImageWorkflow)
                HomeMenuCard("Camera Scanner", onCamera)
                HomeMenuCard("Create Tasks", onCreateTasks)
                HomeMenuCard("Settings", onSettings)
                HomeMenuCard("Workflow History", onWorkflowHistory)
                HomeMenuCard(
                    title = "Talk with Freely (Main Chat)",
                    onClick = onTalkWithFreely,
                    highlight = true
                )
            }
        }
    }
}

@Composable
fun HomeMenuCard(title: String, onClick: () -> Unit, highlight: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primary else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun HomeScreenTest() {
    HomeScreen(
        onCreateWorkflow = { println("Create Workflow clicked") },
        onZoneWorkflow = { println("Zone Workflow clicked") },
        onCreateTasks = { println("Create Tasks clicked") },
        onCreateImageWorkflow = { println("Create Image Workflow clicked") },
        onCamera = { println("Camera clicked") },
        onSettings = { println("Settings clicked") },
        onWorkflowHistory = { println("Workflow History clicked") },
        onTalkWithFreely = { println("Talk with Freely clicked") }
    )
}
