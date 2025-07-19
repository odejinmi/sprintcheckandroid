package com.nibsssdk.services

import android.net.Uri
import android.os.Build
import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager
import com.a5starcompany.sprintchecksdk.util.Logger
import com.nibsssdk.services.retrofit.RetrofitClient
import org.json.JSONException
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

class HttpJsonParser {

    suspend fun makeRetrofitRequest(
        url: String,
        method: String,
        params: Map<String, String?>?,
        publickey: String?
    ): JSONObject? {// Convert params to a string representation for hashing
        val signature = if (params != null) {
            val paramsString = mapToJsonLikeString(params)
            Logger().d("paramsString", paramsString)
            var hashpayload = generateHmacSha512(paramsString)
            hashpayload
        }else{
            ""
        }
        val deviceName = "${Build.MANUFACTURER} | ${Build.MODEL} | ${Build.BRAND} | ${Build.DEVICE}"

        Logger().i("baseurl request", url)
        return try {
            val response = when (method.uppercase()) {
                "GET" -> {
                    // Append encoded params to the URL
                    val fullUrl = if (params?.isNotEmpty() == true) {
                        val query = params.map { "${it.key}=${Uri.encode(it.value)}" }.joinToString("&")
                        "$url?$query"
                    } else url
                    RetrofitClient.apiService.getWithToken(fullUrl, "$publickey", signature)
                }
                "POST" -> {
                    RetrofitClient.apiService.postWithToken(url, "$publickey", signature, params ?: emptyMap())
                }
                "PUT" -> {
                    println("put request")
                    RetrofitClient.apiService.putWithToken(url, "$publickey", signature, params ?: emptyMap())
                }
                else -> throw IllegalArgumentException("Unsupported method: $method")
            }

            if (response.isSuccessful) {
                val responseBody = response.body()!!
                if (responseBody.trim().startsWith("{") || responseBody.trim().startsWith("[")) {
                    JSONObject(responseBody)
                    } else {
                    Logger().v("API", "Invalid JSON: $responseBody", )
                        null
                    }
            } else {
                val errorBody = response.errorBody()?.string()
                Logger().e("Retrofit", "Error: ${response.code()} - ${response.errorBody()?.string()}", null)
                if (!errorBody.isNullOrEmpty() && (errorBody.trim().startsWith("{") || errorBody.trim().startsWith("["))) {
                    try {
                        JSONObject(errorBody)
                    } catch (e: JSONException) {
                        Logger().e("API", "Invalid JSON: $errorBody", e)
                        null
                    }
                }else{
                    null
                }

            }

        } catch (e: Exception) {
            Logger().e("Retrofit", "Exception: ${e.message}", e)
            null
        }
    }

    fun generateHmacSha512(message: String): String {
        val secretKey = KYCVerificationManager.getInstance().config?.encryptionkey
        val key = secretKey?.toByteArray(StandardCharsets.UTF_8)
        val messageBytes = message.toByteArray(StandardCharsets.UTF_8)

        val mac = Mac.getInstance("HmacSHA512")
        val secretKeySpec = SecretKeySpec(key, "HmacSHA512")
        mac.init(secretKeySpec)

        val digest = mac.doFinal(messageBytes)

        return digest.joinToString("") { "%02x".format(it) }
    }

    // Method 4: JSON-like string format
    fun mapToJsonLikeString(params: Map<String, String?>): String {
        val order = listOf("number", "identifier")
        return order.filter { params.containsKey(it) }
            .joinToString(",", "{", "}") {
                "\"$it\":${params[it]?.let { v -> "\"$v\"" } ?: ""}"
            }
    }
}
