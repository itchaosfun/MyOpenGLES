package com.cmim.hdpf.myopengles.geometry

class Circle(var center: Point, var radius: Float) {
    fun scale(scale:Float):Circle{
        return Circle(center,radius * scale)
    }
}