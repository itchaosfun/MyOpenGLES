package com.cmim.hdpf.myopengles.`object`

import android.opengl.GLES20
import com.cmim.hdpf.myopengles.data.VertexArray
import com.cmim.hdpf.myopengles.program.SkyboxShaderProgram
import java.nio.ByteBuffer

class Skybox {
    private val POSITION_COMPONENT_COUNT = 3
    private var vertexArray: VertexArray
    private var indexArray: ByteBuffer

    constructor() {
        vertexArray = VertexArray(
            floatArrayOf(
                -1f, 1f, 1f, //(0) top-left near
                1f, 1f, 1f, //(1) top-right near
                -1f, -1f, 1f, //(2) bottom-left near
                1f, -1f, 1f, //(3) bottom-right near
                -1f, 1f, -1f, //(4) top-left far
                1f, 1f, -1f, //(5) top-right far
                -1f, -1f, -1f, //(6) bottom-left far
                1f, -1f, -1f //(7) bottom-right far
            )
        )

        indexArray = ByteBuffer.allocateDirect(6 * 6)
            .put(
                byteArrayOf(
                    //font
                    1, 3, 0,
                    0, 3, 2,
                    //back
                    4, 6, 5,
                    5, 6, 7,
                    //left
                    0, 2, 4,
                    4, 2, 6,
                    //right
                    5, 7, 1,
                    1, 7, 3,
                    //top
                    5, 1, 4,
                    4, 1, 0,
                    //bottom
                    6, 2, 7,
                    7, 2, 3
                )
            )
        indexArray.position(0)
    }

    fun bindData(skyboxProgram: SkyboxShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            skyboxProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            0
        )
    }

    fun draw(){
        /**
         * gl_unsigned_byte 把这个数组解释为无符号字节数
         * 在OpenGL ES 2中，indices需要是无符号字节数（范围0~255的8位整数型）或者无符号短整型数（范围为0~65535的16位整型数）
         * 之前已经把索引数组定义为ByteBuffer了，因此我们需要告诉OpenGL把这个数据理解为无符号字节数据流。
         * Java的字节类型（byte）实际上是有符号的（signed），意味着它的范围是从-128到127，但是只要我们一直使用这个范围的正值部分
         * 就不会有问题。
         */
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,36,GLES20.GL_UNSIGNED_BYTE,indexArray)
    }
}