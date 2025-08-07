package com.example.pmpsdk.errorSDK

import android.util.Log
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.Executors

class ErrorReporter constructor(private val config: SDKConfig) {
    companion object {
        private const val TAG = "ErrorReporter"
    }

    private val executor = Executors.newCachedThreadPool()  //线程池用于异步上报

    private val retrofitReporter = RetrofitReporter(config)

    fun reportError(throwable: Throwable, errorType: String) {
        if (!config.enableErrorReporting) {
            Log.d(TAG, "错误上报已禁用")
            return
        }
        executor.execute {
            try {
                val errorData = generateErrorData(throwable, errorType)
                sendViaHttp(errorData)
            } catch (e: Exception) {
                Log.e(TAG, "上报错误失败: ${e.message}")
            }
        }
    }

    private fun generateErrorData(throwable: Throwable, errorType: String): String {
        val data = JSONObject().apply {
            put("error", JSONObject().apply {
                put("projectId", config.projectId)
                put("platform", "android")
                put("timestamp", System.currentTimeMillis())
                put("message", throwable.message ?: "Unknown error")
                put("stack", getStackTraceString(throwable))
                put("className", throwable.javaClass.name)
                put("errorType", errorType)
            })
        }
        return data.toString(2)
    }

    private fun getStackTraceString(throwable: Throwable): String { //获取异常堆栈信息
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    private fun sendViaHttp(data: String) {
        try {
            Log.d(TAG, "准备发送JSON数据: $data") //添加调试日志
            retrofitReporter.sendData(data) //使用Retrofit发送
        } catch (e: Exception) {
            Log.e(TAG, "HTTP发送异常: ${e.message}")
        }
    }
}