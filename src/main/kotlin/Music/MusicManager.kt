package Music

import Data.MusicInfo
import LavaPlayerAudioProvider
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.voice.AudioProvider
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


object MusicManager {
    // Creates AudioPlayer instances and translates URLs to AudioTrack instances
    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private lateinit var player : AudioPlayer
    private lateinit var provider :AudioProvider
    private lateinit var scheduler : TrackScheduler

    private var currentMusic : MusicInfo? = null
    private val playList = LinkedList<MusicInfo>()

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
        player.volume = 60
        // We will be creating LavaPlayerAudioProvider in the next step
        provider = LavaPlayerAudioProvider(player)
        scheduler  = TrackScheduler(player)
    }

    fun getAudioProvider() : AudioProvider {
        return provider
    }

    fun playSongWithYoutubeLink(link : String) {
        println(link)
        play(MusicInfo("아직 미구현","미구현",link,null,null,null))
    }

    fun playSongs(songs : List<MusicInfo>) {
        play(songs[0])
        for(i in 1..(songs.size)) {
            playList.add(songs[i])
        }
    }

    fun play(music : MusicInfo) {
        player.playingTrack?.let {
            println("음악 큐 추가")
            playList.add(music)
        } ?:
            println("음악 실행")
            println(music.name)
            currentMusic = music
            scheduler.startPositions = music.startTime
            scheduler.endPositions = music.endTime
            playerManager.loadItem(music.videoLink, scheduler)
    }

    fun playNext() {
        playList.peek().let {
            play(playList.poll())
        }
    }

    fun skip() {
        player.stopTrack()
        scheduler.timerClear()
        playNext()
    }

    fun getCurrentMusicInfo() : MusicInfo? {
        return currentMusic
    }

    fun getPlayList() : List<MusicInfo> {
        return playList
    }

    fun join() {

    }

    fun quit() {
        currentMusic = null
        player.stopTrack()
        playList.clear()
        scheduler.timerClear()
    }
}