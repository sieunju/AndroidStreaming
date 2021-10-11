package com.hmju.streaming.constants


enum class Header(val type : Char) {
    RELIABLE('a'),
    AUTH('b'),
    VIDEO('c'),
    MSG('d')
}

object Constants {
    const val HOST = "172.30.1.46"
    //    const val HOST = "192.168.10.33"
    const val PORT = 60000
    //    const val MAX_BUF = 65_535
    const val MAX_BUF = 6_000
    const val SEP = "|" // 구분자
    const val RELIABLE_UID = "-"
    const val MAX_VIDEO_SIZE = 5_500
    const val VIDEO = "v"
    const val AUDIO = "a"
}

object ClientType {
    const val CASTER = 10 // 방송하는 사람
    const val VIEWER = 20 // 방송을 보는 사람
}