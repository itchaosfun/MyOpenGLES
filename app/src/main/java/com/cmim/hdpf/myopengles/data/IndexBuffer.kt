package com.cmim.hdpf.myopengles.data

import android.opengl.GLES20
import com.cmim.hdpf.myopengles.BYTES_PER_SHORT
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IndexBuffer {

    private var bufferId = 0

    constructor(vertexData: ShortArray) {
        //allocate a buffer
        val buffers = IntArray(1)
        GLES20.glGenBuffers(buffers.size, buffers, 0)
        if (buffers[0] == 0) {
            throw RuntimeException("Could not create a new vertex buffer object.")
        }
        bufferId = buffers[0]

        //bind to the buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[0])

        //transfer data to native memory
        val indexArray = ByteBuffer.allocateDirect(vertexData.size * BYTES_PER_SHORT)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(vertexData)
        indexArray.position(0)

        //transfer data from native memory to the GPU buffer
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            indexArray.capacity() * BYTES_PER_SHORT,
            indexArray,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0)
    }

    fun getBufferId() = bufferId
}
