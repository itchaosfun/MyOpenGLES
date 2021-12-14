package com.cmim.hdpf.myopengles.geometry

class Geometry {

    companion object {
        fun intersects(sphere: Sphere, ray: Ray): Boolean {
            return distanceBetween(sphere.center, ray) < sphere.radius
        }

        fun vectorBetween(from: Point, to: Point): Vector {
            return Vector(to.x - from.x, to.y - from.y, to.z - from.z)
        }

        fun distanceBetween(point: Point, ray: Ray): Float {
            val p1ToPoint = vectorBetween(ray.point, point)
            val p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point)

            val areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length()
            val lengthOfBase = ray.vector.length()

            return areaOfTriangleTimesTwo / lengthOfBase

        }

        fun intersectionPoint(ray: Ray, plane: Plane): Point {
            val rayToPlaneVector = vectorBetween(ray.point,plane.point)
            val scaleFactor = rayToPlaneVector.dotProduct(plane.vector) / ray.vector.dotProduct(plane.vector)
            val intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor))
            return intersectionPoint
        }

        class Ray(val point: Point, val vector: Vector)

        class Vector(val x: Float, val y: Float, val z: Float) {
            fun length(): Float {
                return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            }

            fun crossProduct(vector: Vector): Vector {
                return Vector(
                    (y * vector.z) - (z * vector.y),
                    (z * vector.x) - (x * vector.z),
                    (x * vector.y) - (y * vector.x)
                )
            }

            fun dotProduct(vector: Vector): Float {
                return x * vector.x + y * vector.y + z * vector.z
            }

            fun scale(scaleFactor: Float): Vector {
                return Vector(x * scaleFactor,y * scaleFactor,z*scaleFactor)
            }
        }

        class Sphere(val center: Point, val radius: Float)

        class Plane(val point: Point,val vector: Vector)
    }

}