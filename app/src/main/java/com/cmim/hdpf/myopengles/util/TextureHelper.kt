package com.cmim.hdpf.myopengles.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.opengl.GLUtils.texImage2D
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
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0])

        //缩小时，采用三线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        //放大时，采用双线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        //加载bitmap位图到OpenGL里
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        //bitmap的作用已经用完了，进行释放
        bitmap.recycle()

        //生成MIP贴图，告诉OpenGL生成所有必要的级别
        glGenerateMipmap(GL_TEXTURE_2D)
        //解除与这个纹理的绑定，这样就不会用其他纹理方法调用意外地改变这个纹理
        //传递0给glBindTexture()就与当前的纹理解除绑定了
        glBindTexture(GL_TEXTURE_2D, 0)

        return textureObjectIds[0]

    }

    fun loadCubeMap(context: Context, cubeResource: IntArray): Int {
        val textObjectIds = IntArray(1)
        glGenTextures(1, textObjectIds, 0)

        if (textObjectIds[0] == 0) {
            Log.w(TAG, "Could not generate a new OpenGL texture object.")
            return 0
        }

        val options = BitmapFactory.Options()
        options.inScaled = false

        val cubeBitmaps = arrayOfNulls<Bitmap>(6)
        for (i in 0..5) {
            cubeBitmaps[i] =
                BitmapFactory.decodeResource(context.resources, cubeResource[i], options)
            if (cubeBitmaps[i] == null) {
                Log.w(TAG, "Resource ID ${cubeResource[i]} could not be decoded.")
                glDeleteTextures(1, textObjectIds, 0)
                return 0
            }
        }

        //配置纹理过滤器
        glBindTexture(GL_TEXTURE_CUBE_MAP, textObjectIds[0])
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0)

        glBindTexture(GL_TEXTURE_2D, 0)

        cubeBitmaps.forEach {
            it?.recycle()
        }
        return textObjectIds[0]
    }
}