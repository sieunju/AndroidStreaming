package com.hmju.streaming.viewer

import com.hmju.streaming.base.Client
import com.hmju.streaming.caster.handler.ChannelWriteHandler
import com.hmju.streaming.constants.ClientType
import com.hmju.streaming.constants.Constants
import com.hmju.streaming.model.auth.AuthPacket
import com.hmju.streaming.model.video.VideoPacket
import com.hmju.streaming.utility.JLogger
import com.hmju.streaming.viewer.converter.DatagramToPacketDecoder
import com.hmju.streaming.viewer.handler.ReceiveVideoHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import java.net.InetSocketAddress

/**
 * Description : 뷰어 클라이언트 클래스
 *
 * Created by juhongmin on 10/10/21
 */
class ViewerClient : Client(), OnReceiveMessage {

    private var renderView: SurfaceRenderView? = null

    /**
     * 렌더링 뷰 셋팅
     */
    fun setRenderView(view: SurfaceRenderView) {
        renderView = view
    }

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
    override fun getType() = ClientType.VIEWER

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

                        ch.pipeline().apply {
                            addFirst("control", object : ChannelInboundHandlerAdapter() {
                                /**
                                 * Calls [ChannelHandlerContext.fireChannelActive] to forward
                                 * to the next [ChannelInboundHandler] in the [ChannelPipeline].
                                 *
                                 * Sub-classes may override this method to change behavior.
                                 */
                                override fun channelActive(ctx: ChannelHandlerContext?) {
                                    super.channelActive(ctx)
                                    if (ctx == null) return

                                    ch.writeAndFlush(AuthPacket(serviceAuthKey, getType()))
                                }
                            })
                            addAfter("control", "decode", DatagramToPacketDecoder(ch))
                            addAfter("decode","video", ReceiveVideoHandler(this@ViewerClient))
                            addLast(ChannelWriteHandler())
                        }
                    }
                })
                channel = connect(InetSocketAddress(host, port)).sync().channel()
            }
        }.onFailure {
            JLogger.e("initClient Error $it")
        }
    }

    /**
     * 빌드 패턴으로 한다.
     */
    override fun build(): ViewerClient {
        if (host.isEmpty() || port == 0) throw IllegalArgumentException("접속할 서버 정보가 유효하지 않습니다.")
        if (serviceAuthKey.isEmpty()) throw IllegalArgumentException("인증 키기가 유효하지 않습니다.")
        if (renderView == null) throw NullPointerException("렌더링 뷰가 없습니다.")
        initClient()
        return this
    }

    override fun onVideoStream(packet: VideoPacket) {
        renderView?.addVideoFrame(packet)
    }
}