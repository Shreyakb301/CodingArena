package com.codingarena.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/**
 * Hand-drawn line icons for list rows and stat labels.
 *
 * The web build draws text with Skiko's bundled font, which has no glyphs for
 * the geometric/dingbat characters we used as row marks, so they showed as tofu
 * boxes. These are plain vector paths instead: a single 1.6pt stroke on a 24pt
 * grid, left untinted so the caller colours them to match its surface. Kept
 * deliberately spare so they read as quiet marks, not illustrations.
 */
object ArenaIcons {
    /** Practice by topic — a grid of patterns. */
    val Topics: ImageVector = icon("topics") {
        roundRect(3f, 3f, 7.5f, 7.5f)
        roundRect(13.5f, 3f, 7.5f, 7.5f)
        roundRect(3f, 13.5f, 7.5f, 7.5f)
        roundRect(13.5f, 13.5f, 7.5f, 7.5f)
    }

    /** Work on weak spots — a target. */
    val Target: ImageVector = icon("target") {
        circle(12f, 12f, 8f)
        circle(12f, 12f, 3.25f)
        moveTo(12f, 1.5f); verticalLineTo(4f)
        moveTo(12f, 20f); verticalLineTo(22.5f)
        moveTo(1.5f, 12f); horizontalLineTo(4f)
        moveTo(20f, 12f); horizontalLineTo(22.5f)
    }

    /** Mixed practice — shuffle. */
    val Shuffle: ImageVector = icon("shuffle") {
        moveTo(3f, 7f); horizontalLineToRelative(4f); lineTo(17f, 17f); horizontalLineToRelative(3f)
        moveTo(16.5f, 13.5f); lineTo(20f, 17f); lineTo(16.5f, 20.5f)
        moveTo(3f, 17f); horizontalLineToRelative(4f); lineTo(17f, 7f); horizontalLineToRelative(3f)
        moveTo(16.5f, 3.5f); lineTo(20f, 7f); lineTo(16.5f, 10.5f)
    }

    /** Quick recall — a bolt. */
    val Bolt: ImageVector = icon("bolt") {
        moveTo(13f, 2f); lineTo(5f, 13.5f); lineTo(11f, 13.5f); lineTo(10f, 22f)
        lineTo(19f, 9.5f); lineTo(12.5f, 9.5f); close()
    }

    /** Code Rush / timed practice — a stopwatch. */
    val Timer: ImageVector = icon("timer") {
        circle(12f, 13.5f, 7.5f)
        moveTo(12f, 6f); verticalLineTo(3f)
        moveTo(9.5f, 3f); horizontalLineTo(14.5f)
        moveTo(12f, 13.5f); verticalLineTo(9.5f)
        moveTo(18.4f, 7.6f); lineTo(20f, 6f)
    }

    /** Continue roadmap — a flag on the path. */
    val Flag: ImageVector = icon("flag", fill = { circle(6.5f, 21f, 1.7f) }) {
        moveTo(6.5f, 20f); verticalLineTo(4f)
        moveTo(6.5f, 4.5f); lineTo(18f, 8f); lineTo(6.5f, 11.5f)
    }

    /** Review — a stack of cards. */
    val Cards: ImageVector = icon("cards") {
        roundRect(6f, 4.5f, 14f, 11f, 2f)
        roundRect(4f, 8f, 14f, 11f, 2f)
    }

    /** Complete workout — a checklist. */
    val Checklist: ImageVector = icon("checklist") {
        roundRect(3.5f, 4f, 4.5f, 4.5f, 1f)
        moveTo(4.6f, 6.2f); lineTo(5.6f, 7.2f); lineTo(7.2f, 5.2f)
        roundRect(3.5f, 10f, 4.5f, 4.5f, 1f)
        roundRect(3.5f, 16f, 4.5f, 4.5f, 1f)
        moveTo(11f, 6.25f); horizontalLineTo(20.5f)
        moveTo(11f, 12.25f); horizontalLineTo(20.5f)
        moveTo(11f, 18.25f); horizontalLineTo(18f)
    }

    /** Learn the concept — a bulb. */
    val Bulb: ImageVector = icon("bulb") {
        circle(12f, 9.5f, 5.5f)
        moveTo(10.4f, 10f); lineTo(12f, 7.5f); lineTo(13.6f, 10f)
        moveTo(9.5f, 15.5f); horizontalLineTo(14.5f)
        moveTo(10.5f, 18.5f); horizontalLineTo(13.5f)
    }

    /** Behavioral workouts — a star. */
    val Star: ImageVector = icon("star") {
        moveTo(12f, 3f); lineTo(14.1f, 9.1f); lineTo(20.6f, 9.2f); lineTo(15.4f, 13.1f)
        lineTo(17.3f, 19.3f); lineTo(12f, 15.6f); lineTo(6.7f, 19.3f); lineTo(8.6f, 13.1f)
        lineTo(3.4f, 9.2f); lineTo(9.9f, 9.1f); close()
    }

    /** Technical communication — a speech bubble. */
    val Bubble: ImageVector = icon("bubble", fill = {
        circle(9f, 10.5f, 0.95f); circle(12f, 10.5f, 0.95f); circle(15f, 10.5f, 0.95f)
    }) {
        roundRect(3f, 4f, 18f, 13f, 3.5f)
        moveTo(8f, 16.5f); lineTo(8f, 21f); lineTo(12.5f, 16.5f)
    }

    /** Mock interview — a conversation. */
    val Bubbles: ImageVector = icon("bubbles") {
        roundRect(2.5f, 3.5f, 13f, 9.5f, 3f)
        moveTo(6f, 12.5f); lineTo(6f, 16.5f); lineTo(9.5f, 12.5f)
        roundRect(10.5f, 11f, 11f, 8.5f, 3f)
        moveTo(18f, 19f); lineTo(18f, 22.5f); lineTo(14.5f, 19f)
    }

    /** Rating — a gem. */
    val Diamond: ImageVector = icon("diamond") {
        moveTo(12f, 2.5f); lineTo(20.5f, 12f); lineTo(12f, 21.5f); lineTo(3.5f, 12f); close()
    }
}

// --------------------------------------------------------------------- builders

private fun icon(
    name: String,
    fill: (PathBuilder.() -> Unit)? = null,
    stroke: PathBuilder.() -> Unit,
): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    if (fill != null) {
        builder.addPath(PathData(fill), fill = SolidColor(Color.Black))
    }
    builder.addPath(
        pathData = PathData(stroke),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.6f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
    return builder.build()
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, false, true, r * 2, 0f)
    arcToRelative(r, r, 0f, false, true, -r * 2, 0f)
    close()
}

private fun PathBuilder.roundRect(x: Float, y: Float, w: Float, h: Float, r: Float = 1.8f) {
    moveTo(x + r, y)
    lineTo(x + w - r, y)
    arcToRelative(r, r, 0f, false, true, r, r)
    lineTo(x + w, y + h - r)
    arcToRelative(r, r, 0f, false, true, -r, r)
    lineTo(x + r, y + h)
    arcToRelative(r, r, 0f, false, true, -r, -r)
    lineTo(x, y + r)
    arcToRelative(r, r, 0f, false, true, r, -r)
    close()
}
