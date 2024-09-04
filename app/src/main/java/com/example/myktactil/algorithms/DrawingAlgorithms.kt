package com.example.myktactil.algorithms

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

object DrawingAlgorithms {
    fun drawBezierCurve(canvas: Canvas, paint: Paint, points: List<Pair<Float, Float>>) {
        if (points.size != 4) return

        val path = Path()
        path.moveTo(points[0].first, points[0].second)

        for (t in 0..100) {
            val tt = t / 100f
            val x = bezierPoint(tt, points[0].first, points[1].first, points[2].first, points[3].first)
            val y = bezierPoint(tt, points[0].second, points[1].second, points[2].second, points[3].second)
            path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)
    }

    fun drawBSpline(canvas: Canvas, paint: Paint, points: List<Pair<Float, Float>>) {
        if (points.size < 4) return

        val path = Path()
        path.moveTo(points[0].first, points[0].second)

        for (i in 0 until points.size - 3) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val p2 = points[i + 2]
            val p3 = points[i + 3]

            for (t in 0..100) {
                val tt = t / 100f
                val x = bSplinePoint(tt, p0.first, p1.first, p2.first, p3.first)
                val y = bSplinePoint(tt, p0.second, p1.second, p2.second, p3.second)
                path.lineTo(x, y)
            }
        }

        canvas.drawPath(path, paint)
    }

    private fun bezierPoint(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val u = 1 - t
        return u * u * u * p0 +
               3 * u * u * t * p1 +
               3 * u * t * t * p2 +
               t * t * t * p3
    }

    private fun bSplinePoint(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val t2 = t * t
        val t3 = t2 * t
        return (1f - 3f * t + 3f * t2 - t3) * p0 / 6f +
               (4f - 6f * t2 + 3f * t3) * p1 / 6f +
               (1f + 3f * t + 3f * t2 - 3f * t3) * p2 / 6f +
               t3 * p3 / 6f
    }
}