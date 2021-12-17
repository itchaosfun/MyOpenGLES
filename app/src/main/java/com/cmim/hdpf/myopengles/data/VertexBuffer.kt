package com.cmim.hdpf.myopengles.data

import android.opengl.GLES20
import com.cmim.hdpf.myopengles.BYTES_PER_FLOAT
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VertexBuffer {
    private var bufferId = 0

    constructor(vertexData: FloatArray) {
        //allocate a buffer
        val buffers = IntArray(1)
        GLES20.glGenBuffers(buffers.size, buffers, 0)
        if (buffers[0] == 0) {
            throw RuntimeException("Could not create a new vertex buffer object.")
        }
        bufferId = buffers[0]

        //bind to the buffer
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0])

        //transfer data to native memory
        val vertexArray = ByteBuffer.allocateDirect(vertexData.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexArray.position(0)

        //transfer data from native memory to the GPU buffer
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexArray.capacity() * BYTES_PER_FLOAT,
            vertexArray,
            GLES20.GL_STATIC_DRAW
        )
    }

    fun setVertexAttributePointer(
        dataOffset: Int,
        attributeLocation: Int,
        componentCount: Int,
        stride: Int
    ) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId)
        GLES20.glVertexAttribPointer(
            attributeLocation,
            componentCount,
            GLES20.GL_FLOAT,
            false,
            stride,
            dataOffset
        )
        GLES20.glEnableVertexAttribArray(attributeLocation)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
    }
}