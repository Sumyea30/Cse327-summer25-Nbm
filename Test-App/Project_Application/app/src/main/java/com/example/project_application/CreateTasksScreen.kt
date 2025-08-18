package com.example.project_application

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val dueDateTime: String, // ISO 8601 format, e.g., "2025-08-18T14:30"
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateTasksScreen(navController: NavController) {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Task?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDateTime by remember { mutableStateOf("") }

    // Load tasks from SharedPreferences
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
        val taskJson = prefs.getString("tasks", null)
        tasks = taskJson?.let { Json.decodeFromString<List<Task>>(it) } ?: emptyList()
    }

    // Create notification channel
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifications for task reminders" }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Add/Edit Task Dialog
    if (showAddDialog || showEditDialog != null) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                showEditDialog = null
                title = ""
                description = ""
                dueDateTime = ""
            },
            title = { Text(if (showEditDialog != null) "Edit Task" else "Add Task") },
            text = {
                Column {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = dueDateTime,
                        onValueChange = { dueDateTime = it },
                        label = { Text("Due Date (YYYY-MM-DD HH:MM)") },
                        placeholder = { Text("e.g., 2025-08-18 14:30") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isBlank() || dueDateTime.isBlank()) {
                            Toast.makeText(context, "Title and due date are required", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        try {
                            LocalDateTime.parse(dueDateTime.replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Invalid date format. Use YYYY-MM-DD HH:MM", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        val newTask = Task(
                            id = showEditDialog?.id ?: UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            dueDateTime = dueDateTime,
                            isCompleted = showEditDialog?.isCompleted ?: false
                        )

                        tasks = if (showEditDialog != null) {
                            tasks.map { if (it.id == newTask.id) newTask else it }
                        } else {
                            tasks + newTask
                        }

                        // Save tasks to SharedPreferences
                        val prefs = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("tasks", Json.encodeToString(tasks)).apply()

                        // Schedule alarm if not completed
                        if (!newTask.isCompleted) {
                            scheduleTaskAlarm(context, newTask)
                        }

                        Toast.makeText(context, if (showEditDialog != null) "Task updated" else "Task added", Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                        showEditDialog = null
                        title = ""
                        description = ""
                        dueDateTime = ""
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    showEditDialog = null
                    title = ""
                    description = ""
                    dueDateTime = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tasks") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            title = task.title
                            description = task.description
                            dueDateTime = task.dueDateTime
                            showEditDialog = task
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { isChecked ->
                                tasks = tasks.map {
                                    if (it.id == task.id) it.copy(isCompleted = isChecked) else it
                                }
                                val prefs = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("tasks", Json.encodeToString(tasks)).apply()
                                if (isChecked) {
                                    cancelTaskAlarm(context, task)
                                } else {
                                    scheduleTaskAlarm(context, task)
                                }
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleMedium)
                            Text(task.description, style = MaterialTheme.typography.bodySmall)
                            Text("Due: ${task.dueDateTime}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = {
                            tasks = tasks.filter { it.id != task.id }
                            val prefs = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("tasks", Json.encodeToString(tasks)).apply()
                            cancelTaskAlarm(context, task)
                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("🗑️")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ScheduleExactAlarm")
@RequiresApi(Build.VERSION_CODES.O)
private fun scheduleTaskAlarm(context: Context, task: Task) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
        putExtra("task_id", task.id)
        putExtra("task_title", task.title)
        putExtra("task_description", task.description)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        val dueTime = LocalDateTime.parse(task.dueDateTime.replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val triggerTime = dueTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerTime > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to schedule alarm for ${task.title}", Toast.LENGTH_SHORT).show()
    }
}

private fun cancelTaskAlarm(context: Context, task: Task) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, TaskAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val taskTitle = intent.getStringExtra("task_title") ?: "Task"
        val taskDescription = intent.getStringExtra("task_description") ?: ""

        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Task Reminder: $taskTitle")
            .setContentText(taskDescription)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId.hashCode(), notification)
    }
}