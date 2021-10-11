package com.hmju.streaming.caster.handler

import com.hmju.streaming.caster.manager.CastSendManager
import com.hmju.streaming.constants.Constants
import com.hmju.streaming.constants.Header
import com.hmju.streaming.model.base.ReliablePacket
import com.hmju.streaming.utility.JLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageDecoder

/**
 * Description : 방송 송출자 Receive Handler
 *
 * Created by juhongmin on 10/10/21
 */
class CastInBoundsHandler(
    private val sendManager : CastSendManager
) : MessageToMessageDecoder<DatagramPacket>(){
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
        val bufArray = ByteArray(msg.content().readableBytes())
        msg.content().duplicate().readBytes(bufArray)
        val message = String(bufArray)
        if (message[0] == Header.RELIABLE.type) {
            val packet = ReliablePacket.decode(message)
            if (packet.uid.startsWith(Constants.VIDEO)) {
                sendManager.onReliablePacket(packet)
            } else {
                JLogger.d("CastOtherType $message")
            }
        }
        msg.content().release()
    }
}