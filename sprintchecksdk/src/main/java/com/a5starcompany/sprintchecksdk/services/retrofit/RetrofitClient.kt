package com.nibsssdk.services.retrofit

import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)     // Default is 10s
            .readTimeout(60, TimeUnit.SECONDS)        // Default is 10s
            .writeTimeout(60, TimeUnit.SECONDS)       // Default is 10s
            .callTimeout(120, TimeUnit.SECONDS)       // Overall timeout
            .build()
    }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(KYCVerificationManager.getInstance().baseUrl) // Will be overridden if you use @Url
            .client(okHttpClient) // Add the custom OkHttpClient
            .addConverterFactory(ScalarsConverterFactory.create()) // Use Scalars for raw string
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
