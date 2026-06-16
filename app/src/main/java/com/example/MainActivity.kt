package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomTab
import com.example.ui.components.MusicaBottomBar
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SplashScreen
import androidx.compose.ui.unit.dp
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmCreamBg
import com.example.viewmodel.MusicViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppFontFamily
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.graphicsLayer

class MainActivity : ComponentActivity() {

    private var musicViewModel: MusicViewModel? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            musicViewModel?.scanDeviceSongs(this)
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            musicViewModel?.scanDeviceSongs(this)
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val action = intent.action
        val dataUri = intent.data
        if (action == android.content.Intent.ACTION_VIEW && dataUri != null) {
            musicViewModel?.importSongFromUri(this, dataUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize preferences-backed ThemeState before compose begins
        com.example.ui.theme.ThemeState.initialize(this)
        
        // Enable premium modern Android Edge-to-Edge bleed
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }
                val viewModel: MusicViewModel = viewModel()
                musicViewModel = viewModel

                // Play incoming shared view intent file content
                androidx.compose.runtime.LaunchedEffect(viewModel) {
                    handleIntent(intent)
                }

                // Proactive check/scan of device songs on background thread during splash screen animation
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            permission
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.scanDeviceSongs(this@MainActivity)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(WarmCreamBg) // Strong background fix to solve dark theme transparency bleed
                ) {
                    if (showSplash) {
                        SplashScreen(
                            onSplashFinished = { 
                                showSplash = false 
                                checkAndRequestPermissions()
                            }
                        )
                    } else {
                        MusicaAppFrame(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MusicaAppFrame(
    viewModel: MusicViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isDimmed = state.showQueueSheet

    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var showSettings by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showSettings) {
        showSettings = false
    }

    androidx.activity.compose.BackHandler(enabled = !showSettings && selectedTab == BottomTab.LIBRARY) {
        selectedTab = BottomTab.HOME
    }
    
    // Manage status bar and bottom nav inset paddings cleanly
    val topInsetPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Smooth and perfectly synchronized bottom bar dimming animation that matches the player sheet
    val bottomBarDimAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.65f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "bottomBarDimAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = WarmCreamBg,
            bottomBar = {
                if (!showSettings) {
                    Box {
                        MusicaBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { if (!isDimmed) selectedTab = it }
                        )
                        if (bottomBarDimAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer { alpha = bottomBarDimAlpha }
                                    .background(Color.Black)
                                    .clickable(enabled = isDimmed) {} // absorbs touches/clicks completely
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = topInsetPadding,
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                AnimatedContent(
                    targetState = showSettings,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(200))
                    },
                    label = "settings_navigation"
                ) { isSettingsActive ->
                    if (isSettingsActive) {
                        SettingsScreen(
                            onBackPressed = { showSettings = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Animate transition smoothly when jumping between navigation tabs
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(200))
                            },
                            label = "tab_navigation"
                        ) { tab ->
                            when (tab) {
                                BottomTab.HOME -> {
                                    PlayerScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                BottomTab.LIBRARY -> {
                                    LibraryScreen(
                                        viewModel = viewModel,
                                        onSongSelected = { selectedTab = BottomTab.HOME },
                                        onNavigateToSettings = { showSettings = true },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val currentSong = state.currentSong
        if (!showSettings && currentSong != null && selectedTab == BottomTab.LIBRARY) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 72.dp + 10.dp)
                    .padding(horizontal = 24.dp)
                    .zIndex(10f)
            ) {
                com.example.ui.components.MiniPlayerCapsule(
                    song = currentSong,
                    isPlaying = state.isPlaying,
                    progressSeconds = state.progressSeconds,
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onPrev = { viewModel.previousSong() },
                    onNext = { viewModel.nextSong() },
                    onClick = { selectedTab = BottomTab.HOME },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
