package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.IptvChannel

@Composable
fun VideoPlayer(
    channel: IptvChannel,
    onToggleFavorite: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPreparing by remember(channel.streamUrl) { mutableStateOf(true) }
    var hasError by remember(channel.streamUrl) { mutableStateOf(false) }
    var showControllerOverlay by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }

    // Auto-hide overlay after 4 seconds of idle
    LaunchedEffect(showControllerOverlay, isPlaying) {
        if (showControllerOverlay && isPlaying) {
            kotlinx.coroutines.delay(4000)
            showControllerOverlay = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControllerOverlay = !showControllerOverlay
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    videoViewInstance = this
                    
                    // Simple Android MediaController can be attached, but we build a custom styled compose interface on top
                    setOnPreparedListener { mp ->
                        isPreparing = false
                        hasError = false
                        mp.isLooping = true
                        start()
                        isPlaying = true
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        isPreparing = false
                        hasError = true
                        isPlaying = false
                        true // Handled
                    }
                }
            },
            update = { view ->
                // Reload when streamUrl changes
                isPreparing = true
                hasError = false
                isPlaying = false
                try {
                    view.stopPlayback()
                    view.setVideoURI(Uri.parse(channel.streamUrl))
                } catch (e: Exception) {
                    isPreparing = false
                    hasError = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Overlay Controller UI
        AnimatedVisibility(
            visible = showControllerOverlay || isPreparing || hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = channel.groupTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Row {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite Toggle",
                                tint = if (channel.isFavorite) Color.Red else Color.White
                            )
                        }
                        IconButton(onClick = onCloseClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Player",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Action Play/Pause trigger
                if (!isPreparing && !hasError) {
                    IconButton(
                        onClick = {
                            videoViewInstance?.let { view ->
                                if (view.isPlaying) {
                                    view.pause()
                                    isPlaying = false
                                } else {
                                    view.start()
                                    isPlaying = true
                                }
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Bottom Progress Tracker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isPlaying) Color.Green else Color.Red, shape = MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlaying) "LIVE" else "PAUSED",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }

                    // Indicator of active state
                    Text(
                        text = "IPTV Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Preparing State Spinner overlay
        if (isPreparing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buffering Live Stream...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Failure Fallback Overlay
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Playback Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Playback Error Occurred",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This channel stream link may be temporarily offline, requires authentication, or is in an unsupported format.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
