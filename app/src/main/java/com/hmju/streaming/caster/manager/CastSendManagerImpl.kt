package com.hmju.streaming.caster.manager

import androidx.camera.view.PreviewView
import com.hmju.streaming.model.base.ReliablePacket
import com.hmju.streaming.model.video.VideoPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * Description : 방송 송출자 SendManager
 *
 * Created by juhongmin on 10/10/21
 */
class CastSendManagerImpl(
    private val clientChannel: NioDatagramChannel,
    private val previewView: PreviewView
) : CastSendManager {

    private val videoStreamMap : ConcurrentHashMap<String,VideoPacket> by lazy { ConcurrentHashMap() }

    /**
     * 방송 송출자 시작 처리 함수
     */
    override fun start() {
    }

    /**
     * 패킷이 정상적으로 서버한테 갔으면 리턴 받는 패킷에 대해
     * 처리하는 함수
     */
    override fun onReliablePacket(packet: ReliablePacket) {

    }

    /**
     * 메모리 해제 처리 함수.
     */
    override fun release() {

    }
}