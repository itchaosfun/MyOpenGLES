package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20
import com.cmim.hdpf.myopengles.R

class HeightmapShaderProgram : ShaderProgram {
    private val TAG = "HeightmapShaderProgram"

    private var uMVMatrixLocation = 0
    private var uIT_MVMatrixLocation = 0
    private var uMVPMatrixLocation = 0
    private var uPointLightPositionsLocation = 0
    private var uPointLightColorsLocation = 0

    private var uVectorToLightLocation = 0
    private var aPositionLocation = 0
    private var aNormalLocation = 0


    constructor(context: Context) :
            super(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader) {
        uMVMatrixLocation = GLES20.glGetUniformLocation(program, U_MV_MATRIX)
        uIT_MVMatrixLocation = GLES20.glGetUniformLocation(program, U_IT_MV_MATRIX)
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, U_MVP_MATRIX)
        uPointLightPositionsLocation = GLES20.glGetUniformLocation(program, U_POINT_LIGHT_POSITIONS)
        uPointLightColorsLocation = GLES20.glGetUniformLocation(program, U_POINT_LIGHT_COLORS)
        uVectorToLightLocation = GLES20.glGetUniformLocation(program, U_VECTOR_TO_LIGHT)

        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)
        aNormalLocation = GLES20.glGetAttribLocation(program, A_NORMAL)

    }

    fun setUniforms(
        mvMatrix: FloatArray,
        it_mvMatrix: FloatArray,
        mvpMatrix: FloatArray,
        vectorToDirectionalLight: FloatArray,
        pointLightPositions:FloatArray,
        pointLightColors:FloatArray
    ) {
        GLES20.glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(uIT_MVMatrixLocation, 1, false, it_mvMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0)
        GLES20.glUniform3fv(uVectorToLightLocation,1,vectorToDirectionalLight,0)

        GLES20.glUniform4fv(uPointLightPositionsLocation,3,pointLightPositions,0)
        GLES20.glUniform3fv(uPointLightColorsLocation,3,pointLightColors,0)
    }

    fun getPositionAttributeLocation() = aPositionLocation

    fun getNormalAttributeLocation() = aNormalLocation
}