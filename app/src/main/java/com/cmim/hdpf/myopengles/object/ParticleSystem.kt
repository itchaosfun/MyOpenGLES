package com.cmim.hdpf.myopengles.`object`

import android.graphics.Color
import android.opengl.GLES20
import com.cmim.hdpf.myopengles.BYTES_PER_FLOAT
import com.cmim.hdpf.myopengles.data.VertexArray
import com.cmim.hdpf.myopengles.geometry.Geometry.Companion.Vector
import com.cmim.hdpf.myopengles.geometry.Point
import com.cmim.hdpf.myopengles.program.ParticleShaderProgram

class ParticleSystem {
    private val POSITION_COMPONENT_COUNT = 3
    private val COLOR_COMPONENT_COUNT = 3
    private val VECTOR_COMPONENT_COUNT = 3
    private val PARTICLE_START_TIME_COMPONENT_COUNT = 1

    private val TOTAL_COMPONENT_COUNT =
        POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT + VECTOR_COMPONENT_COUNT + PARTICLE_START_TIME_COMPONENT_COUNT

    private val STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT

    private lateinit var particles: FloatArray
    private lateinit var vertexArray: VertexArray
    private var maxParticleCount = 0

    private var currentParticleCount = 0
    private var nextParticle = 0

    constructor(maxParticleCount: Int) {
        particles = FloatArray(maxParticleCount * TOTAL_COMPONENT_COUNT)
        vertexArray = VertexArray(particles)
        this.maxParticleCount = maxParticleCount
    }

    fun addParticle(position: Point, color: Int, direction: Vector, particleStartTime: Float) {
        val particleOffset = nextParticle * TOTAL_COMPONENT_COUNT

        var currentOffset = particleOffset
        nextParticle++
        if (currentParticleCount < maxParticleCount) {
            currentParticleCount++
        }

        if (nextParticle == maxParticleCount) {
            nextParticle = 0
        }

        particles[currentOffset++] = position.x
        particles[currentOffset++] = position.y
        particles[currentOffset++] = position.z

        particles[currentOffset++] = Color.red(color) / 255f
        particles[currentOffset++] = Color.green(color) / 255f
        particles[currentOffset++] = Color.blue(color) / 255f

        particles[currentOffset++] = direction.x
        particles[currentOffset++] = direction.y
        particles[currentOffset++] = direction.z

        particles[currentOffset] = particleStartTime

        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT)

    }

    fun bindData(particleProgram: ParticleShaderProgram) {
        var dataOffset = 0
        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
        dataOffset += POSITION_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.getColorAttributeLocation(),
            COLOR_COMPONENT_COUNT,
            STRIDE
        )
        dataOffset += COLOR_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.getDirectionVectorAttributeLocation(),
            VECTOR_COMPONENT_COUNT,
            STRIDE
        )
        dataOffset += VECTOR_COMPONENT_COUNT

        vertexArray.setVertexAttribPointer(
            dataOffset,
            particleProgram.getParticleStartTimeAttributeLocation(),
            PARTICLE_START_TIME_COMPONENT_COUNT,
            STRIDE
        )
    }

    fun draw(){
        GLES20.glDrawArrays(GLES20.GL_POINTS,0,currentParticleCount)
    }

}