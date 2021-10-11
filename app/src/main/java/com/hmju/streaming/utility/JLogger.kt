package com.hmju.streaming.utility

import android.util.Log

class JLogger {
    companion object {
        private const val TAG = "JLogger"

        fun d(msg: String) {
            Log.d("[$TAG]", msg)
        }

        fun d(tag: String, msg: String) {
            Log.d("[$tag]", msg)
        }

        fun e(msg: String) {
            Log.e("[$TAG]", msg)
        }
    }
}