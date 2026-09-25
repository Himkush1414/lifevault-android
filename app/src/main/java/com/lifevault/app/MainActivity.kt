package com.lifevault.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.designsystem.color.LifeVaultTheme
import com.lifevault.app.core.navigation.LifeVaultAppScaffold
import com.lifevault.app.feature.security.SecuritySettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safe default until the real setting loads (Section 8.6: default ON).
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            LifeVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val securityViewModel: SecuritySettingsViewModel = hiltViewModel()
                    val securityState by securityViewModel.securityState.collectAsState()
                    LaunchedEffect(securityState.blockScreenshots) {
                        if (securityState.blockScreenshots) {
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE,
                            )
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }

                    LifeVaultAppScaffold()
                }
            }
        }
    }
}
