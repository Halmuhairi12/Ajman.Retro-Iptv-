package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.IptvDatabase
import com.example.data.IptvPlaylist
import com.example.data.IptvChannel
import com.example.data.IptvRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface PlaylistAddState {
    object Idle : PlaylistAddState
    object Loading : PlaylistAddState
    data class Success(val playlistId: Long) : PlaylistAddState
    data class Error(val message: String) : PlaylistAddState
}

class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IptvRepository
    
    // Core database flows
    val playlists: StateFlow<List<IptvPlaylist>>
    val favorites: StateFlow<List<IptvChannel>>
    val recents: StateFlow<List<IptvChannel>>

    // Selected state
    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId = _selectedPlaylistId.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String>("All")
    val selectedGroup = _selectedGroup.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Playlist addition state
    private val _addState = MutableStateFlow<PlaylistAddState>(PlaylistAddState.Idle)
    val addState = _addState.asStateFlow()

    // Live Player state
    private val _activeChannel = MutableStateFlow<IptvChannel?>(null)
    val activeChannel = _activeChannel.asStateFlow()

    init {
        val database = IptvDatabase.getDatabase(application)
        repository = IptvRepository(database.iptvDao())

        playlists = repository.allPlaylists
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        favorites = repository.favoriteChannels
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        recents = repository.getRecentChannels(20)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Pre-populate sample channels if there are no playlists on first run
        viewModelScope.launch {
            playlists.first { true } // Suspend until first stream is fetched
            if (playlists.value.isEmpty()) {
                prepopulateSamplePlaylist()
            } else {
                // Select first playlist automatically
                _selectedPlaylistId.value = playlists.value.firstOrNull()?.id
            }
        }
    }

    // Dynamic Lists based on selected Playlist, Group and Search Query
    val groups: StateFlow<List<String>> = _selectedPlaylistId
        .flatMapLatest { playlistId ->
            if (playlistId != null) {
                repository.getGroupTitles(playlistId).map { listOf("All") + it }
            } else {
                flowOf(listOf("All"))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val displayedChannels: StateFlow<List<IptvChannel>> = combine(
        _selectedPlaylistId,
        _selectedGroup,
        _searchQuery
    ) { playlistId, group, query ->
        Triple(playlistId, group, query)
    }.flatMapLatest { (playlistId, group, query) ->
        when {
            query.isNotEmpty() -> {
                repository.searchChannels(query)
            }
            playlistId == null -> {
                repository.allChannels
            }
            group == "All" -> {
                repository.getChannelsByPlaylist(playlistId)
            }
            else -> {
                repository.getChannelsByGroup(playlistId, group)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
        _selectedGroup.value = "All"
    }

    fun selectGroup(group: String) {
        _selectedGroup.value = group
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playChannel(channel: IptvChannel) {
        _activeChannel.value = channel
        viewModelScope.launch {
            repository.markChannelPlayed(channel.id)
        }
    }

    fun stopPlayback() {
        _activeChannel.value = null
    }

    fun toggleFavorite(channel: IptvChannel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel.id, !channel.isFavorite)
            // Update active channel view if it's the one modified
            if (_activeChannel.value?.id == channel.id) {
                _activeChannel.value = _activeChannel.value?.copy(isFavorite = !channel.isFavorite)
            }
        }
    }

    fun addPlaylistFromWeb(name: String, url: String) {
        viewModelScope.launch {
            _addState.value = PlaylistAddState.Loading
            try {
                val newId = repository.downloadAndAddPlaylist(name, url)
                _addState.value = PlaylistAddState.Success(newId)
                _selectedPlaylistId.value = newId
            } catch (e: Exception) {
                _addState.value = PlaylistAddState.Error(e.localizedMessage ?: "Failed to import playlist")
            }
        }
    }

    fun addPlaylistManual(name: String, m3uText: String) {
        viewModelScope.launch {
            _addState.value = PlaylistAddState.Loading
            try {
                val newId = repository.addPlaylist(name, "", m3uText)
                _addState.value = PlaylistAddState.Success(newId)
                _selectedPlaylistId.value = newId
            } catch (e: Exception) {
                _addState.value = PlaylistAddState.Error(e.localizedMessage ?: "Failed to import playlist")
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylistId.value == playlistId) {
                val remaining = playlists.value.filter { it.id != playlistId }
                _selectedPlaylistId.value = remaining.firstOrNull()?.id
                _selectedGroup.value = "All"
            }
        }
    }

    fun resetAddState() {
        _addState.value = PlaylistAddState.Idle
    }

    private suspend fun prepopulateSamplePlaylist() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-logo="https://raw.githubusercontent.com/AlJazeera/logo/master/logo.png" group-title="News",Al Jazeera News English
            https://live-aljazeera.akamaized.net/hls/live/2004210/aje/index.m3u8
            #EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/e/e5/NASA_logo.svg" group-title="Science",NASA HDTV
            https://ntv1.nasatv.net/hls/nhd/index.m3u8
            #EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/d/dd/France_24_logo.svg" group-title="News",France 24 HD English
            https://static.france24.com/live/F24_EN_LO_HLS/live_tv.m3u8
            #EXTINF:-1 tvg-logo="https://brandslogo.net/wp-content/uploads/2016/11/deutsche-welle-logo.png" group-title="News",Deutsche Welle News
            https://dwamdstream102.akamaized.net/hls/live/2014162/dwamdstream102/index.m3u8
            #EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/e/e0/Red_Bull_TV_Symbol.svg" group-title="Sports",Red Bull TV Live
            https://rbmn-live.akamaized.net/hls/live/590961/sports/index.m3u8
        """.trimIndent()

        val sampleId = repository.addPlaylist("Standard Global Live Info (Demo)", "internal://demo", sampleM3u)
        _selectedPlaylistId.value = sampleId
    }
}
