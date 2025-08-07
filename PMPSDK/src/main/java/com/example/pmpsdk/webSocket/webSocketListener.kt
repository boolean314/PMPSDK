package com.example.pmpsdk.webSocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class webSocketListener : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private val handler= Handler(Looper.getMainLooper())
    private val reconnectInterval=5000L
    private val maxReconnectAttempts=5
    private var reconnectAttempts=0
    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket=webSocket
        Log.d("WS","连接已建立")

    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WS","收到消息：$text")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(code,reason)
        when(code){
            1000-> Log.d("WS","正常关闭")
            else->{
                Log.d("Ws", reason)
                scheduleReconnect()
            }
        }
        Log.d("WS"," 错误：$reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d("WS","错误：${t.message}")
        scheduleReconnect()
    }
    private fun scheduleReconnect(){
        if(reconnectAttempts>=maxReconnectAttempts){
            Log.d("Ws","重连次数以达到上限")
            return
        }
        handler.postDelayed({
            Log.d("WS","正在尝试重连...")
            webSocket?.cancel()// 取消旧连接
            val client= OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build()
            val request= Request.Builder()
                .url("ws://echo.websocket.org")
                .build()
            val newWebSocket=client.newWebSocket(request,this)
            webSocket=newWebSocket

        },reconnectInterval)
    }

}