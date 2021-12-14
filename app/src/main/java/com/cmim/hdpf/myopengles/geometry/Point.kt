package com.cmim.hdpf.myopengles.geometry

import com.cmim.hdpf.myopengles.geometry.Geometry.Companion.Vector

class Point(var x: Float, var y: Float, var z: Float) {

    fun translateY(distance: Float): Point {
        return Point(x, y + distance, z)
    }

    fun translate(vector: Vector):Point {
        return Point(
            x + vector.x,
            y + vector.y,
            z + vector.z
        )
    }
}