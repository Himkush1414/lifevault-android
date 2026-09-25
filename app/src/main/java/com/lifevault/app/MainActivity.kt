package com.lifevault.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

// TODO(step 7 - navigation skeleton): replace the placeholder body with NavigationSuiteScaffold
// and the lock-gate-wrapped NavHost (Section 3.4).
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO(step 8 - app lock): make this conditional on the "Block screenshots & hide
        // in recents" setting (default ON, Section 8.6 / S19) instead of always-on.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            LifeVaultPlaceholderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "LifeVault")
                    }
                }
            }
        }
    }
}

// TODO(step 2 - design system): replace with core/designsystem's LifeVaultTheme
// (light/dark color schemes, Inter typography, motion tokens — Section 4).
@Composable
private fun LifeVaultPlaceholderTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
