package com.cmim.hdpf.myopengles

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.opengl.Matrix.orthoM
import android.util.Log
import com.cmim.hdpf.myopengles.util.ShaderHelper
import com.cmim.hdpf.myopengles.util.TextResourceReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
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

    private val vertexSize = 18

    /**
     * 容纳在OpenGL程序对象中的位置的变量
     */
//    private val U_COLOR = "u_Color"
//    private var uColorLocation = 0

    private val TAG = "AirHockeyRenderer"

    private val POSITION_COMPONENT_COUNT = 2
    private val COLOR_COMPONENT_COUNT = 3
    private val BYTES_PER_FLOAT = 4

    private val A_COLOR = "a_Color"
    private val A_POSITION = "a_Position"
    private val U_MATRIX = "u_Matrix"

    private var aPositionLocation = 0
    private var aColorLocation = 0

    //保存矩阵uniform的位置
    private var uMatrixLocation = 0

    private val projectionMatrix = FloatArray(16)

    /**
     * stride 跨距
     * 我们在同一个数据数组里面既有位置又有颜色属性，OpenGL不能再假定下一个位置紧跟着前一个位置的。
     * 一旦OpenGL读入了一个顶点的位置，如果再想读入下一个顶点的位置，就需要跳过当前的颜色数据
     * stride（跨距）就是告诉OpenGL每个位置之间有多少个字节，需要跳过多少的
     */
    private val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT

    private var vertexData: FloatBuffer? = null

    private var program: Int = 0

    private var context: Context


    constructor(context: Context) {
        this.context = context
        /**
         * 备注见顶部 @3
         * 使用 ByteBuffer.allocateDirect() 分配了一块本地内存，这块内存不会被垃圾回收器管理。这个方法需要知道要分配多少字节的内存块；
         * 因为顶点都存储在一个浮点数组里，并且每个浮点数有4个字节，所以这款内存的大小应该是 tableVerticesWithTriangles.size * BYTES_PER_FLOAT
         *
         * 下一行告诉字节缓冲区(byte buffer),按照本地字节序(native byte order)组织它的内容；
         * 本地字节序是指，当一个值占用多少个字节时，字节按照从最重要位到最不重要位或者相反顺序排列；
         * 怎么排序的无关紧要，重要的是作为一个平台要使用同样的排序；调用 order(ByteOrder.nativeOrder()) 可以保证这一点
         *
         * 最后，我们调用 asFloatBuffer() 得到一个可以反映底层字节的 FloatBuffer 类实例；然后就可以赋值给 vertexData，
         * 这就把数据从 Android虚拟机的内存复制到本地内存了。当本地进程结束的时候，这块内存会被释放掉。
         */

        /**
         * 备注见顶部 @2
         * OpenGL里面，只能绘制点，线和三角形
         * 四边形的桌子需要用两个三角形进行拼接
         */
//        val tableVerticesWithTriangles = floatArrayOf( // Triangle 1
//            -0.5f, -0.5f,
//            0.5f, 0.5f,
//            -0.5f, 0.5f,  // Triangle 2
//            -0.5f, -0.5f,
//            0.5f, -0.5f,
//            0.5f, 0.5f,  // Line 1
//            -0.5f, 0f,
//            0.5f, 0f,  // Mallets
//            0f, -0.25f,
//            0f, 0.25f
//        )

        /**
         * 更新三角形
         * 新顶点的加入，形成了四个三角形，中心点坐标是0，0，五个点形成4个三角形，我们称之为三角形扇
         * 一个三角形扇以一个中心顶点作为起始，使用相邻的两个顶点创建第一个三角形，接下来的每个顶点都会创建一个三角形，
         * 围绕起始的中心点按扇形展开。为了使扇形闭合，我们只需要在最后重复第二个点。
         */
        val tableVerticesWithTriangles = floatArrayOf(
            //order of coordinates:x,y,r,g,b
            0f, 0f, 1f, 1f, 1f,

            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
//            -0.375f, -0.5f, 0.7f, 0.7f, 0.7f,
            -0.25f, -0.8f, 0.7f, 0f, 0.7f,
//            -0.125f, -0.5f, 0.7f, 0.7f, 0.7f,
//            0.125f, -0.5f, 0.7f, 0.7f, 0.7f,
//            0.375f, -0.5f, 0.7f, 0.7f, 0.7f,
            0f, -0.8f, 0.7f, 0.7f, 0.7f,
            0.25f, -0.8f, 0f, 0.7f, 0.7f,

            0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
//            0.5f, -0.375f, 0.7f, 0.7f, 0.7f,
            0.5f, -0.25f, 0.7f, 0f, 0.7f,
//            0.5f, -0.125f, 0.7f, 0.7f, 0.7f,
//            0.5f, 0.125f, 0.7f, 0.7f, 0.7f,
//            0.5f, 0.375f, 0.7f, 0.7f, 0.7f,
            0.5f, 0f, 0.7f, 0.7f, 0.7f,
            0.5f, 0.25f, 0f, 0.7f, 0.7f,

            0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
//            0.375f, 0.5f, 0.7f, 0.7f, 0.7f,
            0.25f, 0.8f, 0.7f, 0f, 0.7f,
//            0.125f, 0.5f, 0.7f, 0.7f, 0.7f,
//            -0.125f, 0.5f, 0.7f, 0.7f, 0.7f,
//            -0.375f, 0.5f, 0.7f, 0.7f, 0.7f,
            0f, 0.8f, 0.7f, 0.7f, 0.7f,
            -0.25f, 0.8f, 0f, 0.7f, 0.7f,

            -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
//            -0.5f, 0.375f, 0.7f, 0.7f, 0.7f,
            -0.5f, 0.25f, 0.7f, 0f, 0.7f,
//            -0.5f, 0.125f, 0.7f, 0.7f, 0.7f,
//            -0.5f, -0.125f, 0.7f, 0.7f, 0.7f,
//            -0.5f, -0.375f, 0.7f, 0.7f, 0.7f,
            -0.5f, 0f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.25f, 0f, 0.7f, 0.7f,

            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,

            // Line 1
            -0.5f, 0f, 1f, 0f, 0f,
            0.5f, 0f, 1f, 1f, 0f,
            // Mallets
            0f, -0.25f, 0f, 0f, 1f,
            0f, 0.25f, 1f, 0f, 0f
        )

//        val tableVerticesWithTriangles = floatArrayOf(
//            //order of coordinates:x,y,r,g,b
//            0f, 0f, 1f, 1f, 1f,
//            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
//            0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
//            0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
//            -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
//            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
//            // Line 1
//            -0.5f, 0f, 1f, 0f, 0f,
//            0.5f, 0f, 1f, 1f, 0f,
//            // Mallets
//            0f, -0.25f, 0f, 0f, 1f,
//            0f, 0.25f, 1f, 0f, 0f
//        )

        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        vertexData?.put(tableVerticesWithTriangles)
    }

    /**
     * 备注见顶部 @1
     * surface被创建时 GLSurfaceView会调用此方法
     * 1.应用程序第一次运行
     * 2.设备被唤醒或者用户从其他的activity切换回来
     * 本方法可能会被调用多次
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置清空屏幕用的颜色，这里是红色
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        //读取着色器的代码
        val vertexShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_vertex_shader)
        val fragmentShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_fragment_shader)

        //着色器编译
        val compileVertexShader = ShaderHelper.compileVertexShader(vertexShaderSource)
        val compileFragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource)

        //着色器链接
        program = ShaderHelper.linkProgram(compileVertexShader, compileFragmentShader)
        ShaderHelper.validateProgram(program)
        //告诉OpenGL在绘制任何东西到屏幕上的时候都要使用这里定义的程序
        glUseProgram(program)

        /**
         * 调用 glGetAttribLocation() 获取属性的位置。有了这个位置，就能告诉OpenGL到哪里去找到这个属性对应的数据了
         */
        aPositionLocation = glGetAttribLocation(program, A_POSITION)

        /**
         * 备注见顶部 @4
         * 获取uniform的位置，aColorLocation
         */
//        uColorLocation = glGetUniformLocation(program, U_COLOR)
        aColorLocation = glGetAttribLocation(program, A_COLOR)

        //关联属性与定点数据的数组
        vertexData?.position(0)
        /**
         * index 属性的位置
         * size 每个属性的数据的技术，上述我们每个顶点使用两个浮点数用来表达位置的一个x坐标和一个y坐标
         *      我们为每个顶点只传递两个分量，但是在着色器中，a_Position被定义为vec4，它有4个分量。
         *      如果一个分量没有被指定值，默认情况下，OpenGL会把前3个分量设为0，最后一个分量设为1
         * type 数据类型。我们上述定义的为浮点数的类型
         * normalized 只有使用整型数据的时候，才有意义
         * stride 只有当一个数组存储多于一个属性时，才有意义
         * ptr 告诉OpenGL去哪里读取数据。它会从缓冲区的当前位置读取，如果我们没有调用 vertexData?.position(0)，
         *      它可能尝试读取缓冲区结尾后面的内容，并使我们的应用程序崩溃。
         *
         * 传递不正确的参数给 glVertexAttribPointer 会导致奇怪的结果，甚至导致程序崩溃
         * glVertexAttribPointer(int indx,int size,int type,boolean normalized,int stride,java.nio.Buffer ptr)
         */
        glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )

        //使能顶点数据
        glEnableVertexAttribArray(aPositionLocation)

        //关联颜色属性与定点数据的数组
        vertexData?.position(POSITION_COMPONENT_COUNT)
        glVertexAttribPointer(
            aColorLocation,
            COLOR_COMPONENT_COUNT,
            GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )
        //使能顶点数据
        glEnableVertexAttribArray(aColorLocation)

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
    }

    /**
     * surface尺寸变化是，GLSurfaceView会调用此方法
     * 横竖屏切换时，surface尺寸会发生变化
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //设置OpenGL的视口（viewPort）尺寸，告诉OpenGL可以用来渲染的surface的大小


        glViewport(0, 0, width, height)
        Log.i(TAG, "width = $width, height = $height")
        val aspectRatio = if (width > height) {
            width.toFloat() / height.toFloat()
        } else {
            height.toFloat() / width.toFloat()
        }
        /**
         * 创建正交投影矩阵，这个矩阵会把屏幕的当前方向计算在内，建立一个虚拟坐标空间。
         * 首先计算了宽高比，使用宽和高中的较大值除以较小值
         * 接下来调用orthoM()函数。
         * 如果在横屏模式下，扩展宽度的坐标空间，取值范围为 -aspectRatio-aspectRatio，高度取值-1,1
         * 如果在横屏模式下，扩展高度的坐标空间，取值范围为 -aspectRatio-aspectRatio，宽度取值-1,1
         *
         */
        if (width > height) {
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
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
        /**
         * 备注见顶部 @5
         * glUniform4f更新着色器代码中的u_color的值。
         * 我们已经把顶点数据和a_Color关联起来了，只需要调用glDrawArrays()即可，OpenGL会自动从顶点数据里读入颜色属性
         */
//        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f)
        /**
         * 绘制
         * mode：点，直线，三角形
         * first：从哪里开始读顶点
         * count：读取几个顶点
         * glDrawArrays(int mode,int first,int count)
         *
         * 下面的代码是，绘制三角形，从开头处读顶点，读取6个顶点，因为每个三角形有三个，我们需要绘制两个三角形
         */
//        glDrawArrays(GL_TRIANGLES, 0, 6)

        //给着色器传递正交投影矩阵
        glUniformMatrix4fv(uMatrixLocation,1,false,projectionMatrix,0)

        /**
         * GL_TRIANGLE_FAN 代表绘制一个三角形扇
         */
        glDrawArrays(GL_TRIANGLE_FAN, 0, vertexSize)

//        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        //画线 从第7个顶点开始读两个顶点
        glDrawArrays(GL_LINES, vertexSize, 2)

//        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f)
        //第8个顶点，并用一个顶点绘制一个点
        glDrawArrays(GL_POINTS, vertexSize + 2, 1)

//        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        //第8个顶点，并用一个顶点绘制一个点
        glDrawArrays(GL_POINTS, vertexSize + 3, 1)
    }
}
