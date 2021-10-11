package com.hmju.streaming.model.video

import com.hmju.streaming.constants.Constants
import com.hmju.streaming.constants.Header
import com.hmju.streaming.model.base.BasePacket
import com.hmju.streaming.model.base.splitIdxInt
import com.hmju.streaming.model.base.splitIdxLong
import com.hmju.streaming.model.base.splitIdxString
import com.hmju.streaming.utility.simpleStrBuilder

/**
 * Description : 비디오 패킷
 *
 * Created by juhongmin on 10/10/21
 */
data class VideoPacket(
    val time: Long = 0, // 프레임 시간
    val currPos: Int = 0, // Source 나눴을때 위치값
    val maxSize: Int = 0, // Source 최대값
    val source: String = "" // Video Source
) : BasePacket(Header.VIDEO) {

    init {
        initUid()
    }

    // c|v-1632566803229-1-3|1632566803229|1|3|qwerqwerqwer
    // {Header}|{Uid}|{time}|{currPos}|{maxSize}|{source}
    override fun encode() = simpleStrBuilder(super.encode(), time, currPos, maxSize, source)

    companion object {
        @JvmStatic
        fun decode(msg: String): VideoPacket {
            val split = msg.split(Constants.SEP)
            return VideoPacket(
                split.splitIdxLong(2),
                split.splitIdxInt(3),
                split.splitIdxInt(4),
                split.splitIdxString(5)
            )
        }
    }
}