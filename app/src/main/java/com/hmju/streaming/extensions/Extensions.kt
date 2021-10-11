package com.hmju.streaming.extensions

// MultiPle Null Check.
inline fun <A, B, R> multiNullCheck(a: A?, b: B?, function: (A, B) -> R): R? {
    return if (a != null && b != null) {
        function(a, b)
    } else {
        null
    }
}

// MultiPle Null Check.
inline fun <A, B, C, R> multiNullCheck(a: A?, b: B?, c: C?, function: (A, B, C) -> R): R? {
    return if (a != null && b != null && c != null) {
        function(a, b, c)
    } else {
        null
    }
}