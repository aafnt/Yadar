package com.yadar.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.yadar.app.YadarApplication
import com.yadar.app.domain.model.AppThemeMode
import com.yadar.app.ui.collections.CollectionsScreen
import com.yadar.app.ui.collections.CollectionsViewModel
import com.yadar.app.ui.home.HomeScreen
import com.yadar.app.ui.home.HomeViewModel
import com.yadar.app.ui.navigation.YadarRoute
import com.yadar.app.ui.navigation.addEditSentenceRoute
import com.yadar.app.ui.navigation.bottomNavItems
import com.yadar.app.ui.schedule.ScheduleScreen
import com.yadar.app.ui.schedule.ScheduleViewModel
import com.yadar.app.ui.sentences.AddEditSentenceScreen
import com.yadar.app.ui.sentences.AddEditSentenceViewModel
import com.yadar.app.ui.sentences.SentencesScreen
import com.yadar.app.ui.sentences.SentencesViewModel
import com.yadar.app.ui.settings.SettingsScreen
import com.yadar.app.ui.settings.SettingsViewModel
import com.yadar.app.ui.theme.YadarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as YadarApplication).container

        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = viewModelFactory {
                initializer { SettingsViewModel(container.settingsRepository, container.exportBackupUseCase, container.importBackupUseCase) }
            })
            val settings by settingsVm.settings.collectAsState()

            // یادآر یک اپلیکیشن کاملاً فارسی و RTL است؛ جهت UI فارغ از زبان سیستم همیشه راست‌به‌چپ است (بند ۴).
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                YadarTheme(
                    themeMode = settings.themeMode,
                    accentColor = androidx.compose.ui.graphics.Color(settings.accentColorArgb)
                ) {
                    YadarApp(container = container, settingsVm = settingsVm)
                }
            }
        }
    }
}

@Composable
private fun YadarApp(container: com.yadar.app.di.AppContainer, settingsVm: SettingsViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route.route,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                        label = { Text(stringResource(item.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = YadarRoute.HOME.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(YadarRoute.HOME.route) {
                val vm: HomeViewModel = viewModel(factory = viewModelFactory {
                    initializer { HomeViewModel(container.getHomeSummaryUseCase) }
                })
                HomeScreen(
                    viewModel = vm,
                    onAddSentence = { navController.navigate(addEditSentenceRoute(null)) },
                    onManageWidgets = { navController.navigate(YadarRoute.SCHEDULE.route) }
                )
            }
            composable(YadarRoute.SENTENCES.route) {
                val vm: SentencesViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        SentencesViewModel(
                            container.sentenceRepository,
                            container.collectionRepository,
                            container.deleteSentenceUseCase,
                            container.duplicateSentenceUseCase,
                            container.toggleSentenceActiveUseCase,
                            container.moveSentenceToCollectionUseCase,
                            container.reorderSentencesManuallyUseCase
                        )
                    }
                })
                SentencesScreen(
                    viewModel = vm,
                    onAddSentence = { navController.navigate(addEditSentenceRoute(null)) },
                    onEditSentence = { id -> navController.navigate(addEditSentenceRoute(id)) }
                )
            }
            composable(
                route = YadarRoute.ADD_EDIT_SENTENCE.route,
                arguments = listOf(navArgument("sentenceId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStackEntry ->
                val sentenceIdArg = backStackEntry.arguments?.getLong("sentenceId") ?: -1L
                val sentenceId = sentenceIdArg.takeIf { it != -1L }
                val vm: AddEditSentenceViewModel = viewModel(
                    key = "add_edit_$sentenceIdArg",
                    factory = viewModelFactory {
                        initializer {
                            AddEditSentenceViewModel(
                                sentenceId,
                                container.sentenceRepository,
                                container.collectionRepository,
                                container.addSentenceUseCase,
                                container.updateSentenceUseCase
                            )
                        }
                    }
                )
                AddEditSentenceScreen(viewModel = vm, onDone = { navController.popBackStack() })
            }
            composable(YadarRoute.COLLECTIONS.route) {
                val vm: CollectionsViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        CollectionsViewModel(
                            container.collectionRepository,
                            container.addCollectionUseCase,
                            container.updateCollectionUseCase,
                            container.setCollectionActiveUseCase,
                            container.deleteCollectionUseCase
                        )
                    }
                })
                CollectionsScreen(viewModel = vm)
            }
            composable(YadarRoute.SCHEDULE.route) {
                val vm: ScheduleViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        ScheduleViewModel(container.widgetConfigRepository, container.collectionRepository, container.clearWidgetHistoryUseCase)
                    }
                })
                ScheduleScreen(viewModel = vm)
            }
            composable(YadarRoute.SETTINGS.route) {
                SettingsScreen(viewModel = settingsVm)
            }
        }
    }
}
