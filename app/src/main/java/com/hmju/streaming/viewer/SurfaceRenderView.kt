package com.hmju.streaming.viewer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Base64
import android.view.SurfaceControlViewHost
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.hmju.streaming.model.video.VideoPacket
import com.hmju.streaming.utility.JLogger
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * Description : 뷰어 렌더링 View Class
 *
 * Created by juhongmin on 10/10/21
 */
class SurfaceRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val surfaceHolder: SurfaceHolder by lazy { holder }
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private val videoStreamQue: ConcurrentLinkedQueue<String> by lazy { ConcurrentLinkedQueue<String>() }
    private var tempStreamArr: Array<String>? = null
    private var renderObservable: Disposable? = null
    private val paint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var currentTime : Long = 0

    init {
        setZOrderOnTop(true)
        surfaceHolder.apply {
            addCallback(this@SurfaceRenderView)
            setFormat(PixelFormat.TRANSPARENT)
        }

        if(isInEditMode) {
            setBackgroundColor(Color.BLACK)
        }
    }

    fun startRender(){
        if(renderObservable != null) return

        renderObservable = Flowable.interval(16,TimeUnit.MILLISECONDS, Schedulers.single())
            .onBackpressureBuffer()
            .map {
                return@map if(videoStreamQue.size > 0) {
                    videoStreamQue.poll()
                } else {
                    ""
                }
            }.subscribe({ videoSource ->
                if (!videoSource.isNullOrEmpty()) {
                    try {
                        val decodedByte = Base64.decode(videoSource, Base64.NO_WRAP)
                        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                        val resizeBitmap =
                            Bitmap.createScaledBitmap(bitmap, viewWidth, viewWidth, true)
                        if (resizeBitmap != null) {
                            val canvas = surfaceHolder.lockCanvas()
                            try {
                                JLogger.d("캡처를 그립니다. ${resizeBitmap.rowBytes}")
                                canvas.drawBitmap(resizeBitmap, 0F, 0F, paint)
                                resizeBitmap.recycle()
                                bitmap.recycle()
                            } catch (ex: Exception) {
                                JLogger.e("여기서????? ${ex}")
                            } finally {
                                surfaceHolder.unlockCanvasAndPost(canvas)
                            }
                        }
                    } catch (ex : Exception) {
                        JLogger.e("비트맵 만드는 도중 에러... ${ex}")
                        // 지금까지 받은 패킷들 초기화
                        currentTime = 0
                        tempStreamArr = null
                    }
                }
            },{
                JLogger.e("Flowable Error ${it}")
            })
    }

    /**
     * 비디오 프레임 추가 처리함수
     * 1 프레임의 비트맵을 다 받은 경우 videoStreamQue 추가
     *
     * @param packet 비디오 패킷
     */
    fun addVideoFrame(packet: VideoPacket) {
        tempStreamArr?.let { arr ->
            if (arr.size > packet.currPos) {
                if(arr[packet.currPos].isEmpty()) {
                    arr[packet.currPos] = packet.source
                }
            }

            var isFull = true
            for (idx in arr.indices) {
                // 하나라도 빈공간이 있는 경우
                if (arr[idx].isEmpty()) {
                    isFull = false
                    break
                }
            }

            // 가득 찬경우 videoStreamQue 저장
            if (isFull) {
                val strBuilder = StringBuffer()
                arr.forEach {
                    strBuilder.append(it)
                }
                videoStreamQue.offer(strBuilder.toString())
                // 비디오 스트림 초기화.
                tempStreamArr = null
                JLogger.d(" 스트림 사이즈 ${videoStreamQue.size}")
            }
        } ?: run {
            if(currentTime != packet.time) {
                JLogger.d("새로운 스트림을 받습니다. ${packet.time} ${packet.reliableUid}")
                tempStreamArr = Array(packet.maxSize) {
                    if (it == packet.currPos) {
                        packet.source
                    } else {
                        ""
                    }
                }
                currentTime = packet.time
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        JLogger.d("surfaceCreated")
        startRender()
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after [.surfaceCreated].
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new [PixelFormat] of the surface.
     * @param width The new width of the surface.
     * @param height The new height of the surface.
     */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        try {
            renderObservable?.dispose()
            renderObservable = null
        } catch (ex : Exception) {

        }
    }
}