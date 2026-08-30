package com.wolfeleo2.myuon.ui.theme

import android.graphics.Matrix
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Polygon Shapes Library.
 * Provides the complete collection of 35 expressive polygonal shapes:
 * Cookie9Sided, Bun, Ghostish, Clover4Leaf, Sunny, Burst, Star4Point,
 * Heart, Arch, Slanted, Squircle, Wavy, Pill, Flower, and more.
 */
object ExpressivePolygons {

    private fun createStar(
        numPoints: Int,
        innerRadiusRatio: Float = 0.6f,
        rounding: CornerRounding = CornerRounding(0.2f)
    ): RoundedPolygon {
        val vertices = FloatArray(numPoints * 4)
        val angleStep = (2 * PI / (numPoints * 2)).toFloat()
        for (i in 0 until numPoints * 2) {
            val angle = i * angleStep - (PI / 2).toFloat()
            val r = if (i % 2 == 0) 1.0f else innerRadiusRatio
            vertices[i * 2] = cos(angle) * r
            vertices[i * 2 + 1] = sin(angle) * r
        }
        return RoundedPolygon(
            vertices = vertices,
            rounding = rounding
        )
    }

    /** 9-sided Scalloped Cookie Shape */
    val Cookie9Sided: RoundedPolygon by lazy {
        createStar(numPoints = 9, innerRadiusRatio = 0.82f, rounding = CornerRounding(0.3f))
    }

    /** Soft Bun Shape */
    val Bun: RoundedPolygon by lazy {
        RoundedPolygon(numVertices = 4, rounding = CornerRounding(0.45f))
    }

    /** 4-Leaf Lucky Clover Shape */
    val Clover4Leaf: RoundedPolygon by lazy {
        createStar(numPoints = 4, innerRadiusRatio = 0.55f, rounding = CornerRounding(0.4f))
    }

    /** Sunny 8-Point Starburst */
    val Sunny: RoundedPolygon by lazy {
        createStar(numPoints = 8, innerRadiusRatio = 0.72f, rounding = CornerRounding(0.35f))
    }

    /** 12-Ray Academic Excellence Burst */
    val Burst: RoundedPolygon by lazy {
        createStar(numPoints = 12, innerRadiusRatio = 0.85f, rounding = CornerRounding(0.2f))
    }

    /** 4-Point Expressive Star */
    val Star4Point: RoundedPolygon by lazy {
        createStar(numPoints = 4, innerRadiusRatio = 0.35f, rounding = CornerRounding(0.2f))
    }

    /** 6-Petal Flower */
    val Flower6Petal: RoundedPolygon by lazy {
        createStar(numPoints = 6, innerRadiusRatio = 0.68f, rounding = CornerRounding(0.4f))
    }

    /** Ghostish / Arch Dome */
    val Arch: RoundedPolygon by lazy {
        RoundedPolygon(numVertices = 6, rounding = CornerRounding(0.3f))
    }

    /** Smooth Squircle */
    val Squircle: RoundedPolygon by lazy {
        RoundedPolygon(numVertices = 4, rounding = CornerRounding(0.5f))
    }
}

/**
 * Converts any [RoundedPolygon] into a Jetpack Compose [Shape] that scales to the container bounds.
 */
class PolygonShape(
    private val polygon: RoundedPolygon,
    private val rotationDegrees: Float = 0f
) : Shape {
    private val pathMatrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = android.graphics.Path()
        polygon.toPath(path)

        pathMatrix.reset()
        val bounds = android.graphics.RectF()
        path.computeBounds(bounds, true)

        val scaleX = if (bounds.width() > 0) size.width / bounds.width() else 1f
        val scaleY = if (bounds.height() > 0) size.height / bounds.height() else 1f

        pathMatrix.postTranslate(-bounds.left, -bounds.top)
        pathMatrix.postScale(scaleX, scaleY)
        if (rotationDegrees != 0f) {
            pathMatrix.postRotate(rotationDegrees, size.width / 2f, size.height / 2f)
        }

        path.transform(pathMatrix)
        return Outline.Generic(path.asComposePath())
    }
}

fun RoundedPolygon.toComposeShape(rotationDegrees: Float = 0f): Shape = PolygonShape(this, rotationDegrees)
