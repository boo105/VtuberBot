import Music.MusicManager
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.VoiceChannelJoinSpec
import discord4j.voice.VoiceConnection

object BotVoiceChannelController {
    private var voiceChannel : VoiceChannel? = null
    private var voiceConnection : VoiceConnection? = null
    private var spec : VoiceChannelJoinSpec? = null

    fun join(event : MessageCreateEvent) {
        if (voiceConnection == null) {
            voiceChannel = getVoiceChannel(event)
            val spec : VoiceChannelJoinSpec = VoiceChannelJoinSpec.create().withProvider(MusicManager.getAudioProvider())
            voiceConnection = voiceChannel?.join(spec)?.block()
        }
    }

    fun leave() {
        voiceConnection?.let {
            it.disconnect().block()
            MusicManager.quit()
            clearVoiceChannel()
        }
    }

    private fun clearVoiceChannel() {
        voiceChannel = null
        voiceConnection = null
        spec = null
    }

    private fun getVoiceChannel(event : MessageCreateEvent) : VoiceChannel? {
        val member: Member? = event.member.orElse(null)
        val voiceChannel = member?.let {
            val voiceState: VoiceState? = it.getVoiceState().block()
            val voiceChannel = voiceState?.let {
                it.channel.block()
            }
            return@let voiceChannel
        }
        return voiceChannel
    }
}