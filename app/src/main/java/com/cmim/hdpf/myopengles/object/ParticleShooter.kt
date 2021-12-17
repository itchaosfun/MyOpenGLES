package com.cmim.hdpf.myopengles.`object`


import android.opengl.GLES20
import android.opengl.Matrix
import android.opengl.Matrix.*
import com.cmim.hdpf.myopengles.geometry.Geometry.Companion.Vector
import com.cmim.hdpf.myopengles.geometry.Point
import kotlin.random.Random

class ParticleShooter {
    private lateinit var position: Point
    private var color = 0

    private var angleVariance = 0f
    private var speedVariance = 0f

    private val random = Random

    private var rotationMatrix = FloatArray(16)
    private var directionVector = FloatArray(4)
    private var resultVector = FloatArray(4)

    constructor(
        position: Point,
        direction: Vector,
        color: Int,
        angleVarianceDegrees: Float,
        speedVariance: Float
    ) {
        this.position = position
        this.color = color

        this.angleVariance = angleVarianceDegrees
        this.speedVariance = speedVariance

        directionVector[0] = direction.x
        directionVector[1] = direction.y
        directionVector[2] = direction.z

    }

    fun addParticles(particleSystem: ParticleSystem, currentTime: Float, count: Int) {
        for (i in 0 until count) {

            setRotateEulerM(
                rotationMatrix, 0,
                (random.nextFloat() - 0.5f) * angleVariance,
                (random.nextFloat() - 0.5f) * angleVariance,
                (random.nextFloat() - 0.5f) * angleVariance,
            )

            multiplyMV(resultVector, 0, rotationMatrix, 0, directionVector, 0)

            val speedAdjustment = 1f + random.nextFloat() * speedVariance

            val thisDirection = Vector(
                resultVector[0] * speedAdjustment,
                resultVector[1] * speedAdjustment,
                resultVector[2] * speedAdjustment
            )

            particleSystem.addParticle(position, color, thisDirection, currentTime)
        }
    }

}