package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20
import com.cmim.hdpf.myopengles.R

class ParticleShaderProgram : ShaderProgram {

    private var uMatrixLocation = 0
    private var uTimeLocation = 0
    private var uTextureUnitLocation = 0

    private var aPositionLocation = 0
    private var aColorLocation = 0
    private var aDirectionVectorLocation = 0
    private var aParticleStartTimeLocation = 0

    constructor(
        context: Context
    ) : super(context, R.raw.particle_vertex_shader, R.raw.particle_fragment_shader){
        uMatrixLocation = GLES20.glGetUniformLocation(program,U_MATRIX)
        uTimeLocation = GLES20.glGetUniformLocation(program,U_TIME)
        uTextureUnitLocation = GLES20.glGetUniformLocation(program,U_TEXTURE_UNIT)

        aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION)
        aColorLocation = GLES20.glGetAttribLocation(program,A_COLOR)
        aDirectionVectorLocation = GLES20.glGetAttribLocation(program,A_DIRECTION_VECTOR)
        aParticleStartTimeLocation = GLES20.glGetAttribLocation(program,A_PARTICLES_START_TIME)
    }

    fun setUniforms(matrix:FloatArray, elapsedTime:Float,textureId:Int){
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0)
        GLES20.glUniform1f(uTimeLocation,elapsedTime)

        GLES20.glActiveTexture(textureId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId)
        GLES20.glUniform1i(uTextureUnitLocation,0)
    }

    fun getPositionAttributeLocation() = aPositionLocation

    fun getColorAttributeLocation() = aColorLocation

    fun getDirectionVectorAttributeLocation() = aDirectionVectorLocation

    fun getParticleStartTimeAttributeLocation() = aParticleStartTimeLocation
}