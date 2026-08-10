package com.codingarena.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.codingarena.domain.model.ReviewLabel

/**
 * Chess-board green, borrowed deliberately: the product's whole framing is
 * "chess.com for coding interviews", and the colour does a lot of that work
 * before a single word is read.
 */
private val ArenaGreen = Color(0xFF5B8C3E)
private val ArenaGreenDark = Color(0xFF3E6329)
private val ArenaCream = Color(0xFFF4F1EA)
private val ArenaCharcoal = Color(0xFF1F1D1A)

private val LightColors = lightColorScheme(
    primary = ArenaGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE9CF),
    onPrimaryContainer = ArenaGreenDark,
    secondary = Color(0xFF6C7A5B),
    background = ArenaCream,
    onBackground = ArenaCharcoal,
    surface = Color.White,
    onSurface = ArenaCharcoal,
    surfaceVariant = Color(0xFFE7E3DA),
    onSurfaceVariant = Color(0xFF4A473F),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BC77A),
    onPrimary = Color(0xFF1B2913),
    primaryContainer = ArenaGreenDark,
    onPrimaryContainer = Color(0xFFDCE9CF),
    secondary = Color(0xFFA8B598),
    background = Color(0xFF16150F),
    onBackground = Color(0xFFE9E5DB),
    surface = Color(0xFF23211B),
    onSurface = Color(0xFFE9E5DB),
    surfaceVariant = Color(0xFF33302A),
    onSurfaceVariant = Color(0xFFC9C4B8),
    error = Color(0xFFF2B8B5),
)

/** Semantic colours for the review labels, mirrored across both schemes. */
data class ReviewColors(
    val brilliant: Color,
    val bestMove: Color,
    val good: Color,
    val inaccuracy: Color,
    val mistake: Color,
    val blunder: Color,
) {
    fun forLabel(label: ReviewLabel): Color = when (label) {
        ReviewLabel.BRILLIANT -> brilliant
        ReviewLabel.BEST_MOVE -> bestMove
        ReviewLabel.GOOD -> good
        ReviewLabel.INACCURACY -> inaccuracy
        ReviewLabel.MISTAKE -> mistake
        ReviewLabel.BLUNDER -> blunder
    }
}

val LightReviewColors = ReviewColors(
    brilliant = Color(0xFF1BAAAA),
    bestMove = Color(0xFF5B8C3E),
    good = Color(0xFF7BA05B),
    inaccuracy = Color(0xFFD9A33A),
    mistake = Color(0xFFDE7C2E),
    blunder = Color(0xFFC0392B),
)

val DarkReviewColors = ReviewColors(
    brilliant = Color(0xFF43D2D2),
    bestMove = Color(0xFF9BC77A),
    good = Color(0xFFAFCB94),
    inaccuracy = Color(0xFFF0C46B),
    mistake = Color(0xFFF29C5D),
    blunder = Color(0xFFE8756A),
)

private val ArenaTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

/** Monospace style for every code snippet the app renders. */
val CodeTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

@Composable
fun ArenaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ArenaTypography,
        content = content,
    )
}

/** Review colours matching the active theme. */
@Composable
fun reviewColors(darkTheme: Boolean = isSystemInDarkTheme()): ReviewColors =
    if (darkTheme) DarkReviewColors else LightReviewColors
