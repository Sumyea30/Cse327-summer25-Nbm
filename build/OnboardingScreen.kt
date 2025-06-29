package com.example.project_application

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Freely",
            description = "A great friend to chat with you.",
            imageRes = R.drawable.chat1
        ),
        OnboardingPage(
            title = "Feeling Confused?",
            description = "Just open Freely — your thoughts, cleared.",
            imageRes = R.drawable.chat2
        ),
        OnboardingPage(
            title = "Cheer Up, Buddy!",
            description = "Freely is always ready to listen and help.",
            imageRes = R.drawable.chat3
        ),
        OnboardingPage(
            title = "Let's Get Started!",
            description = "You're all set!",
            imageRes = R.drawable.chat4
        )
    )

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageUI(pages[page])

        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (pagerState.currentPage == pages.size - 1) {
                    onFinish()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next")
        }
    }
}

@Composable
fun OnboardingPageUI(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(page.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(page.description, fontSize = 16.sp)
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)
