import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import discord4j.voice.AudioProvider
import java.nio.ByteBuffer

class LavaPlayerAudioProvider(player: AudioPlayer) : AudioProvider(
    ByteBuffer.allocate(
        StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()
    )
) {
    private val player: AudioPlayer
    private val frame: MutableAudioFrame = MutableAudioFrame()
    override fun provide(): Boolean {
        // AudioPlayer writes audio data to its AudioFrame
        val didProvide: Boolean = player.provide(frame)
        // If audio was provided, flip from write-mode to read-mode
        if (didProvide) {
            buffer.flip()
        }
        return didProvide
    }

    init {
        // Allocate a ByteBuffer for Discord4J's AudioProvider to hold audio data
        // for Discord
        // Set LavaPlayer's MutableAudioFrame to use the same buffer as the one we
        // just allocated
        frame.setBuffer(buffer)
        this.player = player
    }
}