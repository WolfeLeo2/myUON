package com.wolfeleo2.myuon.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * Ambient access to the app's single [SharedTransitionScope] (created once in
 * `MyUonNavDisplay`'s `SharedTransitionLayout`), so any screen can build container-transform
 * `Modifier.sharedBounds`/`sharedElement` calls without threading the scope through every
 * screen's parameter list. Paired with Nav 3's own (already-ambient)
 * `androidx.navigation3.ui.LocalNavAnimatedContentScope`.
 *
 * Defaults to `null` outside of `MyUonNavDisplay` (e.g. in `@Preview`s) - callers should no-op
 * shared-element modifiers when this is null rather than crash.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
