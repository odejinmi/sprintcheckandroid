package com.a5starcompany.sprintchecksdk.util

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.a5starcompany.sprintchecksdk.MainActivity

class KYCVerificationManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: KYCVerificationManager? = null

        fun getInstance(): KYCVerificationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KYCVerificationManager().also { INSTANCE = it }
            }
        }
    }
    var config: KYCConfig? = null
    var transactiontype: CheckoutMethod? = null
    var identifier: String? = null
    private var callback: KYCCallback? = null
    private var isInitialized = false

    fun initialize(config: KYCConfig): KYCInitializationResult {
        return try {
            // Validate configuration
            val validationResult = validateConfig(config)
            if (validationResult != null) {
                return validationResult
            }

            // Store configuration
            this.config = config
            this.isInitialized = true

            // Optionally perform additional initialization tasks
            performInitializationTasks()

            KYCInitializationResult.Success
        } catch (e: Exception) {
            KYCInitializationResult.Failure(
                errorCode = "INIT_ERROR",
                errorMessage = "Failed to initialize KYC SDK: ${e.message}"
            )
        }
    }

    private fun validateConfig(config: KYCConfig): KYCInitializationResult.Failure? {
        // Validate API key
        if (config.apiKey.isBlank()) {
            return KYCInitializationResult.Failure(
                errorCode = "INVALID_API_KEY",
                errorMessage = "API key cannot be empty"
            )
        }

        if (config.apiKey.length < 10) {
            return KYCInitializationResult.Failure(
                errorCode = "INVALID_API_KEY",
                errorMessage = "API key is too short"
            )
        }

        // Validate encryption key
        if (config.encryptionkey.isBlank()) {
            return KYCInitializationResult.Failure(
                errorCode = "INVALID_ENCRYPTION_KEY",
                errorMessage = "Encryption key cannot be empty"
            )
        }

        if (config.encryptionkey.length < 16) {
            return KYCInitializationResult.Failure(
                errorCode = "INVALID_ENCRYPTION_KEY",
                errorMessage = "Encryption key must be at least 16 characters"
            )
        }

        return null
    }

    private fun performInitializationTasks() {
        // Perform any additional initialization tasks here
        // For example: network connectivity check, SDK version validation, etc.
    }

    fun isInitialized(): Boolean {
        return isInitialized && config != null
    }

    fun startVerification(
        activity: Activity,
        type: CheckoutMethod,
        identifier: String,
        callback: KYCCallback
    ): Boolean {
        // Check if SDK is initialized
        if (!isInitialized()) {
            callback.onKYCFailure(
                KYCResult.Failure(
                    errorCode = "SDK_NOT_INITIALIZED",
                    errorMessage = "KYC SDK is not initialized. Please call initialize() first."
                )
            )
            return false
        }

        this.transactiontype = type
        this.identifier = identifier
        this.callback = callback

        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        return true
    }

    // Internal method for MainActivity to report results
    internal fun reportResult(result: KYCResult) {
        when (result) {
            is KYCResult.Success -> callback?.onKYCSuccess(result)
            is KYCResult.Failure -> callback?.onKYCFailure(result)
            is KYCResult.Cancelled -> callback?.onKYCCancelled()
        }
        // Clear callback after use to prevent memory leaks
        callback = null
    }

    val baseUrl get(): String {
        return "https://sprintcheck.megasprintlimited.com.ng/api/sdk/"
    }
}

// KYCConfig.kt
data class KYCConfig(
    val apiKey: String,
    val encryptionkey: String,
)

// KYCResult.kt
sealed class KYCResult {
    data class Success(
        val message: String,
        val reference: String,
        val name: String,
        val bvn: String,
        val nin: String,
        val confidenceLevel: Int,
        val status: Boolean,
        val verify: Boolean,
        val method: CheckoutMethod?
    ) : KYCResult()

    data class Failure(
        val errorCode: String,
        val errorMessage: String,
        val confidenceLevel: Int = 0
    ) : KYCResult()

    object Cancelled : KYCResult()
}

enum class CheckoutMethod {
    bvn, nin, selectable
}

interface KYCCallback {
    fun onKYCSuccess(result: KYCResult.Success)
    fun onKYCFailure(result: KYCResult.Failure)
    fun onKYCCancelled()
}

sealed class KYCInitializationResult {
    object Success : KYCInitializationResult()

    data class Failure(
        val errorCode: String,
        val errorMessage: String
    ) : KYCInitializationResult()
}