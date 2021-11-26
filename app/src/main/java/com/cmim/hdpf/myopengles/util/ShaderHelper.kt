package com.cmim.hdpf.myopengles.util

import android.opengl.GLES20.*
import android.util.Log

/**
 * 顶点着色器(vertex shader)：生成每个顶点的最终位置，针对每个顶点，它都会执行一次；
 * 片段着色器(fragment shader)：为组成点、直线或者三角形的每个片段生成最终的颜色，针对每个片段，它都会执行一次；
 *    一个片段是一个小的、单一颜色的长方形区域，类似于极端及屏幕上的一个像素。
 *    片段着色器的主要目的就是高度GPU每个片段的最终颜色应该是什么。对于基本图元的每个片段，片段着色器都会被调用一次，
 *    因此，如果一个三角形被映射到10000个片段，片段着色器就会被调用10000次。
 * 一个OpenGL程序就是把一个顶点着色器和一个片段着色器链接在一起变成单个对象。
 * 顶点着色器和片段着色器总是一起工作的。虽然如此，但并不意味着它们必须是一对一匹配的，我们可以同时在多个程序中使用同一个着色器。
 */

object ShaderHelper {

    private val TAG = "ShaderHelper"

    fun compileVertexShader(shaderCode:String):Int{
        return compileShader(GL_VERTEX_SHADER,shaderCode)
    }

    fun compileFragmentShader(shaderCode:String):Int{
        return compileShader(GL_FRAGMENT_SHADER,shaderCode)
    }

    /**
     * 编译着色器
     * @param type 顶点着色器：GL_VERTEX_SHADER/片段着色器：GL_FRAGMENT_SHADER
     */
    private fun compileShader(type: Int, shaderCode: String): Int {
        //1.创建一个新的着色器对象，并检查是否创建成功
        /**
         * 创建对象并检查它是否有效，这种模式在OpenGL里广泛适用。
         * 1.首先适用一个如 glCreateShader() 一样的调用创建一个对象，这个调用会返回一个整型值。
         * 2.这个整型值就是OpenGL对象的引用。无论后面什么时候想要引用这个对象，就要把这个整型值传回OpenGL。
         * 3.返回值0表示这个对象创建失败，类似于Java中返回null。
         */
        val shaderObjectId = glCreateShader(type)
        if (shaderObjectId == 0){
            Log.w(TAG,"count not create new shader")
            return 0
        }
        //2.上传源代码
        /**
         * glShaderSource()告诉OpenGL读入字符串 shaderCode 定义的源代码，并且把它与 shaderObjectId 所引用的着色器对象关联起来
         */
        glShaderSource(shaderObjectId,shaderCode)
        //3.编译着色器
        /**
         * 告诉OpenGL编译先前上传到 shaderObjectId 的源代码
         */
        glCompileShader(shaderObjectId)
        //4.取出编译状态
        /**
         * 为了检查编译是失败还是成功，首先创建一个新的长度为1的int数组，称为 compileStatus；
         * 然后调用 glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS,compileStatus,0)。
         * 告诉OpenGL读取与 shaderObjectId 关联的编译状态，并把它写入 compileStatus 的第0个元素
         */
        val compileStatus = intArrayOf(1)
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS,compileStatus,0)
        // glGetShaderInfoLog(shaderObjectId) 获取一个可读的消息，如果 OpenGL有什么关于着色器的有用内容，就会把消息存到着色器的信息日志里
        Log.i(TAG,"Result of compiling source: ${shaderCode} \n: ${glGetShaderInfoLog(shaderObjectId)}")

        //5.验证编译状态并返回着色器对象ID
        if (compileStatus[0] == 0){
            //如果失败，删除着色器对象
            glDeleteShader(shaderObjectId)
            Log.w(TAG,"Compilation of shader failed.")
            return 0
        }

        return shaderObjectId
    }

    /**
     * 把着色器一起链接进OpenGL的程序
     */
    fun linkProgram(vertexShaderId:Int, fragmentShader:Int):Int{
        Log.i(TAG,"vertexShaderId = $vertexShaderId, fragmentShader = $fragmentShader")
        //创建程序对象，把对象的ID存进 programObjectId
        val programObjectId = glCreateProgram()
        if (programObjectId == 0){
            Log.w(TAG,"count not create new program")
            return 0
        }

        //使用 glAttachShader()把顶点着色器和片段着色器都附加到程序对象身上
        glAttachShader(programObjectId,vertexShaderId)
        glAttachShader(programObjectId,fragmentShader)

        //链接程序
        glLinkProgram(programObjectId)

        val linkStatus = intArrayOf(1)
        glGetProgramiv(programObjectId, GL_LINK_STATUS,linkStatus,0)

        Log.i(TAG,"Results of linking program: ${glGetProgramInfoLog(programObjectId)}")

        if (linkStatus[0] == 0){
            //检查链接状态，如果失败，则删除
            glDeleteProgram(programObjectId)
            Log.w(TAG,"Linking of program failed.")
            return 0
        }
        return programObjectId
    }

    //验证OpenGL程序是否有效
    fun validateProgram(programObjectId:Int):Boolean{
        glValidateProgram(programObjectId)

        val validateStatus = intArrayOf(1)
        //告诉OpenGL 读取与programObjectId有关联的程序状态，并存在validateStatus的第0个位置
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS,validateStatus,0)
        Log.i(TAG,"Results of validating program: ${validateStatus[0]}, ${glGetProgramInfoLog(programObjectId)}")
        return validateStatus[0] != 0
    }

}