package Music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.*
import kotlin.concurrent.timerTask


class TrackScheduler(private val player: AudioPlayer) : AudioLoadResultHandler {
    var startPositions : Long? = null
    var endPositions : Int? = null

    override fun trackLoaded(track: AudioTrack) {
        player.playTrack(track)

        startPositions?.let {
            val startPosition = it * 1000
            endPositions?.let {
                val endPosition = (it * 1000) + 3000   // 3000ms 는 부자연스럽게 끝나는거 방지용
                val duration = endPosition - startPosition

                player.playingTrack.position = startPosition

                // 이거 새로 쓰레드 생성하는거니까 데몬쓰레드로 바꾸든 쓰레드 해제하고 하든 하셈
                Timer(true).schedule(timerTask{
                    // 나중에 스킵할 경우를 생각해 현재 Track이랑 같은지 확인? 하고 해야함
                    player.stopTrack()
                    startPositions = null
                    endPositions = null
                    MusicManager.playNext()
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
}