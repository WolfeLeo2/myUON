package com.wolfeleo2.myuon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wolfeleo2.myuon.ui.theme.Sizes
import com.wolfeleo2.myuon.ui.theme.Spacing

/**
 * Expressive Shimmer Skeleton Modifier.
 * Animates a physics-inspired gradient shimmer across the container.
 */
fun Modifier.shimmer(
    shape: Shape = RoundedCornerShape(12.dp),
    durationMillis: Int = 1200
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnimation - 300f, translateAnimation - 300f),
        end = Offset(translateAnimation, translateAnimation)
    )

    this
        .clip(shape)
        .background(brush)
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier.shimmer(shape = shape)
    )
}

@Composable
fun SkeletonText(
    width: Dp = 120.dp,
    height: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    modifier: Modifier = Modifier
) {
    SkeletonBox(
        shape = shape,
        modifier = modifier
            .width(width)
            .height(height)
    )
}

@Composable
fun SkeletonCircle(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    SkeletonBox(
        shape = CircleShape,
        modifier = modifier.size(size)
    )
}

/**
 * Dashboard Skeleton State
 */
@Composable
fun DashboardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Hero Student Card Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(Sizes.cardCornerRadiusLg)
        )

        // Quick Action Buttons Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            repeat(3) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp),
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusSm)
                )
            }
        }

        // Exam Banner Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(Sizes.cardCornerRadius)
        )

        // Metrics Grid Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            repeat(2) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp),
                    shape = RoundedCornerShape(Sizes.cardCornerRadiusSm)
                )
            }
        }

        // Today Schedule Skeletons
        repeat(2) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
                shape = RoundedCornerShape(Sizes.cardCornerRadiusSm)
            )
        }
    }
}

/**
 * Academics / Gradebook Skeleton State
 */
@Composable
fun AcademicsSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(Sizes.cardCornerRadius)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            repeat(4) {
                SkeletonBox(
                    modifier = Modifier
                        .width(70.dp)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        repeat(4) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(Sizes.cardCornerRadiusSm)
            )
        }
    }
}

/**
 * Fees Statement Skeleton State
 */
@Composable
fun FeesSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            shape = RoundedCornerShape(Sizes.cardCornerRadiusLg)
        )

        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(Sizes.cardCornerRadiusSm)
        )

        repeat(5) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/**
 * Timetable Skeleton State
 */
@Composable
fun TimetableSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            repeat(5) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        repeat(4) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(Sizes.cardCornerRadius)
            )
        }
    }
}

/**
 * Hostels Skeleton State
 */
@Composable
fun HostelsSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(Sizes.cardCornerRadiusLg)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            repeat(3) {
                SkeletonBox(
                    modifier = Modifier
                        .width(90.dp)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        repeat(3) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(Sizes.cardCornerRadius)
            )
        }
    }
}
