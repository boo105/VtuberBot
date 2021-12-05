import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

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

data class HoloLiveOnAirInfo(
    val url : String,
    val startTime : String,
    val category : String,
    val name : String
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
}

// onResponse Callback을 위한 옵저버 패턴 사용함.
interface EventListener {
    fun onLiveReceived(liveInfo : MutableList<HoloLiveOnAirInfo>) : MutableList<HoloLiveOnAirInfo>
}

object InfoRecevier {
    private var liveInfo =  mutableListOf<HoloLiveOnAirInfo>()

    fun setLiveInfo(liveInfo :MutableList<HoloLiveOnAirInfo> ) {
        this.liveInfo = liveInfo
    }

    fun getLiveInfo() : MutableList<HoloLiveOnAirInfo> {
        return this.liveInfo
    }
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
}