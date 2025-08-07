package com.example.pmpsdk.errorSDK
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pmpsdk.databinding.ActivityFriendlyBinding

class FriendlyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendlyBinding
    private val handler = Handler(Looper.getMainLooper())
    private var countdownSeconds = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFriendlyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val message = intent.getStringExtra("message") ?: "该功能暂不可用"
        binding.messageText.text = message
        updateCountdownText()   //更新倒计时文本
        startCountdown()    //启动倒计时
        binding.returnButton.setOnClickListener {
            finish()    //点击返回按钮立即返回之前的页面
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCountdownText() {
        binding.restTime.text = "${countdownSeconds+1}秒后自动返回"
    }

    private fun startCountdown() {
        val countdownRunnable = object : Runnable {
            override fun run() {
                if (countdownSeconds > 0) {
                    countdownSeconds--
                    updateCountdownText()
                    handler.postDelayed(this, 1000)
                } else {
                    finish()  //倒计时结束，关闭当前Activity
                }
            }
        }
        handler.postDelayed(countdownRunnable, 0)  //立即开始倒计时
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) //防止内存泄漏
    }
}