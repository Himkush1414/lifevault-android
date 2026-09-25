package com.lifevault.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lifevault.app.core.designsystem.color.LifeVaultTheme
import com.lifevault.app.core.navigation.LifeVaultAppScaffold
import dagger.hilt.android.AndroidEntryPoint

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
            LifeVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LifeVaultAppScaffold()
                }
            }
        }
    }
}
