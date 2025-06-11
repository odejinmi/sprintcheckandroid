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

    fun initialize(config: KYCConfig) {
        this.config = config
    }

    fun startVerification(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
        type: CheckoutMethod,
        identifier: String
    ) {
        this.transactiontype = type
        this.identifier = identifier
        val intent = Intent(activity, MainActivity::class.java)
        launcher.launch(intent)
    }

    val baseUrl get() : String{
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
