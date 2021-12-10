package com.cmim.hdpf.myopengles

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix.*
import android.util.Log
import com.cmim.hdpf.myopengles.`object`.Mallet
import com.cmim.hdpf.myopengles.`object`.Puck
import com.cmim.hdpf.myopengles.`object`.Table
import com.cmim.hdpf.myopengles.program.ColorShaderProgram
import com.cmim.hdpf.myopengles.program.TextureShaderProgram
import com.cmim.hdpf.myopengles.util.MatrixHelper
import com.cmim.hdpf.myopengles.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @1 GLSurfaceView会在一个单独的线程中调用渲染器的方法。默认情况下，GLSurfaceView会以显示设备的刷新频率不断的渲染
 * 也可以配置为按请求渲染，只需要用GlSurfaceView.RENDERMODE_WITH_DIRTY作为参数调用GLSurfaceView.setRenderMode()即可
 *
 * @2 当我们定义三角形的时候，应以逆时针的排序排列顶点，这称为卷曲顺序，因为在任何地方都使用这种一致的卷曲顺序，可以优化性能
 *
 * @3 当我们在Android模拟器或者设备上编译和运行Java/Kotlin代码的时候，它并不是直接运行在硬件上的；
 * 相反，它运行在一个特殊的环境中，Android虚拟机(ART/Dalvik虚拟机，7.0之前是Dalvik,7.0之后是ART)；
 * 运行在虚拟机上的代码不能直接访问本地环境，除非通过特定的API。
 * OpenGL作为本地系统库直接运行在硬件上，Android和 OpenGL之间需要进行相关必要的交互处理
 * 1.JNI 当调用android.opengl.GLES20包里的方法时，实际上就是在后台使用JNI调用本地系统库的
 * 2.改变内存分配的方式。Java有一个特殊的类集合，可以分配本地内存块，并且把Java的数据复制到本地内存。本地内存可以被本地环境存取，而不受垃圾回收器的管控
 *
 * @4 当OpenGL把着色器链接成一个程序的时候，它实际上用一个位置编号把片段着色器中定义的每个uniform都关联起来了
 * 这些位置编号用来给着色器发送数据，并且我们需要u_color的位置，以便我们可以在要绘画的时候设置颜色。
 *
 * @5 与属性不同uniform的分量没有默认值，因此如果一个uniform在着色器中被定义为vec4类型，我们需要提供所有四个分量的值。
 * 一旦指定了颜色，就可以就行绘制了
 *
 * @6 u_color。它不像属性，每个顶点都要设置一个；一个uniform会让每个顶点都是用同一个值，除非我们再次改变它。
 * 如顶点着色器中的位置所是用的属性一样，u_color也是一个四分量向量
 * main()方法是着色器的主入口点，它把我们在uniform里定义的颜色复制到那个特殊的输出变量---gl_FragColor。
 * 着色器一定要给gl_FragColor赋值，OpenGL会使用这个颜色作为当前片段的最终颜色。
 */

class AirHockeyRenderer : GLSurfaceView.Renderer {
    private val TAG = "AirHockeyRenderer"

    //视图矩阵
    private val viewMatrix = FloatArray(16)

    private val viewProjectMatrix = FloatArray(16)
    private val modelViewProjectMatrix = FloatArray(16)

    //模型矩阵
    private val modelMatrix = FloatArray(16)

    /**
     * 用于存储顶点数组的4x4矩阵
     */
    private val projectionMatrix = FloatArray(16)

    private var context: Context

    private lateinit var table: Table
    private lateinit var mallet: Mallet
    private lateinit var puck: Puck

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram

    private var texture: Int = 0

    constructor(context: Context) {
        this.context = context
    }

    /**
     * 备注见顶部 @1
     * surface被创建时 GLSurfaceView会调用此方法
     * 1.应用程序第一次运行
     * 2.设备被唤醒或者用户从其他的activity切换回来
     * 本方法可能会被调用多次
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        table = Table()
        mallet = Mallet(0.08f, 0.15f, 32)
        puck = Puck(0.06f, 0.02f, 32)

        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)
        texture = TextureHelper.loadTexture(context, R.mipmap.air_hockey_surface)
    }

    /**
     * surface尺寸变化是，GLSurfaceView会调用此方法
     * 横竖屏切换时，surface尺寸会发生变化
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //设置OpenGL的视口（viewPort）尺寸，告诉OpenGL可以用来渲染的surface的大小
        glViewport(0, 0, width, height)
        Log.i(TAG, "width = $width, height = $height")

        //用45度的视野创建一个透视投影。这个视锥体，从z值为-1的位置开始，在z值为-10的位置结束
        MatrixHelper.perspectiveM(
            projectionMatrix,
            45f,
            width.toFloat() / height.toFloat(),
            1f,
            10f
        )

        setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f)

//        //把模型矩阵设为单位矩阵，再沿着z轴平移-2
//        setIdentityM(modelMatrix, 0)
//        translateM(modelMatrix, 0, 0f, 0f, -2.5f)
//        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)
//        //把模型矩阵与投影矩阵相乘，得到一个矩阵，然后把这个矩阵传递给顶点着色器。通过这种方式
//        //我们可以再着色器中只保留一个矩阵
//        /**
//         * 创建一个临时的浮点数组用于存储临时结果，然后把投影矩阵和模型矩阵相乘，其结果存于临时数组
//         * 接着把结果存回projectionMatrix，它包含模型矩阵与投影矩阵的组合效应
//         */
//        val temp = FloatArray(16)
//        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
//        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)
    }

    /**
     * 绘制一帧时，GLSurfaceView会调用此方法
     * 在这个方法中，一定要绘制一些东西，哪怕是清空屏幕，因为在这个方法返回后，渲染缓冲区会被交换并显示在屏幕上，
     * 如果什么都没画，可能会看到糟糕的闪烁效果
     *
     * @param gl OpenGL ES 1.0的API遗留下来的，如果要编写使用OpenGL ES 1.0的渲染器，就要使用这个参数
     * 对于OpenGL ES 2.0，GLES20类提供了静态方法来存取
     *
     */
    override fun onDrawFrame(gl: GL10?) {
        //调用glClear清空屏幕，擦除屏幕上的所有颜色，并用之前的glClearColor调用定义的颜色填充整个屏幕
        glClear(GL_COLOR_BUFFER_BIT)

        multiplyMM(viewProjectMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        positionTableInScene()
        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectMatrix, texture)
        table.bindData(textureProgram)
        table.draw()

        //Draw the mallet
        positionObjectInScene(0f, mallet.height / 2f, -0.4f)
        colorProgram.useProgram()
        colorProgram.setUniforms(modelViewProjectMatrix, 1f, 0f, 0f)
        mallet.bindData(colorProgram)
        mallet.draw()

        positionObjectInScene(0f, mallet.height / 2f, 0.4f)
        colorProgram.setUniforms(modelViewProjectMatrix, 0f, 0f, 1f)

        mallet.draw()

        positionObjectInScene(0f, puck.height / 2f, 0f)
        colorProgram.setUniforms(modelViewProjectMatrix, 0.8f, 0.8f, 1f)
        puck.bindData(colorProgram)
        puck.draw()
    }

    private fun positionObjectInScene(x: Float, y: Float, z: Float) {
       setIdentityM(modelMatrix,0)
        translateM(modelMatrix,0,x,y,z)
        multiplyMM(modelViewProjectMatrix,0,viewProjectMatrix,0,modelMatrix,0)
    }

    private fun positionTableInScene() {
        setIdentityM(modelMatrix, 0)
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        multiplyMM(modelViewProjectMatrix, 0, viewProjectMatrix, 0, modelMatrix, 0)
    }
}
