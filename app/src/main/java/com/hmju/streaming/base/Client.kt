package com.hmju.streaming.base

import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup

/**
 * Description : 공통 클라이언트 구조 클래스
 *
 * Created by juhongmin on 10/10/21
 */
abstract class Client {

    protected var group : EventLoopGroup? = null
    protected var channel: Channel? = null
    protected var host = ""
    protected var port = 0
    protected var serviceAuthKey : String = ""

    abstract fun setServerAddress(host : String, port : Int)

    abstract fun setAuthKey(key : String)

    /**
     * 클라이언트 방송 송출자 or 뷰어에 따라 타입 리턴
     */
    abstract fun getType() : Int

    /**
     * 메모리 해제 처리 함수
     */
    abstract fun release() : Boolean

    /**
     * 빌드 패턴으로 한다.
     */
    abstract fun build() : Client
}