package com.cmim.hdpf.myopengles.`object`

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.util.Log
import com.cmim.hdpf.myopengles.BYTES_PER_FLOAT
import com.cmim.hdpf.myopengles.data.IndexBuffer
import com.cmim.hdpf.myopengles.data.VertexBuffer
import com.cmim.hdpf.myopengles.geometry.Geometry
import com.cmim.hdpf.myopengles.geometry.Geometry.Companion.Vector
import com.cmim.hdpf.myopengles.geometry.Point
import com.cmim.hdpf.myopengles.program.HeightmapShaderProgram
import java.lang.RuntimeException

class Heightmap {
    private val TAG = "Heightmap"

    private val POSITION_COMPONENT_COUNT = 3
    private val NORMAL_COMPONENT_COUNT = 3
    private val TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT
    private val STRIDE = (POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT) * BYTES_PER_FLOAT
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

        val heightmapVertices = FloatArray(width * height * TOTAL_COMPONENT_COUNT)
        var offset = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                val point: Point = getPoint(pixels, row, col)
                heightmapVertices[offset++] = point.x
                heightmapVertices[offset++] = point.y
                heightmapVertices[offset++] = point.z

                val top: Point = getPoint(pixels, row - 1, col)
                val left: Point = getPoint(pixels, row, col - 1)
                val right: Point = getPoint(pixels, row, col + 1)
                val bottom: Point = getPoint(pixels, row + 1, col)

                val rightToLeft: Vector = Geometry.vectorBetween(right, left)
                val topToBottom: Vector = Geometry.vectorBetween(top, bottom)
                val normal: Vector = rightToLeft.crossProduct(topToBottom).normalize()

                heightmapVertices[offset++] = normal.x
                heightmapVertices[offset++] = normal.y
                heightmapVertices[offset++] = normal.z
            }
        }

        return heightmapVertices
    }

    private fun getPoint(pixels: IntArray, row: Int, col: Int): Point {
        val x = (col.toFloat() / (width - 1).toFloat()) - 0.5f
        val z = (row.toFloat() / (height - 1).toFloat()) - 0.5f
        val iRow: Int = clamp(row, 0, width - 1)
        val iCol = clamp(col, 0, height - 1)
        val y = Color.red(pixels[iRow * height + iCol]).toFloat() / 255f
        return Point(x, y, z)
    }

    private fun clamp(value: Int, min: Int, max: Int): Int {
        return Math.max(min, Math.min(max, value))
    }

    private fun calculateNumElements(): Int {
        return (width - 1) * (height - 1) * 2 * 3
    }

    fun bindData(heightmapShaderProgram: HeightmapShaderProgram) {
        vertexBuffer.setVertexAttributePointer(
            0,
            heightmapShaderProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )

        vertexBuffer.setVertexAttributePointer(
            POSITION_COMPONENT_COUNT* BYTES_PER_FLOAT,
            heightmapShaderProgram.getNormalAttributeLocation(),
            NORMAL_COMPONENT_COUNT, STRIDE
        )
    }

    fun draw() {
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId())
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, GLES20.GL_UNSIGNED_SHORT, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}