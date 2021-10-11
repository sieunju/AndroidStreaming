package com.hmju.streaming

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.hmju.streaming.constants.Constants
import com.hmju.streaming.utility.MemoryMonitoryComponent
import com.hmju.streaming.viewer.SurfaceRenderView
import com.hmju.streaming.viewer.ViewerClient

class ViewerActivity : AppCompatActivity() {

    private val viewerView : SurfaceRenderView by lazy { findViewById(R.id.viewer) }
    private val tvMem : TextView by lazy { findViewById(R.id.tvMem) }
    private lateinit var memMonitory : MemoryMonitoryComponent
    private val viewerClient: ViewerClient by lazy {
        ViewerClient().apply {
            setAuthKey("imViewer")
            setServerAddress(Constants.HOST, Constants.PORT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
        memMonitory = MemoryMonitoryComponent(tvMem)
        viewerClient.setRenderView(viewerView)
        viewerClient.build()
    }

    override fun onDestroy() {
        memMonitory.stop()
        super.onDestroy()
    }
}