package com.cmim.hdpf.myopengles

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.cmim.hdpf.myopengles.`object`.Heightmap
import com.cmim.hdpf.myopengles.`object`.ParticleShooter
import com.cmim.hdpf.myopengles.`object`.ParticleSystem
import com.cmim.hdpf.myopengles.`object`.Skybox
import com.cmim.hdpf.myopengles.geometry.Geometry.Companion.Vector
import com.cmim.hdpf.myopengles.geometry.Point
import com.cmim.hdpf.myopengles.program.HeightmapShaderProgram
import com.cmim.hdpf.myopengles.program.ParticleShaderProgram
import com.cmim.hdpf.myopengles.program.SkyboxShaderProgram
import com.cmim.hdpf.myopengles.util.MatrixHelper
import com.cmim.hdpf.myopengles.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ParticlesRenderer : GLSurfaceView.Renderer {
    private val TAG = "ParticlesRenderer"

    private val modelMatrix = FloatArray(16)

    //视图矩阵
    private val viewMatrix = FloatArray(16)
    private val viewMatrixForSkybox = FloatArray(16)

    //用于存储顶点数组的4x4矩阵
    private val projectionMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
//    private val viewProjectMatrix = FloatArray(16)

    private lateinit var heightmapShaderProgram: HeightmapShaderProgram
    private lateinit var heightmap: Heightmap

    private var skyboxShaderProgram: SkyboxShaderProgram? = null
    private var skybox: Skybox? = null
    private var skyboxTexture: Int = 0

    private var particleProgram: ParticleShaderProgram? = null
    private var particleSystem: ParticleSystem? = null
    private var redParticleShooter: ParticleShooter? = null
    private var greenParticleShooter: ParticleShooter? = null
    private var blueParticleShooter: ParticleShooter? = null
    private var globalStartTime = 0L

    private val angleVarianceInDegrees = 5f
    private val speedVariance = 1f

    private var particleTexture = 0

    private var xRotation = 0f
    private var yRotation = 0f

    private var context: Context

    constructor(context: Context) {
        this.context = context
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)

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

        particleTexture = TextureHelper.loadTexture(context, R.mipmap.particle_texture)

        skyboxShaderProgram = SkyboxShaderProgram(context)
        skybox = Skybox()
        skyboxTexture = TextureHelper.loadCubeMap(
            context, intArrayOf(
                R.mipmap.left,
                R.mipmap.right,
                R.mipmap.bottom,
                R.mipmap.top,
                R.mipmap.front,
                R.mipmap.back
            )
        )

        heightmapShaderProgram = HeightmapShaderProgram(context)
        heightmap = Heightmap(
            ((ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.heightmap,
                context.resources.newTheme()
            ) as BitmapDrawable).toBitmap())
        )
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
            100f
        )
        updateViewMatrices()
    }

    override fun onDrawFrame(gl: GL10?) {
        //调用glClear清空屏幕，擦除屏幕上的所有颜色，并用之前的glClearColor调用定义的颜色填充整个屏幕
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        drawHeightmap()
        drawSkybox()
        drawParticle()

    }

    private fun drawParticle() {
        val currentTime = (System.nanoTime() - globalStartTime) / 1000000000f
        redParticleShooter?.addParticles(particleSystem!!, currentTime, 5)
        greenParticleShooter?.addParticles(particleSystem!!, currentTime, 5)
        blueParticleShooter?.addParticles(particleSystem!!, currentTime, 5)

        setIdentityM(modelMatrix, 0)
        updateMvpMatrix()

        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)

        glDepthMask(false)
        particleProgram?.useProgram()
        particleProgram?.setUniforms(modelViewProjectionMatrix, currentTime, particleTexture)
        particleSystem?.bindData(particleProgram!!)
        particleSystem?.draw()
        glDisable(GL_BLEND)
        glDepthMask(true)
    }

    private fun drawSkybox() {
        setIdentityM(modelMatrix, 0)
        updateMvpMatrixForSkybox()

        glDepthFunc(GL_LEQUAL)
        skyboxShaderProgram?.useProgram()
        skyboxShaderProgram?.setUniforms(modelViewProjectionMatrix, skyboxTexture)
        skybox?.bindData(skyboxShaderProgram!!)
        skybox?.draw()
        glDepthFunc(GL_LESS)
    }

    private fun drawHeightmap() {
        setIdentityM(modelMatrix, 0)
        scaleM(modelMatrix, 0, 100f, 10f, 100f)
        updateMvpMatrix()

        heightmapShaderProgram.useProgram()
        heightmapShaderProgram.setUniforms(modelViewProjectionMatrix)
        heightmap.bindData(heightmapShaderProgram)
        heightmap.draw()
    }

    fun handlerTouchDrag(deltaX: Float, deltaY: Float) {
        xRotation += deltaX / 16f
        yRotation += deltaY / 16f

        if (yRotation < -90) {
            yRotation = -90f
        } else if (yRotation > 90) {
            yRotation = 90f
        }

        updateViewMatrices()
    }

    private fun updateViewMatrices() {
        setIdentityM(viewMatrix, 0)
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f)
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f)
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.size)

        translateM(viewMatrix, 0, 0f, -1.5f, -5f)
    }

    private fun updateMvpMatrix() {
        multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
    }

    private fun updateMvpMatrixForSkybox() {
        multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0)
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
    }
}
