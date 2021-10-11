package com.hmju.streaming.caster.manager

import com.hmju.streaming.model.base.ReliablePacket

/**
 * Description : 방송 송출자 SendManager Service
 *
 * Created by juhongmin on 10/10/21
 */
interface CastSendManager {

    /**
     * 방송 송출자 시작 처리 함수
     */
    fun start()

    /**
     * 패킷이 정상적으로 서버한테 갔으면 리턴 받는 패킷에 대해
     * 처리하는 함수
     */
    fun onReliablePacket(packet: ReliablePacket)

    /**
     * 메모리 해제 처리 함수.
     */
    fun release()
}