package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import com.cmim.hdpf.myopengles.R
import javax.microedition.khronos.opengles.GL

class ColorShaderProgram : ShaderProgram {
    //Uniform locations
    private var uMatrixLocation = 0


    //Attribute location
    private var aPositionLocation = 0
    private var aColorLocation = 0

    constructor(context: Context) : super(
        context,
        R.raw.texture_vertex_shader,
        R.raw.texture_fragment_shader
    ) {
        //Retrieve uniform locations for the shader program
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX)

        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)
        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR)
    }

    fun setUniforms(matrix:FloatArray) {
        //Pass the matrix into the shader program
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }

    fun getPositionAttributeLocation():Int{
        return aPositionLocation
    }

    fun getColorAttributeLocation():Int{
        return aColorLocation
    }
}