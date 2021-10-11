package com.hmju.streaming.utility

import com.hmju.streaming.constants.Constants

/**
 * 구분자를 추가하여 간단하게 문자열 리턴처리하는 함수
 * @param values Array<Any>
 * @param sep 구분자
 *
 */
fun simpleStrBuilder(vararg values : Any, sep : String = Constants.SEP) : String {
    val str = StringBuilder()
    for(idx in values.indices) {
        str.append(values[idx])
        if(idx < values.lastIndex) {
            str.append(sep)
        }
    }
    return str.toString()
}