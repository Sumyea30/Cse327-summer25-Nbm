package com.example.project_application

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.project_application.ui.theme.Project_ApplicationTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase once
        FirebaseApp.initializeApp(this)

        setContent {
            Project_ApplicationTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = {
                            Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
                            currentScreen = "permissions"
                        }
                    )

                    "permissions" -> PermissionScreen(
                        onPermissionsGranted = {
                            currentScreen = "onboarding"
                        }
                    )

                    "onboarding" -> OnboardingScreen(
                        onFinish = {
                            currentScreen = "home"
                        }
                    )

                   // "home" -> HomeScreen()
                }
            }
        }
    }
}
