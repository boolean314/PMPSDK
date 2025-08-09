package com.example.pmpsdk.performance

import com.example.mylibrary.PerformanceMonitor
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface SdkService {
    // 使用 @Url 注解允许传入完整 URL
    @POST
  fun sendPerformanceData(@Url url: String, @Body data: PerformanceMonitor.PerformanceData): Call<ResponseBody>
}



