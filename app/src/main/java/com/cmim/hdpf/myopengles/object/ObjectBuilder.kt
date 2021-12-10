package com.cmim.hdpf.myopengles.`object`

import android.opengl.GLES20
import com.cmim.hdpf.myopengles.geometry.Circle
import com.cmim.hdpf.myopengles.geometry.Cylinder
import com.cmim.hdpf.myopengles.geometry.Point

class ObjectBuilder {

    //表示一个顶点需要多少浮点数
    private val FLOAT_PER_VERTEX = 3

    //用于保存顶点的数组
    private lateinit var vertexData: FloatArray

    private val drawList = mutableListOf<DrawCommand>()

    //记录数组中下一个顶点的位置
    private var offset = 0

    constructor(sizeInVertices: Int) {
        vertexData = FloatArray(sizeInVertices * FLOAT_PER_VERTEX)
    }

    companion object {

        interface DrawCommand {
            fun draw()
        }

        class GeneratedData(val vertexData: FloatArray, val drawList: MutableList<DrawCommand>)

        /**
         * 圆柱体顶部顶点的数量
         * 圆柱体的顶部，是一个用三角形扇构造的圆；
         * 有一个顶点在圆心，围着圆的每个点都有一个顶点，并且围着圆的第一个顶点要重复两次才能使圆闭合
         */
        private fun sizeOfCircleInVertices(numPoints: Int): Int {
            return 1 + (numPoints + 1)
        }

        /**
         * 圆柱体侧面顶点的数量
         * 圆柱体的侧面是一个卷起来的长发行，由一个三角形带构造，
         * 围着顶部圆的每个点都需要两个顶点，并且前两个顶点要重复两次才能使这个管闭合
         */
        private fun sizeOfOpenCylinderInVertices(numPoints: Int): Int {
            return (numPoints + 1) * 2
        }

        /**
         * 生成一个冰球
         */
        fun createPuck(puck: Cylinder, numPoints: Int): GeneratedData {
            val size = sizeOfCircleInVertices(numPoints) + sizeOfOpenCylinderInVertices(numPoints)
            val builder = ObjectBuilder(size)
            val puckTop = Circle(puck.center.translateY(puck.height / 2f), puck.radius)
            builder.appendCircle(puckTop, numPoints)
            builder.appendOpenCylinder(puck, numPoints)

            return builder.build()
        }

        /**
         * 创建一个木槌
         */
        fun createMallet(
            center: Point,
            radius: Float,
            height: Float,
            numPoints: Int
        ): GeneratedData {
            val size =
                sizeOfCircleInVertices(numPoints) * 2 + sizeOfOpenCylinderInVertices(numPoints) * 2
            val builder = ObjectBuilder(size)
            //First, generate the mallet base
            val baseHeight = height * 0.25f

            val baseCircle = Circle(center.translateY(-baseHeight), radius)
            val baseCylinder =
                Cylinder(baseCircle.center.translateY(-baseHeight / 2f), radius, baseHeight)

            builder.appendCircle(baseCircle, numPoints)
            builder.appendOpenCylinder(baseCylinder, numPoints)

            val handleHeight = height * 0.75f
            val handleRadius = radius / 3f

            val handleCircle = Circle(center.translateY(height * 0.5f), handleRadius)
            val handleCylinder = Cylinder(
                handleCircle.center.translateY(-handleHeight / 2f),
                handleRadius,
                handleHeight
            )
            builder.appendCircle(handleCircle,numPoints)
            builder.appendOpenCylinder(handleCylinder,numPoints)

            return builder.build()
        }
    }

    private fun build(): GeneratedData {
        return GeneratedData(vertexData, drawList)
    }

    private fun appendOpenCylinder(cylinder: Cylinder, numPoints: Int) {
        val startVertex = offset / FLOAT_PER_VERTEX
        val numVertices = sizeOfOpenCylinderInVertices(numPoints)
        val yStart = cylinder.center.y - (cylinder.height / 2f)
        val yEnd = cylinder.center.y + (cylinder.height / 2f)

        for (i in 0..numPoints) {
            val angleInRadians = (i.toFloat() / numPoints.toFloat()) * (Math.PI * 2f)
            val xPosition: Float =
                (cylinder.center.x + cylinder.radius * Math.cos(angleInRadians)).toFloat()
            val zPosition: Float =
                (cylinder.center.z + cylinder.radius * Math.sin(angleInRadians)).toFloat()
            vertexData[offset++] = xPosition
            vertexData[offset++] = yStart
            vertexData[offset++] = zPosition

            vertexData[offset++] = xPosition
            vertexData[offset++] = yEnd
            vertexData[offset++] = zPosition
        }

        drawList.add(object : DrawCommand {
            override fun draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertices)
            }
        })
    }

    private fun appendCircle(circle: Circle, numPoints: Int) {

        val startVertex = offset / FLOAT_PER_VERTEX
        val numVertices = sizeOfCircleInVertices(numPoints)

        vertexData[offset++] = circle.center.x
        vertexData[offset++] = circle.center.y
        vertexData[offset++] = circle.center.z

        for (i in 0..numPoints) {
            val angleInRadians = (i.toFloat() / numPoints.toFloat()) * (Math.PI * 2f)
            vertexData[offset++] =
                (circle.center.x + circle.radius * Math.cos(angleInRadians)).toFloat()
            vertexData[offset++] = circle.center.y
            vertexData[offset++] =
                (circle.center.z + circle.radius * Math.sin(angleInRadians)).toFloat()
        }

        drawList.add(object : DrawCommand {
            override fun draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, startVertex, numVertices)
            }
        })
    }


}