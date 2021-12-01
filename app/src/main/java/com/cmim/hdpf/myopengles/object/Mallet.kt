package com.cmim.hdpf.myopengles.`object`

import android.opengl.GLES20
import com.cmim.hdpf.myopengles.BYTES_PER_FLOAT
import com.cmim.hdpf.myopengles.data.VertexArray
import com.cmim.hdpf.myopengles.program.ColorShaderProgram

class Mallet {
    //位置分量计数
    private val POSITION_COMPONENT_COUNT = 2

    //纹理分量计数
    private val COLOR_COMPONENT_COUNT = 3

    //跨距
    private val STRIDE =
        (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT

    private val VERTEX_DATA = floatArrayOf(
        //Order of coordinate:X,Y,R,G,B
        0f, -0.4f, 0f, 0f, 1f,
        0f, 0.4f, 1f, 0f, 0f
    )

    private lateinit var vertexArray: VertexArray

    constructor() {
        vertexArray = VertexArray(VERTEX_DATA)
    }

    /**
     * 把顶点数组绑定在一个着色器程序上
     */
    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )

        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            colorProgram.getColorAttributeLocation(),
            COLOR_COMPONENT_COUNT,
            STRIDE
        )
    }

    fun draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 2)
    }
}