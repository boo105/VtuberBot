package Music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.*
import kotlin.concurrent.timerTask


class TrackScheduler(private val player: AudioPlayer) : AudioLoadResultHandler {
    var startPositions : List<Long>? = null
    var endPositions : List<Int>? = null
    var currentIndex = 1

    override fun trackLoaded(track: AudioTrack) {
        // LavaPlayer found an audio source for us to play
        player.playTrack(track)

        startPositions?.let {
            val startPosition = it[currentIndex] * 1000
            endPositions?.let {
                val endPosition = it[currentIndex] * 1000
                val duration = endPosition - startPosition

                player.playingTrack.position = startPosition
                currentIndex += 1

                Timer(false).schedule(timerTask{
                    // 나중에 스킵할 경우를 생각해 현재 Track이랑 같은지 확인? 하고 해야함
                    player.stopTrack()
                    startPositions = null
                    endPositions = null
                    println("종료 성공!")
                },duration)
            }
        }

        println("재생 성공!")
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

    private fun withTimeLink() {

    }

}