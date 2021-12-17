package com.cmim.hdpf.myopengles.`object`

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.util.Log
import com.cmim.hdpf.myopengles.data.IndexBuffer
import com.cmim.hdpf.myopengles.data.VertexBuffer
import com.cmim.hdpf.myopengles.program.HeightmapShaderProgram
import java.lang.RuntimeException

class Heightmap {
    private val TAG = "Heightmap"

    private val POSITION_COMPONENT_COUNT = 3
    private var width = 0
    private var height = 0
    private var numElements = 0
    private lateinit var vertexBuffer: VertexBuffer
    private lateinit var indexBuffer: IndexBuffer

    constructor(bitmap: Bitmap) {
        width = bitmap.width
        height = bitmap.height
        Log.i(TAG, "width = $width, height = $height")
        if (width * height > 65536) {
            throw RuntimeException("Heightmap is too large for the index buffer.")
        }
        numElements = calculateNumElements()
        vertexBuffer = VertexBuffer(loadBitmapData(bitmap))
        indexBuffer = IndexBuffer(createIndexData())

    }

    private fun createIndexData(): ShortArray {
        val indexData = ShortArray(numElements)
        var offset = 0
        for (row in 0 until height - 1) {
            for (col in 0 until width - 1) {
                val topLeftIndexNum = (row * width + col).toShort()
                val topRightIndexNum = (row * width + col + 1).toShort()
                val bottomLeftIndexNum = ((row + 1) * width + col).toShort()
                val bottomRightIndexNum = ((row + 1) * width + col + 1).toShort()

                indexData[offset++] = topLeftIndexNum
                indexData[offset++] = bottomLeftIndexNum
                indexData[offset++] = topRightIndexNum

                indexData[offset++] = topRightIndexNum
                indexData[offset++] = bottomLeftIndexNum
                indexData[offset++] = bottomRightIndexNum
            }
        }
        return indexData
    }

    private fun loadBitmapData(bitmap: Bitmap): FloatArray {
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()

        val heightmapVertices = FloatArray(width * height * POSITION_COMPONENT_COUNT)
        var offset = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                val xPosition = (col.toFloat() / (width - 1).toFloat()) - 0.5f
                val yPosition = Color.red(pixels[(row * height) + col]).toFloat() / 255f
                val zPosition = (row.toFloat() / (height - 1).toFloat()) - 0.5f
                heightmapVertices[offset++] = xPosition
                heightmapVertices[offset++] = yPosition
                heightmapVertices[offset++] = zPosition
            }
        }

        return heightmapVertices
    }

    private fun calculateNumElements(): Int {
        return (width - 1) * (height - 1) * 2 * 3
    }

    fun bindData(heightmapShaderProgram: HeightmapShaderProgram) {
        vertexBuffer.setVertexAttributePointer(
            0,
            heightmapShaderProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            0
        )
    }

    fun draw() {
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId())
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, GLES20.GL_UNSIGNED_SHORT, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}