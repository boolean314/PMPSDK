package com.example.pmpsdk.errorSDK

data class SDKConfig(
    val projectId: String,                              //项目ID
    val apiUrl: String,                                 //API地址
    val enableGlobalExceptionHandler: Boolean = true,   //启用全局异常处理
    val enableChildThreadMonitoring: Boolean = true,    //启用子线程监控
    val showUserFriendlyMessage: Boolean = true,        //显示用户友好页面
    val userFriendlyMessage: String = "该功能暂不可用",    //用户友好页面内容
    val enableErrorReporting: Boolean = true,           //启用错误上报
    val maxRetryAttempts: Int = 3,                      //最大重试次数
    val retryDelayMs: Long = 5000                       //重试延迟时间
) {
    companion object {
        fun createDefault(projectId: String, apiUrl: String): SDKConfig {
            return SDKConfig(
                projectId = projectId,
                apiUrl = apiUrl
            )
        }
    }
}