package com.cmim.hdpf.myopengles.program

import android.content.Context
import android.opengl.GLES20
import com.cmim.hdpf.myopengles.util.ShaderHelper
import com.cmim.hdpf.myopengles.util.TextResourceReader

open class ShaderProgram {
    //Uniform constants
    protected val U_MATRIX = "u_Matrix"
    protected val U_COLOR = "u_Color"
    protected val U_TEXTURE_UNIT = "u_TextureUnit"

    //Attribute constants
    protected val A_POSITION = "a_Position"
    protected val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

    protected var program: Int = 0

    constructor(context: Context, vertexShaderResourceId: Int, fragmentShaderResourceId: Int) {
        program = ShaderHelper.buildProgram(
            TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
            TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId)
        )
    }

    fun useProgram(){
        GLES20.glUseProgram(program)
    }

}