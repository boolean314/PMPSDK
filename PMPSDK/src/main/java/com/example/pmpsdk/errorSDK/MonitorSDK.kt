package com.example.pmpsdk.errorSDK

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.pmpsdk.errorSDK.FriendlyActivity
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("StaticFieldLeak")
object MonitorSDK {
    private const val TAG = "PMP_MonitorSDK"
    private var config: SDKConfig? = null
    private var context: Context? = null
    private var errorReporter: ErrorReporter? = null
    private val isInitialized = AtomicBoolean(false)
    private val isEnabled = AtomicBoolean(false)
    private var originalUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private val hasExceptionOccurred = AtomicBoolean(false)

    fun initA(context: Context, projectId: String, apiUrl: String) {
        val defaultConfig = SDKConfig.createDefault(projectId, apiUrl)
        initB(context, defaultConfig)
    }

    fun initB(context: Context, config: SDKConfig) {
        if (isInitialized.getAndSet(true)) return
        this.context = context.applicationContext
        this.config = config
        this.errorReporter = ErrorReporter(config)
    }

    fun enable() {
        if (!isInitialized.get() || isEnabled.getAndSet(true)) return
        config?.let { cfg ->
            if (cfg.enableGlobalExceptionHandler) setupGlobalExceptionHandler()
            if (cfg.enableChildThreadMonitoring) setupChildThreadMonitoring()
        }
    }

    fun disable() {
        if (!isEnabled.getAndSet(false)) return
        hasExceptionOccurred.set(false) //重置标志位
        restoreOriginalHandlers()
    }

    private fun setupGlobalExceptionHandler() {
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CustomUncaughtExceptionHandler())
    }

    private fun setupChildThreadMonitoring() {
        val originalThreadGroup = Thread.currentThread().threadGroup    //通过ThreadGroup监控子线程异常
        val monitoredThreadGroup = object : ThreadGroup(originalThreadGroup, "PMP_Monitored") {
            override fun uncaughtException(t: Thread, e: Throwable) {
                handleException(e, "child_thread_exception")
                super.uncaughtException(t, e)
            }
        }
    }

    private fun showUserFriendlyMessage() {
        config?.let { cfg ->
            if (cfg.showUserFriendlyMessage) {
                context?.let { ctx ->
                    Handler(Looper.getMainLooper()).post {
                        try {
                            val intent = Intent(ctx, FriendlyActivity::class.java)
                            intent.putExtra("message", cfg.userFriendlyMessage)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //确保可以跨应用启动
                            ctx.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    private fun restoreOriginalHandlers() {
        originalUncaughtExceptionHandler?.let {
            Thread.setDefaultUncaughtExceptionHandler(it)
        }
    }

    private class CustomUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, ex: Throwable) {
            val errorType = determineErrorType(ex)
            handleException(ex, errorType)
            if (hasExceptionOccurred.getAndSet(true)) {
                return
            }
            try {
                showUserFriendlyMessage()
            } catch (_: Exception) {}
        }
    }

    private fun determineErrorType(throwable: Throwable): String {  //根据异常类型确定错误类型
        return when (throwable) {
            is NullPointerException -> "null_pointer_exception"
            is IllegalArgumentException -> "illegal_argument_exception"
            is IllegalStateException -> "illegal_state_exception"
            is IndexOutOfBoundsException -> "index_out_of_bounds_exception"
            is ArithmeticException -> "arithmetic_exception"
            is ClassCastException -> "class_cast_exception"
            is IOException -> "io_exception"
            is SecurityException -> "security_exception"
            is NumberFormatException -> "number_format_exception"
            is StackOverflowError -> "stack_overflow_error"
            is OutOfMemoryError -> "out_of_memory_error"
            is RuntimeException -> "runtime_exception"
            else -> "uncaught_exception"
        }
    }

    private fun handleException(throwable: Throwable, errorType: String) {
        showUserFriendlyMessage()
        errorReporter?.reportError(throwable, errorType)
    }

    fun reportError(
        throwable: Throwable,
        errorType: String = "manual_report",  //改为空字符串默认值
        mapOf: Map<String, String> = emptyMap()
    ) {
        if (!isEnabled.get()) return
        val actualErrorType = errorType.ifBlank {
            determineErrorType(throwable)
        }
        errorReporter?.reportError(throwable, actualErrorType)
    }
}
