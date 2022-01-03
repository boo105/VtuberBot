import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.spec.EmbedCreateSpec

object BotMessageController {
    var preMessage : Message? = null

    fun deletePreMessage() {
        preMessage?.let {
            it.delete().block()
            preMessage = null
        }
    }

    fun sendMessage(channel : MessageChannel, embed : EmbedCreateSpec) {
        preMessage = channel.createMessage(embed).block()
    }

    fun sendMessage(channel : MessageChannel, message : String) {
        preMessage = channel.createMessage(message).block()
    }
}