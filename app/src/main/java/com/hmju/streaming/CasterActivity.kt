package com.hmju.streaming

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hmju.streaming.caster.CasterClient
import com.hmju.streaming.caster.camera.Camera2Provider
import com.hmju.streaming.caster.camera.CameraProvider
import com.hmju.streaming.constants.Constants

class CasterActivity : AppCompatActivity() {
    private val casterClient : CasterClient by lazy {
        CasterClient().apply {
            setAuthKey("imcaster")
            setServerAddress(Constants.HOST,Constants.PORT)
        }
    }
    private val cameraProvider: Camera2Provider by lazy {
        Camera2Provider(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caster)
        cameraProvider.setTextureView(findViewById(R.id.textureView))
        cameraProvider.open()
    }
}