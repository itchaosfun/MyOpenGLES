package com.cmim.hdpf.myopengles

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix.*
import com.cmim.hdpf.myopengles.`object`.ParticleShooter
import com.cmim.hdpf.myopengles.`object`.ParticleSystem
import com.cmim.hdpf.myopengles.geometry.Geometry.Companion.Vector
import com.cmim.hdpf.myopengles.geometry.Point
import com.cmim.hdpf.myopengles.program.ParticleShaderProgram
import com.cmim.hdpf.myopengles.util.MatrixHelper
import com.cmim.hdpf.myopengles.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ParticlesRenderer : GLSurfaceView.Renderer {
    private val TAG = "ParticlesRenderer"


    //用于存储顶点数组的4x4矩阵
    private val projectionMatrix = FloatArray(16)

    //视图矩阵
    private val viewMatrix = FloatArray(16)

    private val viewProjectMatrix = FloatArray(16)


    private var particleProgram: ParticleShaderProgram? = null
    private var particleSystem: ParticleSystem? = null
    private var redParticleShooter: ParticleShooter? = null
    private var greenParticleShooter: ParticleShooter? = null
    private var blueParticleShooter: ParticleShooter? = null
    private var globalStartTime = 0L

    private val angleVarianceInDegrees = 5f
    private val speedVariance = 1f

    private var texture = 0

    private var context: Context

    constructor(context: Context) {
        this.context = context
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)

        particleProgram = ParticleShaderProgram(context)
        particleSystem = ParticleSystem(10000)
        globalStartTime = System.nanoTime()

        val particleDirection = Vector(0f, 0.5f, 0f)

        redParticleShooter =
            ParticleShooter(
                Point(-1f, 0f, 0f),
                particleDirection,
                Color.rgb(255, 50, 5),
                angleVarianceInDegrees,
                speedVariance
            )

        greenParticleShooter =
            ParticleShooter(
                Point(0f, 0f, 0f),
                particleDirection,
                Color.rgb(25, 255, 25),
                angleVarianceInDegrees,
                speedVariance
            )

        blueParticleShooter =
            ParticleShooter(
                Point(1f, 0f, 0f),
                particleDirection,
                Color.rgb(5, 50, 255),
                angleVarianceInDegrees,
                speedVariance
            )

        texture = TextureHelper.loadTexture(context,R.mipmap.particle_texture)

    }

    /**
     * surface尺寸变化是，GLSurfaceView会调用此方法
     * 横竖屏切换时，surface尺寸会发生变化
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //设置OpenGL的视口（viewPort）尺寸，告诉OpenGL可以用来渲染的surface的大小
        glViewport(0, 0, width, height)

        MatrixHelper.perspectiveM(
            projectionMatrix,
            45f,
            width.toFloat() / height.toFloat(),
            1f,
            10f
        )
        setIdentityM(viewMatrix, 0)
        translateM(viewMatrix, 0, 0f, -1.5f, -5f)
        multiplyMM(viewProjectMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        //调用glClear清空屏幕，擦除屏幕上的所有颜色，并用之前的glClearColor调用定义的颜色填充整个屏幕
        glClear(GL_COLOR_BUFFER_BIT)
        val currentTime = (System.nanoTime() - globalStartTime) / 1000000000f
        redParticleShooter?.addParticles(particleSystem!!, currentTime, 1)
        greenParticleShooter?.addParticles(particleSystem!!, currentTime, 1)
        blueParticleShooter?.addParticles(particleSystem!!, currentTime, 1)

        particleProgram?.useProgram()
        particleProgram?.setUniforms(viewProjectMatrix, currentTime,texture)
        particleSystem?.bindData(particleProgram!!)
        particleSystem?.draw()

    }
}
