# PMPSDK

1.基于Handler机制和UncaughtExceptionHandler实现的Android异常监控SDK。

2.可根据需求对特定操作的性能进行采集，指示应用运行效率，提供优化方向。

## 一、异常上报

### 核心特性

- Handler机制接管主线程/子线程异常
- 全局异常处理，防止应用崩溃
- 异常日志JSON通过Http实时上报
- 支持初始化、启用、禁用，便于集成和移植

### 快速开始

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MonitorSDK.initA(this, "PROJ-123", "https://monitor.yourplatform.com/api")	//注意不要输入多的空格哦
        MonitorSDK.enable() //启用SDK
    }
}
```

### 拦截异常日志JSON示例

```json
{
    "error": {
      "projectId": "PROJ-XXXXXX",
      "platform": "android",
      "timestamp": 1620000000000,
      "message": "error message",
      "stack": "at com.example.Service.run(Service.java:42)...",
      "className": "java.lang.NullPointerException",
      "errorType": "NullPointerException"
    }
}
```

### API 参考

```kotlin
MonitorSDK.initA(context: Context, projectId: String, apiUrl: String)    //初始化SDK
MonitorSDK.enable()   //启用SDK
MonitorSDK.disable()    //禁用SDK
MonitorSDK.reportError(throwable: Throwable, errorType: String = "manual_report")   //手动上报异常
```



## 二.性能数据采集

