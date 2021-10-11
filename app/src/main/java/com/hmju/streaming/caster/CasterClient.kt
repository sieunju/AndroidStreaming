package com.hmju.streaming.caster

import com.hmju.streaming.base.Client
import com.hmju.streaming.caster.handler.CastInBoundsHandler
import com.hmju.streaming.caster.handler.ChannelWriteHandler
import com.hmju.streaming.caster.manager.CastSendManager
import com.hmju.streaming.caster.manager.CastSendManagerImpl
import com.hmju.streaming.constants.ClientType
import com.hmju.streaming.constants.Constants
import com.hmju.streaming.model.auth.AuthPacket
import com.hmju.streaming.utility.JLogger
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import java.net.InetSocketAddress

/**
 * Description : 방송 송출자 Client
 *
 * Created by juhongmin on 10/10/21
 */
class CasterClient : Client() {

    private var sendManager: CastSendManager? = null

    override fun setServerAddress(host: String, port: Int) {
        this.host
        this.port
    }

    override fun setAuthKey(key: String) {
        serviceAuthKey = key
    }

    /**
     * 클라이언트 방송 송출자 or 뷰어에 따라 타입 리턴
     */
    override fun getType() = ClientType.CASTER

    /**
     * 메모리 해제 처리 함수
     */
    override fun release(): Boolean {
        group?.shutdownGracefully()?.sync()
        channel?.closeFuture()?.sync()
        group = null
        channel = null
        return true
    }

    /**
     * 클라이언트 생성 처리 함수
     */
    private fun initClient() {
        if (group != null) {
            group?.shutdownGracefully()?.sync()
            channel?.closeFuture()?.sync()
        }

        runCatching {
            group = NioEventLoopGroup()
            Bootstrap().apply {
                group(group)
                channel(NioDatagramChannel::class.java)
                option(ChannelOption.SO_BROADCAST, true)
                option(ChannelOption.SO_SNDBUF, Constants.MAX_BUF)
                option(ChannelOption.SO_RCVBUF, Constants.MAX_BUF)
                option(ChannelOption.RCVBUF_ALLOCATOR, FixedRecvByteBufAllocator(Constants.MAX_BUF))
                handler(object : ChannelInitializer<NioDatagramChannel>() {
                    /**
                     * This method will be called once the [Channel] was registered. After the method returns this instance
                     * will be removed from the [ChannelPipeline] of the [Channel].
                     *
                     * @param ch            the [Channel] which was registered.
                     * @throws Exception    is thrown if an error occurs. In that case it will be handled by
                     * [.exceptionCaught] which will by default close
                     * the [Channel].
                     */
                    override fun initChannel(ch: NioDatagramChannel?) {
                        if (ch == null) return

                        sendManager = CastSendManagerImpl(ch)

                        ch.pipeline().apply {
                            addFirst("control", object : ChannelInboundHandlerAdapter() {

                                override fun channelActive(ctx: ChannelHandlerContext?) {
                                    super.channelActive(ctx)
                                    if (ctx == null) return

                                    ch.writeAndFlush(AuthPacket(serviceAuthKey, getType()))
                                    JLogger.d("CasterClient Active ${ctx.channel().localAddress()}")
                                }
                            })
                            addAfter("control", "inbounds", CastInBoundsHandler(sendManager!!))
                            addLast(ChannelWriteHandler())
                        }
                    }
                })
                channel = connect(InetSocketAddress(host, port)).sync().channel()
            }
        }
    }

    /**
     * 빌드 패턴으로 한다.
     */
    override fun build(): CasterClient {
        if (host.isEmpty() || port == 0) throw IllegalArgumentException("접속할 서버 정보가 유효하지 않습니다.")
        if (serviceAuthKey.isEmpty()) throw IllegalArgumentException("인증 키기가 유효하지 않습니다.")

        initClient()
        return this
    }
}