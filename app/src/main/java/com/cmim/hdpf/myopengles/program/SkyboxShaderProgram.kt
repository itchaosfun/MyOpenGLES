package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20
import com.cmim.hdpf.myopengles.R

class SkyboxShaderProgram : ShaderProgram {

    private val TAG = "SkyboxShaderProgram"

    private var uMatrixLocation = 0
    private var uTextureUnitLocation = 0

    private var aPositionLocation = 0

    constructor(
        context: Context
    ) : super(context, R.raw.skybox_vertex_shader, R.raw.skybox_fragment_shader){
        uMatrixLocation = GLES20.glGetUniformLocation(program,U_MATRIX)
        uTextureUnitLocation = GLES20.glGetUniformLocation(program,U_TEXTURE_UNIT)

        aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION)
    }

    fun setUniforms(matrix:FloatArray,textureId:Int){
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP,textureId)
        GLES20.glUniform1i(uTextureUnitLocation,0)
    }

    fun getPositionAttributeLocation() = aPositionLocation

}