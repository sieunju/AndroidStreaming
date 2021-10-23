package com.hmju.streaming.viewer.converter

import com.hmju.streaming.constants.Constants
import com.hmju.streaming.constants.Header
import com.hmju.streaming.model.base.BasePacket
import com.hmju.streaming.model.base.ReliablePacket
import com.hmju.streaming.model.video.VideoPacket
import com.hmju.streaming.utility.JLogger
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.codec.MessageToMessageCodec
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.marshalling.CompatibleMarshallingDecoder
import java.net.InetSocketAddress

/**
 * Description : 뷰어 Read, Write 코덱 클래스
 *
 * Created by hmju on 2021-10-13
 */
class ViewerPacketCodec(
    private val channel: NioDatagramChannel
    ) : MessageToMessageCodec<DatagramPacket,BasePacket>() {

    private var sender: InetSocketAddress? = null
    private var recipient: InetSocketAddress? = null
    private val recycleBuf = Unpooled.buffer(Constants.MAX_BUF)

    private fun sendReliablePacket(msg: String) {
        val packet = ReliablePacket(msg.split(Constants.SEP)[1])
        channel.writeAndFlush(packet)
    }

    override fun encode(ctx: ChannelHandlerContext?, msg: BasePacket?, out: MutableList<Any>?) {
        if (ctx == null || msg == null || out == null) return
        
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        super.write(ctx, msg, promise)
    }

    override fun decode(ctx: ChannelHandlerContext?, msg: DatagramPacket?, out: MutableList<Any>?) {
        if (ctx == null || msg == null || out == null) return

        // ZeroCopy 할 수 없는지.. 고민
        val bufArray = ByteArray(msg.content().readableBytes())
        msg.content().duplicate().readBytes(bufArray)
        val message = String(bufArray)

        when (message[0]) {
            Header.VIDEO.type -> {
                // Send ReliablePacket
                sendReliablePacket(message)

                out.add(VideoPacket.decode(message))
            }
            else -> {
                JLogger.d("Viewer Other Type ${message}")
            }
        }
    }
}

class ViewerDecoder : MessageToMessageDecoder<DatagramPacket> (){
    override fun decode(ctx: ChannelHandlerContext?, msg: DatagramPacket?, out: MutableList<Any>?) {
    }
}

class ViewerEncoder : MessageToMessageEncoder<BasePacket> (){
    override fun encode(ctx: ChannelHandlerContext?, msg: BasePacket?, out: MutableList<Any>?) {

    }
}