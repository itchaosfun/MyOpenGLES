package com.cmim.hdpf.myopengles.util

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.util.Log

/**
 * 纹理工具类
 */
object TextureHelper {

    private val TAG = "TextureHelper"

    /**
     * 加载纹理
     */
    fun loadTexture(context: Context, resourceId: Int): Int {
        val textureObjectIds = IntArray(1)
        glGenTextures(1, textureObjectIds, 0)
        if (textureObjectIds[0] == 0) {
            Log.i(TAG, "Could not generate a new OpenGL texture object.")
            return 0
        }

        val options = BitmapFactory.Options()
        options.inScaled = false

        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        if (bitmap == null) {
            Log.i(TAG, "Resource ID : $resourceId, could not be decoded.")
            glDeleteTextures(1, textureObjectIds, 0)
            return 0
        }
        //绑定一个二维纹理
        glBindTexture(GL_TEXTURE_2D,textureObjectIds[0])

        //缩小时，采用三线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        //放大时，采用双线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        //加载bitmap位图到OpenGL里
        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0)
        //bitmap的作用已经用完了，进行释放
        bitmap.recycle()

        //生成MIP贴图，告诉OpenGL生成所有必要的级别
        glGenerateMipmap(GL_TEXTURE_2D)
        //解除与这个纹理的绑定，这样就不会用其他纹理方法调用意外地改变这个纹理
        //传递0给glBindTexture()就与当前的纹理解除绑定了
        glBindTexture(GL_TEXTURE_2D,0)

        return textureObjectIds[0]

    }
}