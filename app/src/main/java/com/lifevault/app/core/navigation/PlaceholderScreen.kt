package com.lifevault.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Stands in for every screen (S02–S24) until its own build-order step implements it.
 * The route's own screen (e.g. the real S06 Home in Step 15) replaces this composable
 * at its call site in [LifeVaultNavHost] — nothing else needs to change.
 */
@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
    }
}
