package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.IptvPlaylist
import com.example.ui.IptvViewModel
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainIptvScreen(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    // Collect States
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val displayedChannels by viewModel.displayedChannels.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recents by viewModel.recents.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val addState by viewModel.addState.collectAsStateWithLifecycle()
    val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()

    // Dialog & UI tabs state
    var showAddDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) } // 0: Channels, 1: Favorites, 2: Recents, 3: Playlists

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                // Top logo header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ajman.Retro iptv",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                        Text(
                            text = "Cinema-grade Live TV Directory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Import Button
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Playlist",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add List", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Dynamic Live Player Header Slot
                activeChannel?.let { channel ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        VideoPlayer(
                            channel = channel,
                            onToggleFavorite = { viewModel.toggleFavorite(channel) },
                            onCloseClick = { viewModel.stopPlayback() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Elegant Navigation Bar centered around immersive tab pills
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Channels") },
                    label = { Text("Channels") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Recents") },
                    label = { Text("Recents") }
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.PlaylistPlay, contentDescription = "Playlists") },
                    label = { Text("Playlists") }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Filters bar (Visible on Channels, Favorites, and Recents)
            if (currentTab != 3) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search channel name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "ClearIcon")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Tab View Rendering
            when (currentTab) {
                0 -> { // Channels View
                    if (playlists.isEmpty()) {
                        AppEmptyState(
                            title = "No IPTV Playlists",
                            description = "Import an M3U playlist file or web URL with live video resources to get started.",
                            buttonText = "Import Now",
                            onButtonClick = { showAddDialog = true }
                        )
                    } else {
                        // Playlist Selector + Group Pills
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Playlist selection dropdown or horizontal pills row
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(playlists) { playlist ->
                                    val isSelected = playlist.id == selectedPlaylistId
                                    InputChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectPlaylist(playlist.id) },
                                        label = { 
                                            Text(
                                                text = playlist.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.widthIn(max = 140.dp)
                                            ) 
                                        },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            // Channel category groups pills row
                            if (groups.size > 1) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(groups) { group ->
                                        val isSelected = group == selectedGroup
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectGroup(group) },
                                            label = { Text(group) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                                selectedLabelColor = MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Channel Feed List
                            if (displayedChannels.isEmpty()) {
                                AppEmptyState(
                                    title = if (searchQuery.isNotEmpty()) "No Results Found" else "Empty Group",
                                    description = if (searchQuery.isNotEmpty()) "No channel matches \"$searchQuery\". Try checking the spelling." else "This directory contains no active stream configurations."
                                )
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(displayedChannels, key = { it.id }) { channel ->
                                        ChannelItem(
                                            channel = channel,
                                            isActive = channel.id == activeChannel?.id,
                                            onClick = { viewModel.playChannel(channel) },
                                            onFavoriteToggle = { viewModel.toggleFavorite(channel) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> { // Favorites View
                    if (favorites.isEmpty()) {
                        AppEmptyState(
                            title = "No Bookmarks Yet",
                            description = "Save your most-watched channels with the star/heart symbol to group them here for instant access.",
                            icon = Icons.Default.FavoriteBorder
                        )
                    } else {
                        val filteredFavorites = if (searchQuery.isEmpty()) {
                            favorites
                        } else {
                            favorites.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }

                        if (filteredFavorites.isEmpty()) {
                            AppEmptyState(
                                title = "No Matches found",
                                description = "No channels in Favorites match your current search query."
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredFavorites, key = { it.id }) { channel ->
                                    ChannelItem(
                                        channel = channel,
                                        isActive = channel.id == activeChannel?.id,
                                        onClick = { viewModel.playChannel(channel) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(channel) }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> { // Recents View
                    if (recents.isEmpty()) {
                        AppEmptyState(
                            title = "No Watch History",
                            description = "Your recently tuned live TV feeds will appear here automatically for easy channel-surfing.",
                            icon = Icons.Default.History
                        )
                    } else {
                        val filteredRecents = if (searchQuery.isEmpty()) {
                            recents
                        } else {
                            recents.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }

                        if (filteredRecents.isEmpty()) {
                            AppEmptyState(
                                title = "No Matches found",
                                description = "No channels in Recents match your current search query."
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredRecents, key = { it.id }) { channel ->
                                    ChannelItem(
                                        channel = channel,
                                        isActive = channel.id == activeChannel?.id,
                                        onClick = { viewModel.playChannel(channel) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(channel) }
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> { // Playlists Manager View
                    if (playlists.isEmpty()) {
                        AppEmptyState(
                            title = "No IPTV Playlists",
                            description = "Imports M3U playlist file to get started.",
                            buttonText = "Import Now",
                            onButtonClick = { showAddDialog = true }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(playlists, key = { it.id }) { playlist ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = playlist.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (playlist.url.startsWith("internal://")) "Demo Resource Playlist" else playlist.url,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Active Database Catalog File",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.deletePlaylist(playlist.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Playlist",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Playlist Dialog component inclusion
    if (showAddDialog) {
        PlaylistAddDialog(
            addState = addState,
            onDismissRequest = { showAddDialog = false },
            onImportUrl = { name, url -> viewModel.addPlaylistFromWeb(name, url) },
            onImportManual = { name, txt -> viewModel.addPlaylistManual(name, txt) },
            onResetState = { viewModel.resetAddState() }
        )
    }
}
