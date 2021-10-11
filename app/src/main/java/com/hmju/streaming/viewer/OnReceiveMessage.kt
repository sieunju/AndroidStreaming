package com.hmju.streaming.viewer

import com.hmju.streaming.model.video.VideoPacket

/**
 * Description : Receive Message Interface
 *
 * Created by juhongmin on 10/10/21
 */
interface OnReceiveMessage {
    fun onVideoStream(packet: VideoPacket)
}