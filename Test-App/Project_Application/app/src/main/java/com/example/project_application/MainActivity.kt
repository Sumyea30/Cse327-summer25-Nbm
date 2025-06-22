package com.example.project_application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.example.project_application.ui.theme.Project_ApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            Project_ApplicationTheme {
                LoginScreen(
                    onLoginSuccess = {
                        Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
                        // TODO: Go to HomeScreen later
                    }
                )
            }
        }
    }
}
