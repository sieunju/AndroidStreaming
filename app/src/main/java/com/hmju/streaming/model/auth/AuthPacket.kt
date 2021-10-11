package com.hmju.streaming.model.auth

import com.hmju.streaming.constants.Constants
import com.hmju.streaming.constants.Header
import com.hmju.streaming.model.base.BasePacket
import com.hmju.streaming.model.base.splitIdxInt
import com.hmju.streaming.model.base.splitIdxString
import com.hmju.streaming.utility.simpleStrBuilder

/**
 * Description :
 *
 * Created by juhongmin on 10/10/21
 */
data class AuthPacket(
    val authKey: String,
    val type: Int
) : BasePacket(Header.AUTH) {
    init {
        initUid()
    }

    override fun encode() = simpleStrBuilder(super.encode(),authKey,type)

    companion object {
        @JvmStatic
        fun decode(msg: String): AuthPacket {
            val split = msg.split(Constants.SEP)
            return AuthPacket(
                split.splitIdxString(2),
                split.splitIdxInt(3)
            )
        }
    }
}