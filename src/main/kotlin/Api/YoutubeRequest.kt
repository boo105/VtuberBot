package Api

import Data.*
import Music.LinkManager
import retrofit2.Call
import retrofit2.awaitResponse
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.regex.Pattern

interface API {
    @GET("videos")
    fun getVideoInfo(
        @Query("key") apiKey : String,
        @Query("part") part : String = "contentDetails,snippet",
        @Query("id") id : String
    ) : Call<ResultYoutubeVideoItem>

    @GET("channels")
    fun getChannelInfo(
        @Query("key") apiKey : String,
        @Query("part") part : String = "snippet",
        @Query("id") id : String
    ) : Call<ResultChannelItem>
}

object YoutubeRequest {
    private val API_KEY = APIKeyManager.getYoutubeAPIKEY()
    private val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    private val api : API

    init {
        val retrofit = RetroFitManager.getNewRetroFitInstance(BASE_URL)
        api = retrofit.create(API::class.java)
    }

    suspend fun getVideoInfo(url : String) : MusicInfo? {
        val id = getVideoID(url)
        val getVideoInfoResponse = api.getVideoInfo(API_KEY, id = id).awaitResponse()
        val body = getVideoInfoResponse.body()

        body?.let {
            val video = it.items[0]
            val title = video.snippet.title
            val channelId = video.snippet.channelId
            val duration = video.contentDetails.duration
            var durationForSeconds = transferToSeconds(duration)
            val artist = getChannelName(channelId) ?: ""
            val startTime = LinkManager.getStartTimeForYoutubeLink(url)
            // 만약 startTime 이 있으면 duration 수정
            startTime?.let {
                durationForSeconds = durationForSeconds - startTime
            }
            return MusicInfo(title, artist, url, startTime, null, durationForSeconds)
        }

        return null
    }

    private suspend fun getChannelName(channelId : String) : String? {
        val getChannelInfoResponse = api.getChannelInfo(API_KEY, id = channelId).awaitResponse()
        val body = getChannelInfoResponse.body()

        body?.let {
            val channel = it.items[0]
            val channelName = channel.snippet.title
            return channelName
        }

        return null
    }

    private fun getVideoID(url : String) : String {
        var vId = ""
        val pattern = Pattern.compile(
            "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*",
            Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(url ?: "")

        if (matcher.matches()) {
            vId = matcher.group(1) ?: ""
        }

        return vId
    }

    private fun transferToSeconds(duration : String) : Long {
        val hourReg = "[0-9]*H".toRegex()
        val minReg = "[0-9]*M".toRegex()
        val secReg = "[0-9]*S".toRegex()

        val min = minReg.find(duration)?.value?.replace("M","")?.toInt() ?: 0
        val sec = secReg.find(duration)?.value?.replace("S","")?.toInt() ?: 0
        val hour = hourReg.find(duration)?.value?.replace("H","")?.toInt() ?: 0

        val durationToSeconds = (hour * 60 * 60) + (min * 60) + sec
        return durationToSeconds.toLong()
    }
}