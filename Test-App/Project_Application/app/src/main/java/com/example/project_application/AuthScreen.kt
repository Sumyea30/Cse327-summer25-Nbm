// FILE: AuthScreen.kt
package com.example.project_application

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp


enum class AuthScreenType {
    HOME, LOGIN, SIGNUP
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var currentScreen by remember { mutableStateOf(AuthScreenType.HOME) }

    when (currentScreen) {
        AuthScreenType.HOME -> WelcomeScreen(
            onLogin = { currentScreen = AuthScreenType.LOGIN },
            onSignUp = { currentScreen = AuthScreenType.SIGNUP },
            onLoginSuccess = onLoginSuccess
        )

        AuthScreenType.LOGIN -> LoginScreen(
            onBack = { currentScreen = AuthScreenType.HOME },
            onLoginSuccess = onLoginSuccess,
            onNavigateToSignUp = { currentScreen = AuthScreenType.SIGNUP }
        )

        AuthScreenType.SIGNUP -> SignUpScreen(
            onBack = { currentScreen = AuthScreenType.HOME },
            onLoginSuccess = onLoginSuccess,
            onNavigateToLogin = { currentScreen = AuthScreenType.LOGIN }
        )
    }
}

fun Modifier.gradientBackground(): Modifier = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(Color(0x56EC4BE9), Color(0x6A1572D6))
    )
)

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onSignUp: () -> Unit, onLoginSuccess: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome!")

        Spacer(modifier = Modifier.height(24.dp))

        GoogleSignInButton(onLoginSuccess = onLoginSuccess)

        Text("Highly Recommended", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLogin, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF42B6F4), // Google Blue
            contentColor = Color.White
        ), modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSignUp, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF42F4E5), // Google Blue
            contentColor = Color.White
        ), modifier = Modifier.fillMaxWidth()) {
            Text("Sign Up")
        }
    }
}

@Composable
fun LoginScreen(onBack: () -> Unit, onLoginSuccess: () -> Unit, onNavigateToSignUp: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF42F4A7), // Google Blue
            contentColor = Color.White
        ), modifier = Modifier.fillMaxWidth()) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (email.isNotBlank()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                Toast.makeText(context, "Password reset sent", Toast.LENGTH_SHORT).show()
            }
        }, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF46C42), // Google Blue
            contentColor = Color.White
        ), modifier = Modifier.fillMaxWidth()) {
            Text("Forgot Password?")
        }

        TextButton(onClick = onNavigateToSignUp) {
            Text("Don’t have an account? Sign Up")
        }

        GoogleSignInButton(onLoginSuccess)
    }
}

@Composable
fun SignUpScreen(onBack: () -> Unit, onLoginSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Fetching...") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        country = address?.firstOrNull()?.countryName ?: "Unknown"
                    } else {
                        country = "Unavailable"
                    }
                } catch (e: SecurityException) {
                    country = "Permission error"
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text("Sign Up")

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Date Of Birth") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = country, onValueChange = {}, label = { Text("Country") }, enabled = false, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                    db.collection("users").document(uid).set(
                        mapOf(
                            "name" to fullName,
                            "dob" to dob,
                            "email" to email,
                            "country" to country
                        )
                    ).addOnSuccessListener {
                        Toast.makeText(context, "Signed up!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Signup failed", Toast.LENGTH_SHORT).show()
                }
        }, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF42F4D9),
            contentColor = Color.White
        )
            , modifier = Modifier.fillMaxWidth()) {

            Text("Sign Up")
        }

        GoogleSignInButton(onLoginSuccess)
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Sign In")
        }
    }
}

@Composable
fun GoogleSignInButton(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Google Sign-In success", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-In error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(stringResource(id = R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    Button(
        onClick = {
            val intent = googleSignInClient.signInIntent
            launcher.launch(intent)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4E42F4),
            contentColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign in with Google")
    }
}
