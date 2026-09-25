package com.lifevault.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing
import kotlinx.coroutines.launch

private data class OnboardingPage(val icon: Int, val title: String, val description: String)

// Section 3.3 S02 copy.
private val Pages = listOf(
    OnboardingPage(
        LifeVaultIcons.Core.Documents,
        "All your important papers, one private vault.",
        "Passports, licences, insurance, warranties — everything in one encrypted place.",
    ),
    OnboardingPage(
        LifeVaultIcons.Core.Scan,
        "Scan once. We'll read the dates for you.",
        "On-device OCR suggests the category, number and expiry date automatically.",
    ),
    OnboardingPage(
        LifeVaultIcons.Core.Reminders,
        "Reminders before anything expires. 100% offline, encrypted.",
        "Nothing ever leaves your phone — no accounts, no servers, no tracking.",
    ),
)

/** S02 (Section 3.3): 3-page onboarding pager. */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { Pages.size })
    val scope = rememberCoroutineScope()

    fun finish() {
        viewModel.markOnboardingCompleted()
        onFinished()
    }

    Box(modifier = modifier.fillMaxSize()) {
        TextButton(
            onClick = ::finish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Spacing.lg),
        ) {
            Text("Skip")
        }

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                OnboardingPageContent(Pages[page])
            }

            PageIndicator(
                pageCount = Pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = Spacing.xl),
            )

            val isLastPage = pagerState.currentPage == Pages.lastIndex
            Button(
                onClick = {
                    if (isLastPage) {
                        finish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.lg)
                    .height(56.dp),
            ) {
                Text(if (isLastPage) "Get started" else "Next")
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(page.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(96.dp),
            )
        }
        Spacer(Modifier.size(Spacing.xxl))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp),
        )
        Spacer(Modifier.size(Spacing.md))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 360.dp),
        )
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        for (index in 0 until pageCount) {
            val active = index == currentPage
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (active) 24.dp else 8.dp)
                    .background(
                        color = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}
