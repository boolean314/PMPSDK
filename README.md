# PMPSDK

1.基于Handler机制和UncaughtExceptionHandler实现的Android异常监控SDK。

2.可根据需求对特定操作的性能进行采集，指示应用运行效率，提供优化方向。

# 注意！

* 使用前在请在Module级的build.gradle.kts文件下导入依赖

```kotlin
implementation("com,github.boolean314:PMPSDK:v1.1.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

* 在settings.gradle.kts文件中添加链接

```kotlin
dependencyResolutionManagement {
    ...
        maven {
            url = uri("https://jitpack.io")
        }
    ...
}
```

* 在gradle.properties文件中加上

```kotlin
android.enableJetifier=true
```

**祝你使用愉快~**

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
      "projectId": "PROJ-XXXXXX",
      "platform": "android",
      "timestamp": 1620000000000,
      "message": "error message",
      "stack": "at com.example.Service.run(Service.java:42)...",
      "className": "java.lang.NullPointerException",
      "errorType": "NullPointerException"
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

### API参考

| function                                                     | 功能                         |
| :----------------------------------------------------------- | :--------------------------- |
| **initPerformanceMonitor**(projectId: String, url: String,isMonitor: Boolean=true) | 初始化性能监控               |
| **wrapClickListener**(listener: (View) -> Unit): (View)      | 监测执行点击事件的性能       |
| **wrapLongClickListener**(listener: (View) -> Boolean): (View) | 监测执行长按事件的性能       |
| **wrapScrollViewListener**(scrollView: ScrollView)           | 监测ScrollView滚动的性能     |
| **wrapRecyclerViewListener**(recyclerView: RecyclerView)     | 监测RecyclerView滚动的性能   |
| **wrapPageChangeListener**(viewPager: ViewPager2)            | 监测页面跳转的性能           |
| **wrapFragmentLifecycleCallbacks**(fragment: androidx.fragment.app.Fragment) | 监测碎片创建到销毁时的性能   |
| **wrapActivityLifecycleCallbacks**(activity: AppCompatActivity） | 监测活动从创建到销毁时的性能 |

### 性能数据JSON示例

```json
{
	"project id":"PROJ-XXXXXX",
	"platform":"android",
	"type":"performance",
	"timestamp": 1620000000000,
    "data":{
   		 "device_model":"sdk gphone64 x86 64",
   		 "os_version":"Android 13",
   		 "battery_level":"100%",
   		 "memory_usage":{
    		"usedMemory":"6MB",
    		"totalMemory":"47MB"
						},
    "operation_fps":"btn:55.56639307648892'
			}
}
```

### 调用示例

* **initPerformanceMonitor**(projectId: String, url: String,isMonitor: Boolean=true)

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...
        PerformanceMonitor.initPerformanceMonitor("PROJ-123","https://monitor.yourplatform.com/api")//如果要关闭监控功能，再传入第三个参数为false
        ...
        }
```



* **wrapClickListener**(listener: (View) -> Unit): (View)

  ```kotlin
  //监控按钮的点击事件
  binding.yourButtonId.setOnClickListener(
      PerformanceMonitor.wrapClickListener { 
      //你的点击事件逻辑
          ...
      }
  )
  
  
  //监控列表子项的点击事件
  //在Adapter中
   override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       ...
          holder.itemView.setOnClickListener(
              PerformanceMonitor.wrapClickListener {
                 //你的点击事件逻辑
              }
          )
      }	
  ```



* **wrapLongClickListener**(listener: (View) -> Boolean): (View)

  ```kotlin
  binding.yourButtonId.setOnLongClickListener(
  PerformanceMonitor.wrapLongClickListener { 
      // 长按处理逻辑
   ...
      true // 返回true表示消费了长按事件
  }
  )
  ```



* **wrapScrollViewListener**(scrollView: ScrollView)

  ```kotlin
  PerformanceMonitor.wrapScrollViewListener(binding.yourScrollViewId)
  ```



* **wrapRecyclerViewListener**(recyclerView: RecyclerView)

  ```kotlin
  PerformanceMonitor.wrapRecyclerViewListener(binding.yourRecyclerViewId)
  ```



* **wrapPageChangeListener**(viewPager: ViewPager2)

  ```kotlin
  PerformanceMonitor.wrapPageChangeListener(binding.yourViewPagerId)
  ```



* **wrapFragmentLifecycleCallbacks**(fragment: androidx.fragment.app.Fragment)

  ```kotlin
  class MyFragment : Fragment() {
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           // 在Fragment中调用此方法来监控性能
           PerformanceMonitor.wrapFragmentLifecycleCallbacks(this)
       }
      // ... 其他Fragment代码
   }
  ```



* **wrapActivityLifecycleCallbacks**(activity: AppCompatActivity）

```kotlin
 class YourActivity : AppCompatActivity(){
 override fun onCreate(savedInstanceState: Bundle?) {
     super.onCreate(savedInstanceState)
     binding = ActivityMainBinding.inflate(layoutInflater)
     setContentView(binding.root)
     // 在Activity中调用此方法来监控性能
     PerformanceMonitor.wrapActivityLifecycleCallbacks(this)

     // 其他初始化代码...
 }
 }
```
