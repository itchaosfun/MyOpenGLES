package com.cmim.hdpf.myopengles.`object`

import com.cmim.hdpf.myopengles.data.VertexArray
import com.cmim.hdpf.myopengles.geometry.Cylinder
import com.cmim.hdpf.myopengles.geometry.Point
import com.cmim.hdpf.myopengles.program.ColorShaderProgram

class Puck {

    private val POSITION_COMPONENT_COUNT = 3

    var radius = 0.0f
    var height = 0.0f

    private var vertexArray: VertexArray? = null
    private var drawList = mutableListOf<ObjectBuilder.Companion.DrawCommand>()

    constructor(radius: Float, height: Float, numPointsAroundPuck: Int) {
        val generatedData = ObjectBuilder.createPuck(
            Cylinder(
                Point(0f, 0f, 0f), radius, height
            ), numPointsAroundPuck
        )
        this.radius = radius
        this.height = height
        vertexArray = VertexArray(generatedData.vertexData)
        drawList = generatedData.drawList
    }

    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray?.setVertexAttribPointer(
            0, colorProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT, 0
        )
    }

    fun draw() {
        drawList.forEach {
            it.draw()
        }
    }
}