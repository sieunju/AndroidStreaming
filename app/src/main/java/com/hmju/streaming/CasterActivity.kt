package com.hmju.streaming

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.hmju.streaming.caster.CasterClient
import com.hmju.streaming.caster.camera.Camera2Provider
import com.hmju.streaming.caster.camera.CameraProvider
import com.hmju.streaming.constants.Constants

class CasterActivity : AppCompatActivity() {

    private val castPreview: PreviewView by lazy { findViewById(R.id.vFinder) }
    private val tvMem : TextView by lazy { findViewById(R.id.tvMem) }

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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // cameraProvider.setTextureView(findViewById(R.id.textureView))
        // cameraProvider.open()
        startCamera()
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(castPreview.surfaceProvider)
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                casterClient.setTextureView(castPreview)
            } catch (exc: Exception) {
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        casterClient.release()
    }
}