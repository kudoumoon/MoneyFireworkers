package com.moneyfireworkers.paytrack.feature.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyAccent
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackground
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBackgroundWarm
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyBerry
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyButter
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyDivider
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyGlow
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyMint
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPeach
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyPrimaryDeep
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySky
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurface
import com.moneyfireworkers.paytrack.core.ui.theme.MoneySurfaceRaised
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextPrimary
import com.moneyfireworkers.paytrack.core.ui.theme.MoneyTextSecondary

data class ScreenBackdropPalette(
    val top: Color,
    val middle: Color,
    val bottom: Color,
    val glow: Color,
    val accent: Color,
)

val HomeBackdropPalette = ScreenBackdropPalette(
    top = MoneyBackgroundWarm,
    middle = Color(0xFFFFF7F1),
    bottom = MoneyBackground,
    glow = MoneyMint,
    accent = MoneyPeach,
)

val AddBackdropPalette = ScreenBackdropPalette(
    top = Color(0xFFFFF8F2),
    middle = Color(0xFFFFF3EB),
    bottom = MoneyBackground,
    glow = MoneyPeach,
    accent = MoneyGlow,
)

val StatsBackdropPalette = ScreenBackdropPalette(
    top = Color(0xFFFBF8F2),
    middle = Color(0xFFF6F8F2),
    bottom = MoneyBackground,
    glow = MoneySky,
    accent = MoneyButter,
)

val CategoryBackdropPalette = ScreenBackdropPalette(
    top = MoneyBackgroundWarm,
    middle = Color(0xFFF9F6EE),
    bottom = MoneyBackground,
    glow = MoneyButter,
    accent = MoneyMint,
)

val ProfileBackdropPalette = ScreenBackdropPalette(
    top = Color(0xFFFFF8F3),
    middle = Color(0xFFF9F4EC),
    bottom = MoneyBackground,
    glow = MoneyGlow,
    accent = MoneyBerry.copy(alpha = 0.55f),
)

@Composable
fun GentleScreen(
    modifier: Modifier = Modifier,
    palette: ScreenBackdropPalette = HomeBackdropPalette,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(palette.top, palette.middle, palette.bottom),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = palette.glow.copy(alpha = 0.34f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.18f, size.height * 0.12f),
            )
            drawCircle(
                color = palette.accent.copy(alpha = 0.24f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.84f, size.height * 0.24f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = size.minDimension * 0.16f,
                center = Offset(size.width * 0.72f, size.height * 0.08f),
                style = Stroke(width = 3f),
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                ),
                topLeft = Offset(size.width * 0.08f, size.height * 0.58f),
                size = Size(size.width * 0.84f, size.height * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(72f, 72f),
            )
        }
        content()
    }
}

@Composable
fun AppScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MoneySurface.copy(alpha = 0.82f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        ) {
            Text(
                text = eyebrow,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MoneyPrimaryDeep,
            )
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MoneyTextPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MoneyTextSecondary)
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.985f else 1f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "glass_card_scale",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed && onClick != null) 4.dp else 10.dp,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "glass_card_elevation",
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MoneySurface.copy(alpha = 0.78f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MoneySurfaceRaised.copy(alpha = 0.96f),
                            Color.White.copy(alpha = 0.70f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.88f), MoneyDivider.copy(alpha = 0.18f)),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(18.dp),
        ) {
            content()
        }
    }
}

@Composable
fun EmotionPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun AmbientOrb(
    modifier: Modifier = Modifier,
    emoji: String,
    color: Color,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(color.copy(alpha = 0.22f), Color.White.copy(alpha = 0.72f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun AnimatedLaunchScene(
    modifier: Modifier = Modifier,
    progress: Float,
) {
    val loop = rememberInfiniteTransition(label = "launch")
    val pulse by loop.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val drift by loop.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )
    val shimmer by loop.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFCF7),
                        MoneyGlow.copy(alpha = 0.82f),
                        MoneyBackgroundWarm,
                    ),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.42f)

            drawCircle(
                color = MoneyPrimary.copy(alpha = 0.08f),
                radius = size.minDimension * 0.34f,
                center = Offset(center.x, center.y - size.height * 0.02f),
            )
            drawCircle(
                color = MoneyPeach.copy(alpha = 0.09f),
                radius = size.minDimension * 0.22f,
                center = Offset(center.x * 0.76f, center.y * 0.92f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = size.minDimension * 0.12f,
                center = Offset(size.width * 0.72f, size.height * 0.22f),
                style = Stroke(width = 2f),
            )

            val particles = listOf(
                Triple(0.18f, 0.30f, 11f),
                Triple(0.78f, 0.18f, 8f),
                Triple(0.84f, 0.63f, 10f),
                Triple(0.24f, 0.72f, 7f),
                Triple(0.62f, 0.78f, 6f),
            )
            particles.forEachIndexed { index, particle ->
                val wobble = (index + 1) * 0.7f
                val offsetX = particle.first * size.width + drift * wobble * 8f
                val offsetY = particle.second * size.height + shimmer * wobble * 6f
                drawCircle(
                    color = if (index % 2 == 0) {
                        MoneyPrimary.copy(alpha = 0.14f)
                    } else {
                        MoneyAccent.copy(alpha = 0.10f)
                    },
                    radius = particle.third * (0.92f + pulse * 0.08f),
                    center = Offset(offsetX, offsetY),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = progress.coerceIn(0f, 1f)
                    scaleX = 0.94f + progress * 0.06f
                    scaleY = 0.94f + progress * 0.06f
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(138.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White.copy(alpha = 0.14f)),
                )
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .scale(pulse)
                        .clip(RoundedCornerShape(34.dp))
                        .background(Brush.linearGradient(listOf(MoneyPrimaryDeep, MoneyPrimary))),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(15.dp)
                            .size(10.dp)
                            .background(MoneyPeach.copy(alpha = 0.96f), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(18.dp)
                            .size(width = 18.dp, height = 5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.74f)),
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.90f)),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .size(width = 34.dp, height = 8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MoneyGlow.copy(alpha = 0.90f)),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "MoneyFireworkers",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MoneyTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "让每一笔花费，都有被温柔看见的空间。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MoneyTextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
