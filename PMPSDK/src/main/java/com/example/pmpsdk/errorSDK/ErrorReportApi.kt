package com.example.pmpsdk.errorSDK

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface ErrorReportApi {
    @POST
    fun reportError(@Url fullUrl: String, @Body errorReport: ErrorInfo): Call<ErrorReportResponse>
}

data class ErrorInfo(
    val projectId: String,
    val platform: String = "android",
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
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

