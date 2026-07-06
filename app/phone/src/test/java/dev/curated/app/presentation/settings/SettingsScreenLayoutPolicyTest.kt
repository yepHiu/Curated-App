package dev.curated.app.presentation.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenLayoutPolicyTest {
    @Test
    fun rootSettingsUsesTopLevelNavigationChrome() {
        val source =
            projectFile("src/main/java/dev/curated/app/presentation/settings/SettingsScreen.kt")
                .readText()

        assertTrue(source.contains("onOpenNavigation: (() -> Unit)? = null"))
        assertTrue(source.contains("internal fun isSettingsRootRoute("))
        assertTrue(source.contains("CuratedPageHeader("))
        assertTrue(source.contains("CuratedNavigationMenuButton(onClick = it)"))
        assertTrue(source.contains("onAction(SettingsAction.OnBackClick)"))
        assertFalse(source.contains("Scaffold("))
        assertFalse(source.contains("TopAppBar("))
    }

    @Test
    fun navigationRootPassesDrawerOpenActionToSettingsScreen() {
        val source = projectFile("src/main/java/dev/curated/app/NavigationRoot.kt").readText()
        val settingsCall =
            source.substringAfter("SettingsScreen(").substringBefore("composable<AboutRoute>")

        assertTrue(settingsCall.contains("onOpenNavigation = onOpenNavigation"))
    }

    private fun projectFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/phone", relativePath))
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not find $relativePath from ${File(".").absoluteFile.normalize().path}")
    }
}
