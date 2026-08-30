package com.wolfeleo2.myuon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import com.wolfeleo2.myuon.data.repo.AuthRepository

private val TOP_LEVEL_DESTINATIONS: List<TopLevelDestination> =
    listOf(Dashboard, Academics, Fees, Timetable, Hostels)

/**
 * Owns the Navigation 3 back stack.
 *
 * Keeps one back stack per top-level tab (issue #13, "exit through home": the flat
 * [backStack] is always Dashboard's stack, followed by the current tab's stack if it
 * isn't Dashboard), and seeds its initial state from real auth state (issue #14) instead
 * of always assuming logged-in.
 *
 * [tabStacks] and [currentTopLevelState] are created by [rememberNavigator] using
 * `rememberSerializable` (issue #15), so this class's construction site is what actually
 * survives configuration changes *and* process death — every `NavKey` here is already
 * `@Serializable`, and Nav3's own [NavKeySerializer] resolves each concrete type via
 * reflection, so no manual `SerializersModule` registration is needed.
 */
class Navigator(
    private val tabStacks: Map<TopLevelDestination, SnapshotStateList<NavKey>>,
    private val currentTopLevelState: MutableState<TopLevelDestination>,
    authRepository: AuthRepository
) {
    private var currentTopLevel: TopLevelDestination
        get() = currentTopLevelState.value
        set(value) { currentTopLevelState.value = value }

    val backStack: SnapshotStateList<NavKey> = mutableStateListOf()

    init {
        // backStack itself isn't separately persisted - it's fully derived from tabStacks +
        // currentTopLevel (both restored by rememberNavigator before this runs), so a plain
        // re-sync here correctly reflects restored state after process death too.
        if (authRepository.isLoggedIn.value) {
            syncBackStack()
        } else {
            backStack.add(Login)
        }
    }

    val currentDestination: NavKey
        get() = backStack.lastOrNull() ?: Dashboard

    private fun syncBackStack() {
        backStack.clear()
        backStack.addAll(tabStacks.getValue(Dashboard))
        if (currentTopLevel != Dashboard) {
            backStack.addAll(tabStacks.getValue(currentTopLevel))
        }
    }

    fun goTo(destination: NavKey) {
        tabStacks.getValue(currentTopLevel).add(destination)
        syncBackStack()
    }

    fun switchTopLevel(destination: TopLevelDestination) {
        if (destination == currentTopLevel) return
        currentTopLevel = destination
        syncBackStack()
    }

    fun goBack(): Boolean {
        val currentStack = tabStacks.getValue(currentTopLevel)
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
            syncBackStack()
            return true
        }
        if (currentTopLevel != Dashboard) {
            currentTopLevel = Dashboard
            syncBackStack()
            return true
        }
        return false
    }

    fun replaceAll(destination: NavKey) {
        when (destination) {
            Login -> {
                tabStacks.forEach { (root, stack) ->
                    stack.clear()
                    stack.add(root)
                }
                currentTopLevel = Dashboard
                backStack.clear()
                backStack.add(Login)
            }

            Dashboard -> {
                currentTopLevel = Dashboard
                val dashboardStack = tabStacks.getValue(Dashboard)
                dashboardStack.clear()
                dashboardStack.add(Dashboard)
                syncBackStack()
            }

            else -> {
                backStack.clear()
                backStack.add(destination)
            }
        }
    }
}

/**
 * Creates and remembers a [Navigator] whose per-tab back stacks and current-tab state survive
 * configuration changes and process death (issue #15), via `rememberSerializable` + Nav 3's own
 * [NavKeySerializer] (no manual `SerializersModule` needed - it resolves each concrete `NavKey`
 * type via reflection).
 */
@Composable
fun rememberNavigator(authRepository: AuthRepository): Navigator {
    val tabStacks = TOP_LEVEL_DESTINATIONS.associateWith { root ->
        rememberSerializable(serializer = SnapshotStateListSerializer(NavKeySerializer<NavKey>())) {
            mutableStateListOf<NavKey>(root)
        }
    }

    val currentTopLevelState = rememberSerializable(
        serializer = MutableStateSerializer(NavKeySerializer<TopLevelDestination>())
    ) {
        mutableStateOf<TopLevelDestination>(Dashboard)
    }

    return remember { Navigator(tabStacks, currentTopLevelState, authRepository) }
}
