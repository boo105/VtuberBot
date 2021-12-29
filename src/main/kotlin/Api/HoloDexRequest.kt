import Data.*
import Music.LinkManager
import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


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

    @POST("songs/latest")
    @Headers("accept: application/json",
        "content-type: application/json"
    )
    fun getPlayList(
        @Header("X-APIKEY") apiKey : String,
        @Body requestPlayList : RequestPlayList
    ) : Call<List<ResultGetPlayListItemItem>>
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

    suspend fun getHotSongs() : MutableList<MusicInfo> {
        val response = api.getHotSongs(API_KEY,org = "Hololive").awaitResponse()
        val data = response.body()

        val hotSongs = mutableListOf<MusicInfo>()

        for(song in data.orEmpty()) {
            val videoLink : String = LinkManager.getVideoLink(song.video_id, song.start)
            hotSongs.add(MusicInfo(song.name,song.channel.english_name ,videoLink, song.start.toLong(), song.end))
        }

        return hotSongs
    }

    suspend fun getPlayList(channelId: String) : MutableList<MusicInfo> {
        val response = api.getPlayList(API_KEY, RequestPlayList(channelId)).awaitResponse()
        val data = response.body()
        val playList = mutableListOf<MusicInfo>()

        for(song in data.orEmpty()) {
            val videoLink : String = LinkManager.getVideoLink(song.video_id, song.start)
            println("song name : ${song.name}, name : ${song.channel.english_name}, start : ${song.start.toLong()}, end : ${song.end}")
            playList.add(MusicInfo(song.name,song.channel.english_name ,videoLink, song.start.toLong(), song.end))
        }

        return playList
    }
}