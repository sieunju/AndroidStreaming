package com.hmju.streaming.caster.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.util.Size
import android.view.Surface
import androidx.core.content.ContextCompat
import com.hmju.streaming.extensions.multiNullCheck
import com.hmju.streaming.utility.JLogger
import java.lang.Integer.signum
import java.util.*
import java.util.concurrent.Executors

/**
 * Description : 카메라 제공자 클래스
 *
 * Created by juhongmin on 10/11/21
 */
class Camera2Provider(
    private val context: Context
) : CameraProvider {

    private var cameraId: String? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraSize: Size? = null
    private var textureView: AuthFitTextureView? = null
    private var captureRequest: CaptureRequest.Builder? = null



    private val captureSessionCallback = object : CameraCaptureSession.StateCallback() {

        override fun onConfigured(session: CameraCaptureSession) {
            captureSession = session

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {

        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        /**
         * This method is called when an image capture makes partial forward progress; some
         * (but not all) results from an image capture are available.
         *
         * @see .capture
         *
         * @see .captureBurst
         *
         * @see .setRepeatingRequest
         *
         * @see .setRepeatingBurst
         */
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            JLogger.d("onCaptureProgressed ${session}")
        }
    }

    /**
     * set TextureView
     * @param view TextureView
     */
    fun setTextureView(view: AuthFitTextureView) {
        textureView = view
    }


    private fun cameraManager(): CameraManager {
        return context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private fun cameraCharacteristics(manager: CameraManager, isFront: Boolean = true): String? {
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                // 전면
                if (isFront && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes = map?.getOutputSizes(SurfaceTexture::class.java)
                    cameraSize = sizes?.get(0)
                    for (size in sizes!!) {
                        if (size.width > cameraSize!!.width) {
                            cameraSize = size
                        }
                    }
                    return cameraId
                } else if (!isFront && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    // 후면
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes = map?.getOutputSizes(SurfaceTexture::class.java)
                    cameraSize = sizes?.get(0)
                    for (size in sizes!!) {
                        if (size.width > cameraSize!!.width) {
                            cameraSize = size
                        }
                    }
                    return cameraId
                }
            }
        } catch (ex: Exception) {
            return null
        }
        return null
    }


    private fun showCameraPreview() {
        runCatching {
            multiNullCheck(textureView?.surfaceTexture,cameraDevice,cameraSize) { texture,camera,size ->
                texture.setDefaultBufferSize(size.width, size.height)
                val surface = Surface(texture)
                captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(surface)
                    set(
                        CaptureRequest.CONTROL_MODE,
                        CameraMetadata.CONTROL_MODE_AUTO
                    )
                }
                val outConfigs = arrayListOf<OutputConfiguration>()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    outConfigs.add(OutputConfiguration(surface))
                } else {

                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    cameraDevice?.createCaptureSession(
                        SessionConfiguration(
                            SessionConfiguration.SESSION_REGULAR,
                            outConfigs,
                            Executors.newSingleThreadExecutor(),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    captureSession = session
                                    updatePreview()
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {

                                }
                            }
                        )
                    )
                } else {

                }
            }
        }
    }

    private fun updatePreview() {
        multiNullCheck(captureSession, captureRequest) { session, capture ->
            runCatching {
                session.setRepeatingRequest(capture.build(), null, null)
            }
        }
    }

    override fun open() {
        val permissions = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissions != PackageManager.PERMISSION_GRANTED) {
            throw IllegalAccessException("카메라 권한이 거부 상태입니다.")
        }
        val cameraManager = cameraManager()
        cameraId = cameraCharacteristics(cameraManager, true)

        cameraId?.let {
            cameraManager.openCamera(it, object : CameraDevice.StateCallback(){

                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    showCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                }

                override fun onError(camera: CameraDevice, error: Int) {
                }
            }, null)
        }
    }

    override fun switchCamera() {
    }

    internal class CompareSizesByArea : Comparator<Size> {

        // We cast here to ensure the multiplications won't overflow
        override fun compare(lhs: Size, rhs: Size) =
            signum((lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height).toInt())

    }
}