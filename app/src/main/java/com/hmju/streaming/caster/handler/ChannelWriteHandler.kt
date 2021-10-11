package com.hmju.streaming.caster.handler

import com.hmju.streaming.constants.Constants
import com.hmju.streaming.model.base.BasePacket
import com.hmju.streaming.utility.JLogger
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.channel.socket.DatagramPacket
import io.netty.util.ReferenceCountUtil
import java.net.InetSocketAddress

/**
 * Description : 데이터가 큰 스트림을 보내는 경우 ByteBuf 에서 OOM 발생
 * 재활용 하는 ByteBuf를 만들어서 처리하는 함수.
 *
 * Created by juhongmin on 10/10/21
 */
class ChannelWriteHandler : ChannelOutboundHandlerAdapter() {

    private var sender: InetSocketAddress? = null
    private var recipient: InetSocketAddress? = null
    private val recycleBuf = Unpooled.buffer(Constants.MAX_BUF)

    /**
     * Calls [ChannelHandlerContext.write] to forward
     * to the next [ChannelWriteHandler] in the [ChannelPipeline].
     *
     * Sub-classes may override this method to change behavior.
     */
    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if (ctx == null || msg == null) return

        try {
            if (sender == null) sender = ctx.channel().localAddress() as InetSocketAddress
            if (recipient == null) recipient = ctx.channel().remoteAddress() as InetSocketAddress

            if (msg is BasePacket) {
                // JLogger.d("Write Packet ${msg.encode()}")
                recycleBuf.retain()
                recycleBuf.resetWriterIndex()
                recycleBuf.writeBytes(msg.encode().toByteArray())
                val packet = DatagramPacket(recycleBuf, recipient, sender)
                ctx.write(packet)
                ReferenceCountUtil.release(msg)
            } else {
                throw ClassCastException("No BasePacket!!")
            }
        } catch (ex: Exception) {
            JLogger.e("ChannelOutHandler Write Error $ex")
        }
    }

    /**
     * Calls [ChannelHandlerContext.flush] to forward
     * to the next [ChannelOutboundHandler] in the [ChannelPipeline].
     *
     * Sub-classes may override this method to change behavior.
     */
    override fun flush(ctx: ChannelHandlerContext?) {
        super.flush(ctx)
    }
}