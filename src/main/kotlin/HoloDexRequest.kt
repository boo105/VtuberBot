import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// response 파라미터
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

// Response에서 쓸만한 정보들만 추출한 객체
// 홀로라이브 생방송 정보 객체
data class HoloLiveOnAirInfo(
    val url : String,
    val startTime : String,
    val category : String,
    val name : String
)

data class HotSongs(
    val name : List<String>,
    val videoLink : List<String>,
    val startTime : List<Long>,
    val endTime : List<Int>
)

interface API {
    @GET("live")
    fun getLive(
        @Header("X-APIKEY") apiKey : String,
        @Query("channel_id") channelId : String? = null,
        @Query("id") id : String? = null,
        @Query("include") include : Array<String>? = null,
        @Query("lang") lang : String? = null,
        @Query("limit") limit : Int? = null,
        @Query("max_upcoming_hours") max_upcoming_hours : Number? = null,
        @Query("mentioned_channel_id") mentioned_channel_id : String? = null,
        @Query("offset") offset : Int? = null,
        @Query("order") order : String? = null,
        @Query("org") org : String? = null,
        @Query("paginated") paginated : String? = null,
        @Query("sort") sort : String? = null,
        @Query("status") status : String? = null,
        @Query("topic") topic : String? = "",
        @Query("type") type : String? = null
    ): Call<List<ResultGetLiveItem>>

    @GET("songs/hot")
    fun getHotSongs(
        @Header("X-APIKEY") apiKey : String,
        @Query("org") org : String
        ) : Call<List<ResultGetHotSongsItem>>
}

object HoloDexRequest {
    private val API_KEY = "c70cd6cf-91d5-4923-bdcb-62064609c040"
    private val BASE_URL = "https://holodex.net/api/v2/"

    private val gson = GsonBuilder().setLenient().create()
    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    private val api = retrofit.create(API::class.java)

    suspend fun getLive() : MutableList<HoloLiveOnAirInfo> {
        val getLiveResponse = api.getLive(API_KEY,org = "Hololive",limit = 10).awaitResponse()
        val body = getLiveResponse.body()
        val hololive = mutableListOf<HoloLiveOnAirInfo>()

        val live_url = "https://holodex.net/watch/"
        for(item in body.orEmpty()) {
            hololive.add(HoloLiveOnAirInfo(live_url + item.id,item.available_at,item.topic_id?:"",item.channel.name))
        }
        return hololive
    }

    suspend fun getHotSongs() : HotSongs {
        val response = api.getHotSongs(API_KEY,org = "Hololive").awaitResponse()
        val data = response.body()

        val names = mutableListOf<String>()
        val videoLinks = mutableListOf<String>()
        val startTimes = mutableListOf<Long>()
        val endTimes = mutableListOf<Int>()

        for(song in data.orEmpty()) {
            val videoLink : String = getVideoLink(song.video_id, song.start)
            names.add(song.name)
            videoLinks.add(videoLink)
            startTimes.add(song.start.toLong())
            endTimes.add(song.end)
            //hotSongs.add(HotSongs(song.name, videoLink))
        }

        val hotSongs = HotSongs(names,videoLinks,startTimes,endTimes)

        return hotSongs
    }

    private fun getVideoLink(video_id : String,startTime : Int) : String {
        val YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch"
        val startTimeForLink : String = getStartTimeForLink(startTime)
        val videoLink : String = YOUTUBE_VIDEO_BASE_URL + "?v=${video_id}#t=${startTimeForLink}"

        return videoLink
    }

    private fun getStartTimeForLink(startTime : Int) : String {
        val MINUTE = 60
        val startTimeMinute = startTime / MINUTE
        val startTimeSeconds = startTime - (startTimeMinute * MINUTE)
        val startTimeForLink : String = startTimeMinute.toString() + "m" + startTimeSeconds.toString() + "s"

        return startTimeForLink
    }
}

suspend fun main() {
    HoloDexRequest.getHotSongs()
}