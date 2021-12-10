package Data

// live response
data class ResultGetLiveItem(
    val available_at: String,
    val channel: Channel,
    val duration: Int,
    val id: String,
    val live_viewers: Int,
    val published_at: String,
    val start_actual: String,
    val start_scheduled: String,
    val status: String,
    val title: String,
    val topic_id: String?,
    val type: String
)
data class Channel(
    val english_name: String,
    val id: String,
    val name: String,
    val photo: String,
    val type: String
)

// hot songs response
data class ResultGetHotSongsItem(
    val available_at: String,
    val channel: ChannelForSong,
    val channel_id: String,
    val end: Int,
    val id: Int,
    val name: String,
    val original_artist: String,
    val score: Double,
    val start: Int,
    val video_id: String
)
data class ChannelForSong(
    val english_name: String,
    val name: String,
    val photo: String
)