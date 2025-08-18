package com.example.project_application

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project_application.core.WorkflowModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkflowHistoryScreen(
    navController: NavController,
    workflows: List<WorkflowModel>,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Workflow History", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        if (workflows.isEmpty()) {
            Text("No workflows yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(workflows) { workflow ->
                    WorkflowItem(workflow)
                    Divider()
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onClearHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Clear History")
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun WorkflowItem(workflow: WorkflowModel) {
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dateText = dateFormatter.format(Date(workflow.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Workflow ID: ${workflow.id}", style = MaterialTheme.typography.titleMedium)
        Text("Source: ${workflow.source} → Destination: ${workflow.destination}")
        Text("Receiver: ${workflow.receiver}")
        Text("Type: ${workflow.type}")
        if (!workflow.filterField.isNullOrEmpty()) {
            Text("Filter: ${workflow.filterField} = ${workflow.filterValue ?: ""}")
        }
        Text("Scheduled: ${workflow.schedule}")
        Text("Created: $dateText")
    }
}
