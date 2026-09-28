package com.yadar.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.yadar.app.R

enum class YadarRoute(val route: String) {
    HOME("home"),
    SENTENCES("sentences"),
    ADD_EDIT_SENTENCE("add_edit_sentence?sentenceId={sentenceId}"),
    COLLECTIONS("collections"),
    SCHEDULE("schedule"),
    SETTINGS("settings")
}

data class BottomNavItem(
    val route: YadarRoute,
    val labelRes: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(YadarRoute.HOME, R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(YadarRoute.SENTENCES, R.string.nav_sentences, Icons.Filled.List),
    BottomNavItem(YadarRoute.COLLECTIONS, R.string.nav_collections, Icons.Filled.Menu),
    BottomNavItem(YadarRoute.SCHEDULE, R.string.nav_schedule, Icons.Filled.DateRange),
    BottomNavItem(YadarRoute.SETTINGS, R.string.nav_settings, Icons.Filled.Settings)
)

fun addEditSentenceRoute(sentenceId: Long?): String =
    "add_edit_sentence?sentenceId=${sentenceId ?: -1L}"
