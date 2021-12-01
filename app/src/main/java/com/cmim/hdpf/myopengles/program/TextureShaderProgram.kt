package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import com.cmim.hdpf.myopengles.R
import javax.microedition.khronos.opengles.GL

class TextureShaderProgram : ShaderProgram {
    //Uniform locations
    private var uMatrixLocation = 0
    private var uTextureUnitLocation = 0

    //Attribute location
    private var aPositionLocation = 0
    private var aTextureCoordinatesLocation = 0

    constructor(context: Context) : super(
        context,
        R.raw.texture_vertex_shader,
        R.raw.texture_fragment_shader
    ) {
        //Retrieve uniform locations for the shader program
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX)
        uTextureUnitLocation = GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT)

        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)
        aTextureCoordinatesLocation = GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES)
    }

    fun setUniforms(matrix:FloatArray,textureId:Int) {
        //Pass the matrix into the shader program
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        //Set the active texture unit to texture unit 0
        GLES20.glActiveTexture(GL_TEXTURE0)

        //Bind the texture to this unit
        GLES20.glBindTexture(GL_TEXTURE_2D, textureId)

        //Tell the texture uniform sampler to use this texture in the shader by telling it to read from texture unit 0
        GLES20.glUniform1i(uTextureUnitLocation, 0)
    }

    fun getPositionAttributeLocation():Int{
        return aPositionLocation
    }

    fun getTextureCoordinatesAttributeLocation():Int{
        return aTextureCoordinatesLocation
    }
}