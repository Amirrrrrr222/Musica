package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepSoil
import com.example.ui.theme.SandBrown
import com.example.ui.theme.WarmCreamBg
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0.0f) }

    LaunchedEffect(key1 = true) {
        // Run entry animations in parallel
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 700)
        )
    }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 500)
        )
        // Keep splash short and professional
        delay(600)
        // Transition outward animation
        alpha.animateTo(
            targetValue = 0.0f,
            animationSpec = tween(durationMillis = 200)
        )
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.5.dp, SandBrown, RoundedCornerShape(28.dp))
            ) {
                com.example.ui.components.MusicaLogo(
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Musica",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = DeepSoil,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pure sound. Truly free.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                color = SandBrown,
                letterSpacing = 1.sp
            )
        }
    }
}
