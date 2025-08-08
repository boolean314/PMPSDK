package com.example.pmpsdk.errorSDK

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ErrorReportApi {
    @POST
    fun reportError(@Url fullUrl: String, @Body errorReport: ErrorInfo): Call<ErrorReportResponse>
}

data class ErrorInfo(
    val projectId: String,
    val platform: String = "android",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "error",
    val message: String,
    val stack: String,
    val className: String,
    val errorType: String
)

data class ErrorReportResponse(
    val success: Boolean,
    val message: String? = null,
    val errorId: String? = null
)

