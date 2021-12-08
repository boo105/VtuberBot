package Music

import HotSongs
import LavaPlayerAudioProvider
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.voice.AudioProvider
import java.util.concurrent.atomic.AtomicBoolean

object MusicManager {
    // Creates AudioPlayer instances and translates URLs to AudioTrack instances
    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private lateinit var player : AudioPlayer
    private lateinit var provider :AudioProvider
    private lateinit var scheduler : TrackScheduler

    init {
        // This is an optimization strategy that Discord4J can utilize.
        // It is not important to understand
        playerManager.configuration.frameBufferFactory =
            AudioFrameBufferFactory { bufferDuration: Int, format: AudioDataFormat?, stopping: AtomicBoolean? ->
                NonAllocatingAudioFrameBuffer(
                    bufferDuration,
                    format,
                    stopping
                )
            }
        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager)
        // Create an AudioPlayer so Discord4J can receive audio data
        player = playerManager.createPlayer()
        // We will be creating LavaPlayerAudioProvider in the next step
        provider = LavaPlayerAudioProvider(player)
        scheduler  = TrackScheduler(player)
    }

    fun getAudioProvider() : AudioProvider {
        return provider
    }

    fun playSong(hotSongs : HotSongs) {
        scheduler.startPositions = hotSongs.startTime
        scheduler.endPositions = hotSongs.endTime
        playerManager.loadItem(hotSongs.videoLink[1], scheduler)
    }

    fun playSongWithYoutubeLink(link : String) {
        println(link)
        playerManager.loadItem(link, scheduler)
    }


    fun playSongs() {

    }

}