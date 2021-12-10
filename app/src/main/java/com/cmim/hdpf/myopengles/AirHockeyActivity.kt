package com.cmim.hdpf.myopengles

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AirHockeyActivity : AppCompatActivity() {

    private val TAG = "AirHockeyActivity"

    private val glSurfaceView by lazy {
        GLSurfaceView(this)
    }

    private var rendererSet:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //设置opengl es 2.0的版本依赖上下文
        glSurfaceView.setEGLContextClientVersion(2)
        //设置渲染器
        glSurfaceView.setRenderer(AirHockeyRenderer(this))
        //已经渲染置为true
        rendererSet = true

        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet){
            glSurfaceView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet){
            glSurfaceView.onResume()
        }
    }
}
