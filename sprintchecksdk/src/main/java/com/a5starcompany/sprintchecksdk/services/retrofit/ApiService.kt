package com.nibsssdk.services.retrofit

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST
    suspend fun postWithToken(
        @Url url: String,
        @Header("Authorization") publickey: String,
        @Header("signature") signature: String?,
        @FieldMap params: Map<String, String?>
    ): Response<String>

    @FormUrlEncoded
    @PUT
    suspend fun putWithToken(
        @Url url: String,
        @Header("Authorization") publickey: String,
        @Header("signature") signature: String?,
        @FieldMap params: Map<String, String?>
    ): Response<String>

    @GET
    suspend fun getWithToken(
        @Url url: String,
        @Header("Authorization") publickey: String,
        @Header("signature") signature: String?,
    ): Response<String>
}
