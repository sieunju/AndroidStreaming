package com.hmju.streaming.caster.manager

import android.graphics.Bitmap
import android.util.Base64
import androidx.camera.view.PreviewView
import com.hmju.streaming.constants.Constants
import com.hmju.streaming.model.base.ReliablePacket
import com.hmju.streaming.model.video.VideoPacket
import com.hmju.streaming.utility.JLogger
import io.netty.channel.socket.nio.NioDatagramChannel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.flowables.ConnectableFlowable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * Description : 방송 송출자 SendManager
 *
 * Created by juhongmin on 10/10/21
 */
class CastSendManagerImpl(
    private val clientChannel: NioDatagramChannel,
    private val previewView: PreviewView
) : CastSendManager {

    private val WRITE_DELAY = 100L
    private val CAPTURE_DELAY = 1L
    private val videoStreamMap: ConcurrentHashMap<String, VideoPacket> by lazy { ConcurrentHashMap() }
    private val captureBitmapQue: ConcurrentLinkedQueue<String> by lazy { ConcurrentLinkedQueue() }
    private val sendPublish: ConnectableFlowable<Long> by lazy {
        Flowable.interval(
            WRITE_DELAY,
            TimeUnit.MILLISECONDS
        ).onBackpressureBuffer()
            .observeOn(Schedulers.single()).publish()
    }
    private val capturePublish: ConnectableFlowable<Long> by lazy {
        Flowable.interval(
            CAPTURE_DELAY,
            TimeUnit.MILLISECONDS
        ).subscribeOn(AndroidSchedulers.mainThread())
            .onBackpressureBuffer()
            .observeOn(Schedulers.single()).publish()
    }
    private var sendDisposable: Disposable? = null
    private var captureDisposable: Disposable? = null
    private var cntTime = 0L

    /**
     * 비디오 스트림 맵에 추가 처리함수.
     */
    private fun addVideoStreamMap() {
        if (captureBitmapQue.size > 0) {
            captureBitmapQue.poll()?.let { videoSource ->
                var maxCnt = (videoSource.length / Constants.MAX_VIDEO_SIZE)
                if (videoSource.length % Constants.MAX_VIDEO_SIZE != 0) {
                    maxCnt++
                }
                for (idx in 0 until maxCnt) {
                    val startIdx = (idx * Constants.MAX_VIDEO_SIZE)
                    var endIdx = startIdx + Constants.MAX_VIDEO_SIZE
                    if (endIdx > videoSource.length) {
                        endIdx = videoSource.length
                    }
                    val sliceText =
                        videoSource.substring(startIdx, endIdx)
                    val videoPacket = VideoPacket(
                        time = cntTime,
                        currPos = idx,
                        maxSize = maxCnt,
                        source = sliceText
                    )
                    JLogger.d("VideoPacket ${videoPacket.reliableUid}")
                    videoStreamMap[videoPacket.reliableUid] = videoPacket
                }
                cntTime++
            }
        }
    }

    /**
     * 방송 송출자 시작 처리 함수
     */
    override fun start() {
        // Disposable 해제.
        release()

        sendDisposable = sendPublish.subscribe({
            runCatching {
                JLogger.d("CastSendThread ${Thread.currentThread()}")
                if (videoStreamMap.size > 0) {
                    val keys = videoStreamMap.keys
                    keys.forEach { uid ->
                        videoStreamMap.get(uid)?.let {
                            clientChannel.writeAndFlush(it)
                        }
                    }
                } else {
                    addVideoStreamMap()
                }
            }
        }, {
            JLogger.e("SendPublish Error $it")
        })

        // 일단 프리뷰 캡처 로직 SendManager 에서 처리 나중에 View 에서 처리할 예정
        captureDisposable = capturePublish.map {
            previewView.bitmap?.let { bitmap ->
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val b = baos.toByteArray()
                val baseStr = Base64.encodeToString(b, Base64.NO_WRAP)
                bitmap.recycle()
                return@map baseStr
            } ?: run {
                return@map ""
            }
        }.subscribe({
            if (it.isNotEmpty()) {
                // 메모리상 20개씩 관리함.
                if (captureBitmapQue.size > 20) {
                    captureBitmapQue.poll()
                }
                captureBitmapQue.offer(it)
            }
        }, {
            JLogger.e("CapturePublish Error $it")
        })
    }


    /**
     * 패킷이 정상적으로 서버한테 갔으면 리턴 받는 패킷에 대해
     * 처리하는 함수
     */
    override fun onReliablePacket(packet: ReliablePacket) {
        JLogger.d("Remove Uid ${packet.uid}")
        videoStreamMap.remove(packet.uid)
    }

    /**
     * 메모리 해제 및 쓰레드 동작 해제 처리함수.
     */
    override fun release() {
        if (sendDisposable != null) {
            sendDisposable?.dispose()
            sendDisposable = null
        }

        if (captureDisposable != null) {
            captureDisposable?.dispose()
            captureDisposable = null
        }
    }
}