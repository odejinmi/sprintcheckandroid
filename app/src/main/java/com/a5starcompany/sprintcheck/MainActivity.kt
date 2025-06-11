package com.a5starcompany.sprintcheck

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a5starcompany.sprintcheck.ui.theme.SprintcheckTheme
import com.a5starcompany.sprintchecksdk.util.CheckoutMethod
import com.a5starcompany.sprintchecksdk.util.KYCConfig
import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager

class MainActivity : ComponentActivity() {

    private val kycLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                val verificationId = result.data?.getStringExtra("reference")
                val score = result.data?.getIntExtra("confidenceLevel", 0) ?: 0
                val isLive = result.data?.getBooleanExtra("status", false) ?: false
                val verify = result.data?.getBooleanExtra("verify", false) ?: false
                val message = result.data?.getStringExtra("message")
                val name = result.data?.getStringExtra("name")
                val bvn = result.data?.getStringExtra("bvn")
                val nin = result.data?.getStringExtra("nin")
                val method = result.data?.getStringExtra("method")

                Log.d("TAG", "result: ${result.data?.extras} $method")
                // Handle successful verification
                handleVerificationSuccess(verificationId, score, isLive)
            }
            RESULT_CANCELED -> {
                val errorCode = result.data?.getStringExtra("error_code")
                val errorMessage = result.data?.getStringExtra("error_message")

                // Handle verification failure or cancellation
                handleVerificationFailure(errorCode, errorMessage)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SprintcheckTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(

                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp)
                            .focusable(true) // Make it focusable to capture key events
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp) { // Capture only when key is released
                                    val key = keyEvent.key

//                                    if (key == Key.Backspace && text.value.isNotEmpty()) {
//                                        showDialog.value = false
//                                        text.value = text.value.dropLast(1) // Remove last digit
//                                    } else if (key == Key.Back) {
//                                        showDialog.value = false
//                                    } else if (key == Key.Enter && text.value.isNotEmpty()) {
//                                        startdebit()
//                                    } else if (text.value.length < 4 && key.nativeKeyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) {
//                                        text.value += key.nativeKeyCode
//                                            .minus(KeyEvent.KEYCODE_0)
//                                            .toString() // Append digit
//                                    }
                                }
                                true // Consume event
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                        Button(
                            onClick = {

                                KYCVerificationManager.getInstance().startVerification(this@MainActivity, kycLauncher, CheckoutMethod.bvn,"odejinmiabraham@gmail.com")
//                                SprintCheck().start(this@MainActivity)
                            },
//                            enabled = text.value.isNotEmpty() && text.value.toInt() != 0,
                            ) {
                            Text("start BVN Verification")
                        }
                        Button(
                            onClick = {

                                KYCVerificationManager.getInstance().startVerification(this@MainActivity, kycLauncher, CheckoutMethod.nin,"odejinmiabraham@gmail.com")
//                                SprintCheck().start(this@MainActivity)
                            },
//                            enabled = text.value.isNotEmpty() && text.value.toInt() != 0,
                            ) {
                            Text("start NIN Verification")
                        }

                    }
                }
            }
        }
    // Initialize KYC library
    val config = KYCConfig(
        apiKey = "scb1edcd88-64f7485186d9781ca624a903",
        encryptionkey = "enc67fe4978b16fc1744718200"
    )
    KYCVerificationManager.getInstance().initialize(config)
}

private fun handleVerificationSuccess(verificationId: String?, score: Int, isLive: Boolean) {
    // Handle successful verification
    Toast.makeText(this, "Verification successful! ID: $verificationId, Score: $score", Toast.LENGTH_LONG).show()
}

private fun handleVerificationFailure(errorCode: String?, errorMessage: String?) {
    // Handle verification failure
    Toast.makeText(this, "Verification failed: $errorMessage", Toast.LENGTH_LONG).show()
}
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SprintcheckTheme {
        Greeting("Android")
    }
}