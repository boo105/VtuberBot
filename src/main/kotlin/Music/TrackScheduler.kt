package Music

import Data.MusicInfo
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
    private var startPositions : Long? = null
    private var duration : Long? = null
    private var timer = Timer(true)

    override fun trackLoaded(track: AudioTrack) {
        player.playTrack(track)
        val startTime = LocalTime.now()

        startPositions?.let {
            val startPosition = it * 1000
            player.playingTrack.position = startPosition

            duration?.let {
                duration = (it * 1000) + 2000   // 2000ms 는 부자연스럽게 끝내는거 방지용

                timer = Timer(true)
                timer.schedule(timerTask{
                    player.stopTrack()
                    startPositions = null
                    duration = null
                    val formatter = DateTimeFormatter.ofPattern("mm분 ss초")
                    val endTime = LocalTime.now()
                    val realTime = endTime.minusHours(startTime.hour.toLong()).minusMinutes(startTime.minute.toLong()).minusSeconds(startTime.second.toLong())
                    println("실제 소요시간 : ${realTime.format(formatter)}")
                    println("종료 성공!")
                    MusicManager.playNext()
                },duration!!)
            }
        }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        // LavaPlayer found multiple AudioTracks from some playlist
    }

    override fun noMatches() {
        // LavaPlayer did not find any audio to extract
        println("오디오 소스를 찾지 못함.")
    }

    override fun loadFailed(exception: FriendlyException) {
        // LavaPlayer could not parse an audio source for some reason
        println("오디오 소스 load 실패.")
    }

    fun setMusic(music : MusicInfo) {
        startPositions = music.getStartTime()
        duration = music.getMusicDruation()
    }

    fun clear() {
        startPositions = null
        timerClear()
    }

    private fun timerClear() {
        timer.cancel()
        timer.purge()
    }
}