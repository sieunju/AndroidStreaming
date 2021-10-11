package com.hmju.streaming.viewer.handler

import com.hmju.streaming.model.video.VideoPacket
import com.hmju.streaming.viewer.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

/**
 * Description : 비디오 패킷 핸들러
 *
 * Created by juhongmin on 10/10/21
 */
class ReceiveVideoHandler(
    private val callback: OnReceiveMessage
) : SimpleChannelInboundHandler<VideoPacket>() {
    /**
     * Is called for each message of type [I].
     *
     * @param ctx           the [ChannelHandlerContext] which this [SimpleChannelInboundHandler]
     * belongs to
     * @param msg           the message to handle
     * @throws Exception    is thrown if an error occurred
     */
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: VideoPacket?) {
        if (ctx == null || msg == null) return
        callback.onVideoStream(msg)
    }
}