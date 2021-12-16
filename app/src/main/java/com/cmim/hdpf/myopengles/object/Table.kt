package com.cmim.hdpf.myopengles.`object`

import android.opengl.GLES20
import com.cmim.hdpf.myopengles.BYTES_PER_FLOAT
import com.cmim.hdpf.myopengles.data.VertexArray
import com.cmim.hdpf.myopengles.program.AirHockeyTextureShaderProgram

class Table {
    //位置分量计数
    private val POSITION_COMPONENT_COUNT = 2

    //纹理分量计数
    private val TEXTURE_COORDINATE_COMPONENT_COUNT = 2

    //跨距
    private val STRIDE =
        (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATE_COMPONENT_COUNT) * BYTES_PER_FLOAT

    private val VERTEX_DATA = floatArrayOf(
        //Order of coordinate:X,Y,S,T
        //三角形扇
        0f, 0f, 0.5f, 0.5f,
        -0.5f, -0.8f, 0f, 0.9f,
        0.5f, -0.8f, 1f, 0.9f,
        0.5f, 0.8f, 1f, 0.1f,
        -0.5f, 0.8f, 0f, 0.1f,
        -0.5f, -0.8f, 0f, 0.9f
    )

    private lateinit var vertexArray: VertexArray

    constructor() {
        vertexArray = VertexArray(VERTEX_DATA)
    }

    /**
     * 把顶点数组绑定在一个着色器程序上
     */
    fun bindData(textureProgram: AirHockeyTextureShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            textureProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )

        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            textureProgram.getTextureCoordinatesAttributeLocation(),
            TEXTURE_COORDINATE_COMPONENT_COUNT,
            STRIDE
        )
    }

    fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
    }
}