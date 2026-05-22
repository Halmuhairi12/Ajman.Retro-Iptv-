package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.PlaylistAddState

@Composable
fun PlaylistAddDialog(
    addState: PlaylistAddState,
    onDismissRequest: () -> Unit,
    onImportUrl: (name: String, url: String) -> Unit,
    onImportManual: (name: String, text: String) -> Unit,
    onResetState: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var playlistName by remember { mutableStateOf("") }
    var playlistUrl by remember { mutableStateOf("") }
    var playlistText by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var inputError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(addState) {
        if (addState is PlaylistAddState.Success) {
            onDismissRequest()
            onResetState()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (addState !is PlaylistAddState.Loading) {
                onDismissRequest()
                onResetState()
            }
        },
        title = {
            Text(
                text = "Import IPTV Playlist",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (addState is PlaylistAddState.Loading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Downloading & Cataloging Channels...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    // Show error state banner if any
                    if (addState is PlaylistAddState.Error) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = addState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("M3U URL") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Paste M3U Text") }
                        )
                    }

                    // Common Playlist Name Input
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = {
                            playlistName = it
                            nameError = null
                        },
                        label = { Text("Playlist Name") },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTab == 0) {
                        // Web link URL Input
                        OutlinedTextField(
                            value = playlistUrl,
                            onValueChange = {
                                playlistUrl = it
                                inputError = null
                            },
                            label = { Text("M3U Playlist URL") },
                            placeholder = { Text("https://example.com/playlist.m3u") },
                            isError = inputError != null,
                            supportingText = inputError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        // Raw M3U Paste box
                        OutlinedTextField(
                            value = playlistText,
                            onValueChange = {
                                playlistText = it
                                inputError = null
                            },
                            label = { Text("Raw M3U Content") },
                            placeholder = { Text("#EXTM3U\n#EXTINF:-1,Channel Name\nhttp://link...") },
                            isError = inputError != null,
                            supportingText = inputError?.let { { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            maxLines = 10
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (addState !is PlaylistAddState.Loading) {
                Button(
                    onClick = {
                        // Validation
                        var isValid = true
                        if (playlistName.trim().isEmpty()) {
                            nameError = "Requires a playlist name."
                            isValid = false
                        }

                        if (selectedTab == 0) {
                            val urlTrimmed = playlistUrl.trim()
                            if (urlTrimmed.isEmpty()) {
                                inputError = "Requires a URL path."
                                isValid = false
                            } else if (!urlTrimmed.startsWith("http://") && !urlTrimmed.startsWith("https://")) {
                                inputError = "Must start with http:// or https://"
                                isValid = false
                            }
                            if (isValid) {
                                onImportUrl(playlistName.trim(), urlTrimmed)
                            }
                        } else {
                            val textTrimmed = playlistText.trim()
                            if (textTrimmed.isEmpty()) {
                                inputError = "Must paste M3U text."
                                isValid = false
                            } else if (!textTrimmed.contains("#EXTM3U") && !textTrimmed.contains("#EXTINF")) {
                                inputError = "Doesn't appear to be a valid M3U format."
                                isValid = false
                            }
                            if (isValid) {
                                onImportManual(playlistName.trim(), textTrimmed)
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            }
        },
        dismissButton = {
            if (addState !is PlaylistAddState.Loading) {
                TextButton(
                    onClick = {
                        onDismissRequest()
                        onResetState()
                    }
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}
