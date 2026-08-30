package com.wolfeleo2.myuon.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.wolfeleo2.myuon.data.repo.AcademicRepository
import com.wolfeleo2.myuon.data.repo.AuthRepository
import com.wolfeleo2.myuon.data.repo.ThemeRepository
import com.wolfeleo2.myuon.ui.academics.*
import com.wolfeleo2.myuon.ui.auth.LoginScreen
import com.wolfeleo2.myuon.ui.auth.LoginViewModel
import com.wolfeleo2.myuon.ui.components.AnnouncementsSheet
import com.wolfeleo2.myuon.ui.components.ExpressiveNavigationBar
import com.wolfeleo2.myuon.ui.components.LocalSharedTransitionScope
import com.wolfeleo2.myuon.ui.components.LocalAvatarConfig
import com.wolfeleo2.myuon.ui.components.LocalStudentProfile
import com.wolfeleo2.myuon.ui.components.LocalUonGlobalActions
import com.wolfeleo2.myuon.ui.components.ProfileSettingsSheet
import com.wolfeleo2.myuon.ui.components.UonGlobalActions
import com.wolfeleo2.myuon.ui.dashboard.DashboardScreen
import com.wolfeleo2.myuon.ui.dashboard.DashboardViewModel
import com.wolfeleo2.myuon.ui.fees.FeesScreen
import com.wolfeleo2.myuon.ui.fees.FeesViewModel
import com.wolfeleo2.myuon.ui.hostels.HostelsScreen
import com.wolfeleo2.myuon.ui.hostels.HostelsViewModel
import com.wolfeleo2.myuon.ui.timetable.TimetableScreen
import com.wolfeleo2.myuon.ui.timetable.TimetableViewModel
import com.wolfeleo2.myuon.ui.venues.VenueMapScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MyUonNavDisplay(
    navigator: Navigator,
    authRepository: AuthRepository,
    academicRepository: AcademicRepository,
    themeRepository: ThemeRepository,
    modifier: Modifier = Modifier
) {
    val currentDestination = navigator.currentDestination
    val isTopLevel = currentDestination is TopLevelDestination

    var showProfileSheet by remember { mutableStateOf(false) }
    var showAnnouncementsSheet by remember { mutableStateOf(false) }

    val studentProfile by authRepository.studentProfile.collectAsState()
    val useDynamicColor by themeRepository.useDynamicColor.collectAsState()
    val isDarkMode by themeRepository.isDarkMode.collectAsState()
    val avatarConfig by themeRepository.avatarConfig.collectAsState()
    val examTimetableList by academicRepository.examTimetable.collectAsState()
    val attendanceSummary by academicRepository.attendanceSummary.collectAsState()

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                ExpressiveNavigationBar(
                    currentDestination = currentDestination,
                    onTabSelected = { topLevel ->
                        navigator.switchTopLevel(topLevel)
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        SharedTransitionLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isTopLevel) innerPadding.calculateBottomPadding() else innerPadding.calculateBottomPadding())
        ) {
          CompositionLocalProvider(
              LocalSharedTransitionScope provides this@SharedTransitionLayout,
              LocalStudentProfile provides studentProfile,
              LocalAvatarConfig provides avatarConfig,
              LocalUonGlobalActions provides UonGlobalActions(
                  onProfileClick = { showProfileSheet = true },
                  onNotificationsClick = { showAnnouncementsSheet = true }
              )
          ) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<Login> {
                        val viewModel: LoginViewModel = hiltViewModel()
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { navigator.replaceAll(Dashboard) }
                        )
                    }

                    entry<Dashboard> {
                        val viewModel: DashboardViewModel = hiltViewModel()
                        DashboardScreen(
                            viewModel = viewModel,
                            onOpenExamCard = { navigator.goTo(ExamCardScreen) },
                            onOpenAcademics = { navigator.switchTopLevel(Academics) },
                            onOpenFees = { navigator.switchTopLevel(Fees) },
                            onOpenTimetable = { navigator.switchTopLevel(Timetable) },
                            onOpenHostels = { navigator.switchTopLevel(Hostels) },
                            onOpenUnitDetail = { unitCode -> navigator.goTo(UnitDetail(unitCode)) },
                            onOpenVenueMap = { venue -> navigator.goTo(VenueMapScreen(venue)) },
                            onOpenExamTimetable = { navigator.goTo(ExamTimetableRoute) },
                            onOpenAttendance = { navigator.goTo(AttendanceAnalyticsRoute("CSC 311")) },
                            onProfileClick = { showProfileSheet = true },
                            onNotificationsClick = { showAnnouncementsSheet = true }
                        )
                    }

                    entry<Academics> {
                        val viewModel: AcademicsViewModel = hiltViewModel()
                        AcademicsScreen(
                            viewModel = viewModel,
                            onOpenExamCard = { navigator.goTo(ExamCardScreen) },
                            onOpenUnitDetail = { unitCode -> navigator.goTo(UnitDetail(unitCode)) },
                            onOpenSpecialExam = { unit -> navigator.goTo(SpecialExamRequestScreen(unit)) },
                            onOpenSupplementary = { navigator.goTo(SupplementaryRequestScreen) },
                            onOpenMissingMarks = { navigator.goTo(MissingMarksDisputeScreen) },
                            onOpenExamTimetable = { navigator.goTo(ExamTimetableRoute) },
                            onOpenAttendance = { unitCode -> navigator.goTo(AttendanceAnalyticsRoute(unitCode)) },
                            onProfileClick = { showProfileSheet = true },
                            onNotificationsClick = { showAnnouncementsSheet = true }
                        )
                    }

                    entry<UnitDetail> { key ->
                        val viewModel: AcademicsViewModel = hiltViewModel()
                        UnitDetailScreen(
                            unitCode = key.unitCode,
                            viewModel = viewModel,
                            onOpenVenueMap = { venue -> navigator.goTo(VenueMapScreen(venue)) },
                            onOpenSpecialExam = { unit -> navigator.goTo(SpecialExamRequestScreen(unit)) },
                            onOpenAttendance = { unitCode -> navigator.goTo(AttendanceAnalyticsRoute(unitCode)) },
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<ExamTimetableRoute> {
                        ExamTimetableScreen(
                            examTimetable = examTimetableList,
                            onOpenVenueMap = { venue -> navigator.goTo(VenueMapScreen(venue)) },
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<AttendanceAnalyticsRoute> { key ->
                        AttendanceAnalyticsScreen(
                            unitCode = key.unitCode,
                            attendanceSummary = attendanceSummary,
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<Fees> {
                        val viewModel: FeesViewModel = hiltViewModel()
                        FeesScreen(
                            viewModel = viewModel,
                            onProfileClick = { showProfileSheet = true },
                            onNotificationsClick = { showAnnouncementsSheet = true }
                        )
                    }

                    entry<Timetable> {
                        val viewModel: TimetableViewModel = hiltViewModel()
                        TimetableScreen(
                            viewModel = viewModel,
                            onOpenUnitDetail = { unitCode -> navigator.goTo(UnitDetail(unitCode)) },
                            onOpenVenueMap = { venue -> navigator.goTo(VenueMapScreen(venue)) },
                            onProfileClick = { showProfileSheet = true },
                            onNotificationsClick = { showAnnouncementsSheet = true }
                        )
                    }

                    entry<Hostels> {
                        val viewModel: HostelsViewModel = hiltViewModel()
                        HostelsScreen(
                            viewModel = viewModel,
                            onProfileClick = { showProfileSheet = true },
                            onNotificationsClick = { showAnnouncementsSheet = true }
                        )
                    }

                    entry<ExamCardScreen> {
                        val viewModel: AcademicsViewModel = hiltViewModel()
                        ExamCardScreen(
                            viewModel = viewModel,
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<SpecialExamRequestScreen> { key ->
                        val viewModel: AcademicsViewModel = hiltViewModel()
                        SpecialExamScreen(
                            viewModel = viewModel,
                            prefillUnitCode = key.prefillUnitCode,
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<SupplementaryRequestScreen> {
                        val viewModel: AcademicsViewModel = hiltViewModel()
                        SupplementaryScreen(
                            viewModel = viewModel,
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<MissingMarksDisputeScreen> {
                        val viewModel: AcademicsViewModel = hiltViewModel()
                        MissingMarksScreen(
                            viewModel = viewModel,
                            onBack = { navigator.goBack() }
                        )
                    }

                    entry<VenueMapScreen> { key ->
                        VenueMapScreen(
                            initialVenueCode = key.venueCode,
                            onBack = { navigator.goBack() }
                        )
                    }
                }
            )
          }
        }

        // Modal Profile Settings Sheet
        if (showProfileSheet) {
            ProfileSettingsSheet(
                student = studentProfile,
                avatarConfig = avatarConfig,
                onSelectAvatarConfig = { themeRepository.setAvatarConfig(it) },
                isDarkMode = isDarkMode,
                onSelectDarkMode = { themeRepository.setDarkMode(it) },
                useDynamicColor = useDynamicColor,
                onToggleDynamicColor = { themeRepository.setDynamicColor(it) },
                isBiometricEnabled = studentProfile?.isBiometricEnabled ?: true,
                onToggleBiometric = { authRepository.toggleBiometric(it) },
                onLogout = {
                    authRepository.logout()
                    navigator.replaceAll(Login)
                },
                onDismissRequest = { showProfileSheet = false }
            )
        }

        // Modal Announcements Hub Sheet
        if (showAnnouncementsSheet) {
            AnnouncementsSheet(
                onDismissRequest = { showAnnouncementsSheet = false }
            )
        }
    }
}
