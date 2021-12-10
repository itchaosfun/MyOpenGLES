package com.cmim.hdpf.myopengles.geometry

class Point(var x: Float, var y: Float, var z: Float) {

    fun translateY(distance:Float):Point{
        return Point(x,y+distance,z)
    }
}