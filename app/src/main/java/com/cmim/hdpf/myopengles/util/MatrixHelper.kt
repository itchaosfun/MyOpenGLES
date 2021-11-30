package com.cmim.hdpf.myopengles.util

object MatrixHelper {

    /**
     * @param m 16位的矩阵
     * @param yFovInDegrees 视野的角度
     * @param aspect 屏幕的宽高比
     * @param f 到远处的距离
     * @param n 到近处的距离
     */
    fun perspectiveM(m: FloatArray, yFovInDegrees: Float, aspect: Float, n: Float, f: Float) {
        //角度转弧度
        val angleInRadius: Float = (yFovInDegrees * Math.PI / 180.0).toFloat()
        //焦距 = 1/tan(视野/2) 视野必须小于180
        val a: Float = (1.0 / Math.tan(angleInRadius / 2.0)).toFloat()

        m[0] = a / aspect
        m[1] = 0f
        m[2] = 0f
        m[3] = 0f

        m[4] = 0f
        m[5] = a
        m[6] = 0f
        m[7] = 0f

        m[8] = 0f
        m[9] = 0f
        m[10] = -((f + n) / (f - n))
        m[11] = -1f
        m[12] = 0f
        m[13] = 0f
        m[14] = -((2 * f * n) / (f - n))
        m[15] = 0f
    }
}