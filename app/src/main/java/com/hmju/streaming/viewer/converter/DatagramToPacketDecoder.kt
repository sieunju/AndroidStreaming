package com.hmju.streaming.viewer.converter

import com.hmju.streaming.constants.Constants
import com.hmju.streaming.constants.Header
import com.hmju.streaming.model.base.ReliablePacket
import com.hmju.streaming.model.video.VideoPacket
import com.hmju.streaming.utility.JLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.codec.MessageToMessageDecoder

/**
 * Description : Server -> Client Decoder Class
 *
 * Created by juhongmin on 10/10/21
 */
class DatagramToPacketDecoder(
    private val channel: NioDatagramChannel
) : MessageToMessageDecoder<DatagramPacket>(){

    private fun sendReliablePacket(msg: String) {
        val packet = ReliablePacket(msg.split(Constants.SEP)[1])
        channel.writeAndFlush(packet)
    }

    /**
     * Decode from one message to an other. This method will be called for each written message that can be handled
     * by this decoder.
     *
     * @param ctx           the [ChannelHandlerContext] which this [MessageToMessageDecoder] belongs to
     * @param msg           the message to decode to an other one
     * @param out           the [List] to which decoded messages should be added
     * @throws Exception    is thrown if an error occurs
     */
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