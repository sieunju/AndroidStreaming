package com.hmju.streaming.utility

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import java.text.NumberFormat

/**
 * Description : 메모리 모니터링 컴포넌트
 *
 * Created by juhongmin on 10/10/21
 */
class MemoryMonitoryComponent(
    private val textView : TextView
) : Handler(Looper.getMainLooper()) {

    private val DELAY = 1000L

    fun start(){
        sendEmptyMessage(0)
    }

    fun sendMonitory(){
        sendEmptyMessageDelayed(0,DELAY)
    }

    fun stop(){
        removeMessages(0)
    }

    /**
     * Subclasses must implement this to receive messages.
     */
    override fun handleMessage(msg: Message) {
        try {
            Runtime.getRuntime().run {
                val usedMemKB = (totalMemory() - freeMemory()) / 1024
                val maxMemKB = maxMemory() / 1024
                val usedMemStr = NumberFormat.getInstance().format(usedMemKB)
                val maxMemStr = NumberFormat.getInstance().format(maxMemKB)
                textView.text = "Used ${usedMemStr}KB MAX ${maxMemStr}KB"
            }
            sendMonitory()
        } catch (ex : Exception) {

        }
    }
}