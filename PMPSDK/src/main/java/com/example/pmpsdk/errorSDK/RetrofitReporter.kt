package com.example.pmpsdk.errorSDK

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class RetrofitReporter(private val config: SDKConfig) {
    companion object {
        private const val TAG = "PMP_RetrofitReporter"
    }

    private val okHttpClient = OkHttpClient.Builder()   //创建OkHttpClient
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .retryOnConnectionFailure(true)
        .build()

    private val gson: Gson = GsonBuilder()  //创建Gson实例
        .setLenient()
        .create()

    private val retrofit: Retrofit by lazy {    //创建Retrofit实例
        Retrofit.Builder()
            .baseUrl("http://placeholder.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val errorReportApi: ErrorReportApi by lazy {    //创建API服务
        retrofit.create(ErrorReportApi::class.java)
    }

    fun sendData(jsonData: String): Boolean {
        if (!config.enableErrorReporting) {
            Log.d(TAG, "错误上报已禁用")
            return false
        }
        try {
            val errorReportData = parseJsonData(jsonData)   //解析JSON数据到ErrorReportData对象
            if (errorReportData == null) {
                Log.e(TAG, "解析JSON数据失败")
                return false
            }
            val call = errorReportApi.reportError(config.apiUrl, errorReportData)   //创建API调用
            call.enqueue(object : Callback<ErrorReportResponse> {   //执行异步请求
                override fun onResponse(call: Call<ErrorReportResponse>, response: Response<ErrorReportResponse>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        Log.d(TAG, "错误报告发送成功: ${responseBody?.message}")
                    } else {
                        Log.e(TAG, "服务器响应错误: ${response.code()}, ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ErrorReportResponse>, t: Throwable) {
                    Log.e(TAG, "发送错误报告失败: ${t.message}")
                }
            })

            return true
        } catch (e: Exception) {
            Log.e(TAG, "创建HTTP请求失败: ${e.message}")
            return false
        }
    }

    private fun parseJsonData(jsonData: String): ErrorReportData? {
        return try {
            gson.fromJson(jsonData, ErrorReportData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "使用GSON解析JSON失败: ${e.message}")
            null
        }
    }

    fun getStatus(): Map<String, Any> {
        return mapOf(
            "queuedMessages" to 0,
            "isProcessingQueue" to false
        )
    }
}