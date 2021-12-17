package com.cmim.hdpf.myopengles

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.View

class ParticlesActivity : AppCompatActivity() {

    private val TAG = "ParticlesActivity"

    private val glSurfaceView by lazy {
        GLSurfaceView(this)
    }

    private var rendererSet: Boolean = false
    private lateinit var particlesRenderer:ParticlesRenderer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //设置opengl es 2.0的版本依赖上下文
        glSurfaceView.setEGLContextClientVersion(2)
        //设置渲染器
        particlesRenderer = ParticlesRenderer(this)
        glSurfaceView.setRenderer(particlesRenderer)
        //已经渲染置为true
        rendererSet = true

        setOnTouchEvent()

        setContentView(glSurfaceView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchEvent() {
        glSurfaceView.setOnTouchListener(object :View.OnTouchListener{
            var previousX = 0f
            var previousY = 0f
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null || v == null){
                    return false
                }

                if (event.action == ACTION_DOWN){
                    previousX = event.x
                    previousY = event.y
                }else if (event.action == ACTION_MOVE){
                    val deltaX = event.x - previousX
                    val deltaY = event.y - previousY
                    previousX = event.x
                    previousY = event.y

                    glSurfaceView.queueEvent {
                        particlesRenderer.handlerTouchDrag(deltaX,deltaY)
                    }
                }
                return true
            }
        })
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
