package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20
import com.cmim.hdpf.myopengles.R

class HeightmapShaderProgram : ShaderProgram {
    private val TAG = "HeightmapShaderProgram"

    private var uMatrixLocation = 0
    private var aPositionLocation = 0


    constructor(context: Context) :
            super(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader) {
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX)
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)

    }

    fun setUniforms(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0)
    }

    fun getPositionAttributeLocation() = aPositionLocation
}