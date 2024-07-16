package com.example.biometricsampleapp

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.biometricsampleapp.ui.theme.BiometricSampleAppTheme
import com.example.biometricsampleapp.utils.BiometricStatus
import com.example.biometricsampleapp.utils.BiometricUtils
import com.example.biometricsampleapp.utils.showToast

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricSampleAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val executor = remember { ContextCompat.getMainExecutor(context) }

    val biometricUtils =
        remember {
            BiometricUtils(context)
        }

    val promptInfo =
        biometricUtils.createPromptInfo(
            "Title",
            "Subtitle",
            BIOMETRIC_STRONG,
            "Negative Button",
            "Description",
            false,
        )

    val biometricSetting =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        }

    val biometricPrompt =
        biometricUtils.createBiometricPromptCallback(
            activity = activity!!,
            executor = executor,
            onSuccess = { result ->
                Log.d("TAG", "onAuthenticationSucceeded: $result")
                Log.d("TAG", "onAuthenticationSucceeded: ${result.authenticationType}")
                Log.d("TAG", "onAuthenticationSucceeded: ${result.cryptoObject}")
                showToast(context, result.toString())
            },
            onFailed = {
                showToast(context, "Failed")
            },
            onError = { errorCode, message ->
                showToast(context, "$errorCode $message")
            },
        )

    Column(
        modifier = modifier,
    ) {
        Button(onClick = { showToast(context, biometricUtils.checkBiometricAvailability().name) }) {
            Text(text = "Check Availability")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val status = biometricUtils.checkBiometricStatus(type = BIOMETRIC_STRONG)
            when (status) {
                BiometricStatus.AVAILABLE ->
                    showToast(context, "Available")

                BiometricStatus.UNAVAILABLE ->
                    showToast(context, "Unavailable")

                BiometricStatus.NOT_ENROLLED ->
                    biometricUtils.navigateToEnroll(biometricSetting)

                BiometricStatus.NO_HARDWARE ->
                    showToast(context, "No Hardware Available")
            }
        }) {
            Text(text = "Show Biometric Status")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            biometricPrompt.authenticate(promptInfo)
        }) {
            Text(text = "Show Biometric Prompt")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiometricSampleAppTheme {
        Greeting()
    }
}
