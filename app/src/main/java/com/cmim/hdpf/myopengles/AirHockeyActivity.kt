package com.cmim.hdpf.myopengles

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent

class AirHockeyActivity : AppCompatActivity() {

    private val TAG = "AirHockeyActivity"

    private val glSurfaceView by lazy {
        GLSurfaceView(this)
    }

    private var rendererSet: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //设置opengl es 2.0的版本依赖上下文
        glSurfaceView.setEGLContextClientVersion(2)
        //设置渲染器
        val airHockeyRenderer = AirHockeyRenderer(this)
        glSurfaceView.setRenderer(airHockeyRenderer)
        //已经渲染置为true
        rendererSet = true

        glSurfaceView.setOnTouchListener { view, event ->
            if (event != null) {
                val normalizedX = event.x / (view.width.toFloat()) * 2 - 1
                val normalizedY = -(event.y / (view.height.toFloat()) * 2 - 1)

                if (event.action == MotionEvent.ACTION_DOWN){
                    glSurfaceView.queueEvent {
                        airHockeyRenderer.handleTouchPress(
                            normalizedX,
                            normalizedY
                        )
                    }
                }else if (event.action == MotionEvent.ACTION_MOVE){
                    glSurfaceView.queueEvent{
                        airHockeyRenderer.handleTouchDrag(
                            normalizedX,
                            normalizedY
                        )
                    }
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            glSurfaceView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            glSurfaceView.onResume()
        }
    }
}
