package com.example.mylibrary

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.pmpsdk.performance.SdkService
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




object PerformanceMonitor {
    private var lastFrameTimeNanos = 0L
    private var lastFrameLongTimeNanos = 0L
    private var frameCount = 0
    private var frameLongCount = 0
    private var frameCallback: Choreographer.FrameCallback? = null
    private var frameLongCallback: Choreographer.FrameCallback? = null
    private var operationFps = 0.0
    private var isMonitoring = false
    private var isLongMonitoring = false
    private val model = Build.MODEL
    private val OS = Build.VERSION.RELEASE
    private var projectId = "123"//需要初始化，暂时赋值为123
    private lateinit var viewResourceId: String
    private  var isMonitor:Boolean=true
    private  var url:String=""
    private lateinit var appService: SdkService
    fun initPerformanceMonitor(projectId: String, url: String,isMonitor: Boolean=true) {
        if(isMonitor){
            this.projectId = projectId
            this.url=url
            val retrofit=Retrofit.Builder()
                .baseUrl("https://httpbin.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            appService=retrofit.create(SdkService::class.java)
        }else{
            return
        }



    }


    fun sendPerformanceData(context: Context) {
        if (!isMonitor){
            return
        }
        val memoryInfo = JSONObject().apply {
            put("usedMemory", "${getMemoryInfo().usedMemory}MB")
            put("totalMemory", "${getMemoryInfo().totalMemory}MB")
        }
        val dataJson = JSONObject().apply {
            put("device_model", model)
            put("os_version", "Android $OS")
            put("battery_level", "${getBatteryLevel(context)}%")
            put("memory_usage", memoryInfo)
            put("operation_fps", "${viewResourceId}:$operationFps")
        }
        val json = JSONObject().apply {
            put("project_id", projectId)
            put("platform", "android")
            put("type", "performance")
            put("timestamp", System.currentTimeMillis())
                .put("data", dataJson)

        }
        // appService.sendPerformanceData(url, dataJson)
        Log.d("PerformanceMonitor", "sendPerformanceData: $json")
    }


    //获取电池电量
    private fun getBatteryLevel(context: Context): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1 // 获取失败返回-1
        }
    }


    // 获取内存使用情况
    private fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val totalMemory = usedMemory + (runtime.freeMemory() / (1024 * 1024))

        // 获取应用内存信息
        val debugMemoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(debugMemoryInfo)
        val pssMemory = debugMemoryInfo.getTotalPss() / 1024 // 转换为MB

        return MemoryInfo(usedMemory, totalMemory, pssMemory)
    }

    // 内存信息数据类
    private data class MemoryInfo(
        val usedMemory: Long,//已经申请到并且正在使用的内存
        val totalMemory: Long,//申请到的总内存
        val pssMemory: Int // 实际使用的物理内存(MB)，包括应用占用的内存和共享库占用的内存
    )


    //开始监控
    private fun startMonitoring() {
        if (isMonitoring) {
            return
        }

        isMonitoring = true
        // 开始FPS监控逻辑
        frameCount = 0
        lastFrameTimeNanos = System.nanoTime()
        // 注册Choreographer回调等

        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (isMonitoring) {
                    frameCount++
                    Choreographer.getInstance().postFrameCallback(this)
                }
            }
        }

        Choreographer.getInstance().postFrameCallback(frameCallback!!)
    }


    //开始对长时间的事件进行监控
    private fun startLongMonitoring() {
        if (isLongMonitoring) {
            return
        }

        isLongMonitoring = true
        // 开始FPS监控逻辑
        frameLongCount = 0
        lastFrameLongTimeNanos = System.nanoTime()
        // 注册Choreographer回调等

        frameLongCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (isLongMonitoring) {
                    frameLongCount++
                    Choreographer.getInstance().postFrameCallback(this)
                }
            }
        }

        Choreographer.getInstance().postFrameCallback(frameLongCallback!!)
    }


    //停止监控
    private fun stopMonitoring(view: View) {
        if (!isMonitoring) {
            return
        }
        isMonitoring = false
        // 停止Choreographer回调
        frameCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
        }
        frameCallback = null

        // 停止FPS监控并计算结果
        // 计算FPS
        val durationNanos = System.nanoTime() - lastFrameTimeNanos
        val durationSeconds = durationNanos / 1_000_000_000.0
        operationFps = if (durationSeconds > 0) {
            frameCount / durationSeconds
        } else 0.0
        viewResourceId = getViewIdentifier(view)
        sendPerformanceData(view.context)
    }



    //停止对长时间事件的监控
    private fun stopLongMonitoring(view: View,activityName:String?=null) {
        if (!isLongMonitoring) {
            return
        }
        isLongMonitoring = false
        // 停止Choreographer回调
        frameLongCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
        }
        frameLongCallback = null

        // 停止FPS监控并计算结果
        // 计算FPS
        val durationNanos = System.nanoTime() - lastFrameLongTimeNanos
        val durationSeconds = durationNanos / 1_000_000_000.0
        operationFps = if (durationSeconds > 0) {
            frameLongCount / durationSeconds
        } else 0.0


        if (activityName==null){
            viewResourceId = getViewIdentifier(view)
        }else{
            viewResourceId=activityName
        }

        sendPerformanceData(view.context)
    }


    // 获取视图标识的辅助方法
    private fun getViewIdentifier(view: View): String {
        return try {
            if (view.id != View.NO_ID) {
                view.resources.getResourceEntryName(view.id)
            } else {
                // 尝试从父视图获取位置信息
                val parent = view.parent
                if (parent is ViewGroup) {
                    val index = parent.indexOfChild(view)
                    "${view.javaClass.simpleName}_$index"
                } else {
                    view.javaClass.simpleName
                }
            }
        } catch (e: Exception) {
            "unknown_view"
        }
    }


    //监控执行点击事件时的FPS（可以是按钮，也可以是列表的子项）
    fun wrapClickListener(listener: (View) -> Unit): (View) -> Unit {

        return { v ->
            startMonitoring()
            listener(v)
            v.post {
                stopMonitoring(v)
            }
        }
    }
    // 监控长按事件时的FPS
    fun wrapLongClickListener(listener: (View) -> Boolean): (View) -> Boolean {
        return { v ->
            startMonitoring()
            val result = listener(v)
            v.post {
                stopMonitoring(v)
            }
            result
        }
    }
    /* 使用方式
    button.setOnLongClickListener(
    PerformanceMonitor.wrapLongClickListener { v ->
        // 长按处理逻辑
        Toast.makeText(v.context, "长按了按钮", Toast.LENGTH_SHORT).show()
        true // 返回true表示消费了长按事件
    }
    )*/



    //监控ScrollView滚动列表时的FPS
    fun wrapScrollViewListener(scrollView: ScrollView) {
        // 记录是否正在滚动
        var isScrolling = false
        val scrollEndRunnable = Runnable {
            if (isScrolling) {
                isScrolling = false
                stopMonitoring(scrollView)
            }
        }
        scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!isScrolling) {
                isScrolling = true
                startMonitoring()
            }

            // 延迟停止监控，确保捕获整个滚动过程
            scrollView.removeCallbacks(scrollEndRunnable)
            scrollView.postDelayed(scrollEndRunnable, 100)
        }
    }

    // 添加对 RecyclerView的监控
    fun wrapRecyclerViewListener(recyclerView: RecyclerView) {
        var isScrolling = false

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // 用户开始拖拽滚动
                        if (!isScrolling) {
                            isScrolling = true
                            startMonitoring()
                        }
                    }

                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // 滚动完全停止
                        if (isScrolling) {
                            isScrolling = false
                            recyclerView.post {
                                stopMonitoring(recyclerView)
                            }
                        }
                    }
                }
            }
        })
    }


    // 监控ViewPager2页面跳转时的性能
    fun wrapPageChangeListener(viewPager: ViewPager2) {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                startMonitoring()
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // 页面跳转完成，停止监控
                    viewPager.post {
                        stopMonitoring(viewPager)
                    }
                }
            }
        })
    }

    //监控碎片创建到销毁时的性能
    fun wrapFragmentLifecycleCallbacks(fragment: androidx.fragment.app.Fragment) {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                startLongMonitoring()
            }

            // 重写onPause方法，当生命周期所有者暂停时调用
            override fun onPause(owner: LifecycleOwner) {
                // 停止长时间监控fragment的视图
                stopLongMonitoring(fragment.requireView(),fragment.javaClass.simpleName)
            }


        })
    }
    /* class MyFragment : Fragment() {
         override fun onCreate(savedInstanceState: Bundle?) {
             super.onCreate(savedInstanceState)

             // 在Fragment中调用此方法来监控性能
             PerformanceMonitor.wrapFragmentLifecycleCallbacks(this)
         }

         // ... 其他Fragment代码
     }
 */

    //监控活动从创建到销毁时的性能
    fun wrapActivityLifecycleCallbacks(activity: AppCompatActivity) {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver,LifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                Log.d("PerformanceMonitor", "执行开始监控")
                startLongMonitoring()
            }
            override fun onPause(owner: LifecycleOwner) {
                Log.d("PerformanceMonitor", "执行结束监控")
                stopLongMonitoring(activity.window.decorView,activity.javaClass.simpleName)
            }
        })
    }
    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)

         // 监控Activity生命周期性能
         PerformanceMonitor.wrapActivityLifecycleCallbacks(this)

         // 其他初始化代码...
         setupViews()
     }*/

}
