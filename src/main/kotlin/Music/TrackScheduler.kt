package Music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timerTask


class TrackScheduler(private val player: AudioPlayer) : AudioLoadResultHandler {
    var startPositions : Long? = null
    var endPositions : Int? = null
    private var timer = Timer(true)

    override fun trackLoaded(track: AudioTrack) {
        player.playTrack(track)
        val startTime = LocalTime.now()

        startPositions?.let {
            val startPosition = it * 1000
            endPositions?.let {
                val endPosition = (it * 1000) + 2000   // 3000ms 는 부자연스럽게 끝나는거 방지용
                val duration = endPosition - startPosition
                player.playingTrack.position = startPosition

                val MIN = duration / 60 / 1000
                val SECONDS = (duration / 1000) - (MIN * 60)
                println("예상 소요시간 : ${MIN}분 ${SECONDS}초")

                // 이거 새로 쓰레드 생성하는거니까 데몬쓰레드로 바꾸든 쓰레드 해제하고 하든 하셈
                timer = Timer(true)
                timer.schedule(timerTask{
                    player.stopTrack()
                    startPositions = null
                    endPositions = null
                    val formatter = DateTimeFormatter.ofPattern("mm분 ss초")
                    val endTime = LocalTime.now()
                    val realTime = endTime.minusHours(startTime.hour.toLong()).minusMinutes(startTime.minute.toLong()).minusSeconds(startTime.second.toLong())
                    println("실제 소요시간 : ${realTime.format(formatter)}")
                    println("종료 성공!")
                    MusicManager.playNext()
                },duration)
            }
        }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        // LavaPlayer found multiple AudioTracks from some playlist
    }

    override fun noMatches() {
        // LavaPlayer did not find any audio to extract
    }

    override fun loadFailed(exception: FriendlyException) {
        // LavaPlayer could not parse an audio source for some reason
    }

    fun clear() {
        startPositions = null
        endPositions = null
        timerClear()
    }

    private fun timerClear() {
        timer.cancel()
        timer.purge()
    }
}