package dev.curated.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.curated.app.presentation.theme.CuratedTheme
import dev.curated.app.presentation.utils.LocalOfflineMode
import dev.curated.app.viewmodels.MainViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.applyCuratedHighRefreshRatePreference()
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()

            CuratedTheme {
                val navController = rememberNavController()
                if (!state.isLoading) {
                    CompositionLocalProvider(LocalOfflineMode provides state.isOfflineMode) {
                        NavigationRoot(
                            navController = navController,
                            hasServers = state.hasServers,
                            hasCurrentServer = state.hasCurrentServer,
                            hasCurrentUser = state.hasCurrentUser,
                            isCuratedAuthLocked = state.isCuratedAuthLocked,
                        )
                    }
                }
            }
        }
    }
}
