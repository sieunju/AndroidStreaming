package com.hmju.streaming.caster.camera

/**
 * Description : 카메라 제공자 클래스
 *
 * Created by juhongmin on 10/10/21
 */
interface CameraProvider {
    fun open()
    fun switchCamera()
}