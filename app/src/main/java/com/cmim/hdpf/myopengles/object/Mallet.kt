package com.cmim.hdpf.myopengles.`object`

import com.cmim.hdpf.myopengles.data.VertexArray
import com.cmim.hdpf.myopengles.geometry.Point
import com.cmim.hdpf.myopengles.program.AirHockeyColorShaderProgram

class Mallet {
    //位置分量计数
    private val POSITION_COMPONENT_COUNT = 3

    var radius = 0.0f
    var height = 0.0f
    private var drawList = mutableListOf<ObjectBuilder.Companion.DrawCommand>()

    private lateinit var vertexArray: VertexArray

    constructor(radius: Float, height: Float, numPositionAroundMallet: Int) {
        val generatedData =
            ObjectBuilder.createMallet(Point(0f, 0f, 0f), radius, height, numPositionAroundMallet)
        this.radius = radius
        this.height = height
        vertexArray = VertexArray(generatedData.vertexData)
        drawList = generatedData.drawList
    }

    /**
     * 把顶点数组绑定在一个着色器程序上
     */
    fun bindData(colorProgram: AirHockeyColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT, 0
        )
    }

    fun draw() {
        drawList.forEach {
            it.draw()
        }
    }
}