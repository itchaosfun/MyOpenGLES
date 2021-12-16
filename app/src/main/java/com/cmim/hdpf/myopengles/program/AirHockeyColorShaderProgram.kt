package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20.*
import com.cmim.hdpf.myopengles.R

class AirHockeyColorShaderProgram : ShaderProgram {
    //Uniform locations
    private var uMatrixLocation = 0
    private var uColorLocation = 0

    //Attribute location
    private var aPositionLocation = 0

    constructor(context: Context) : super(
        context,
        R.raw.simple_vertex_shader,
        R.raw.simple_fragment_shader
    ) {
        //Retrieve uniform locations for the shader program
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
        uColorLocation = glGetUniformLocation(program, U_COLOR)

        aPositionLocation = glGetAttribLocation(program, A_POSITION)
    }

    fun setUniforms(matrix:FloatArray,r:Float,g:Float,b:Float) {
        //Pass the matrix into the shader program
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glUniform4f(uColorLocation,r,g,b,1f)
    }

    fun getPositionAttributeLocation():Int{
        return aPositionLocation
    }
}