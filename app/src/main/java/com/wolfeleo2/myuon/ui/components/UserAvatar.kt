package com.wolfeleo2.myuon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.wolfeleo2.myuon.data.model.AvatarConfig
import com.wolfeleo2.myuon.data.model.AvatarProvider

@Composable
fun UserAvatar(
    seed: String,
    modifier: Modifier = Modifier,
    config: AvatarConfig = LocalAvatarConfig.current,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val url = config.buildUrl(seed)

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seed.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

/**
 * Backward-compatible helper for DiceBear specific invocations.
 */
@Composable
fun DiceBearAvatar(
    seed: String,
    modifier: Modifier = Modifier,
    style: String = "notionists",
    contentDescription: String? = null
) {
    UserAvatar(
        seed = seed,
        modifier = modifier,
        config = AvatarConfig(provider = AvatarProvider.DICEBEAR_PORTRAITS, style = style),
        contentDescription = contentDescription
    )
}
