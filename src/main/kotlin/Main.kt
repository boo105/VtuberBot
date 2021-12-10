import Music.*
import com.google.gson.Gson
import discord4j.common.store.action.gateway.MessageDeleteBulkAction
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageBulkDeleteEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.MessageDeleteEvent
import discord4j.core.spec.*
import discord4j.discordjson.json.gateway.MessageDelete
import discord4j.discordjson.json.gateway.MessageDeleteBulk
import discord4j.rest.util.Color
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono
import java.io.File
import java.time.Instant
import java.util.*


data class ClientToken(val id : String, val secret : String, val token : String)

fun helpCommand(message : Message) : Mono<Message> {
    return message.channel.flatMap<Message> { channel: MessageChannel ->
        // 추후 embed 전용 클래스를 따로 만들어서 설정하기
        val embed: EmbedCreateSpec = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title("도움말")
            .url("")
            .author("I LOVE 민초", "", "https://i.imgur.com/F9BhEoz.png")
            .addField("Command Help", "추후 도움말 작성 예정", false)
            .addField("\u200B", "\u200B", false)
            .timestamp(Instant.now())
            .build()

        channel.createMessage(embed)
    }
}

fun botJoin(event : MessageCreateEvent) {
    val member: Member? = event.member.orElse(null)
    member?.let {
        val voiceState: VoiceState? = it.getVoiceState().block()
        voiceState?.let {
            val channel = it.channel.block()
            val spec : VoiceChannelJoinSpec = VoiceChannelJoinSpec.create().withProvider(MusicManager.getAudioProvider())
            channel.join(spec).block()
        }
    }
}

fun botLeave(event : MessageCreateEvent) {
    val member: Member? = event.member.orElse(null)
    member?.let {
        val voiceState: VoiceState? = it.getVoiceState().block()
        voiceState?.let {
            val channel = it.channel.block()
            val spec : VoiceChannelJoinSpec = VoiceChannelJoinSpec.create().withProvider(MusicManager.getAudioProvider())
            channel.join(spec).block().disconnect().block()
        }
    }
}

fun main(args : Array<String>) {
    val clientInfoString = File("./src/main/resources/ClientToken.json").readText(Charsets.UTF_8)
    val clientInfo = Gson().fromJson(clientInfoString, ClientToken::class.java)

    val token = clientInfo.token
    val client = DiscordClient.create(token)

    var preBotMessage : Message? = null

    val login = client.withGateway { gateway: GatewayDiscordClient ->
        // ReadyEvent
        val printOnLogin = gateway.on(ReadyEvent::class.java) { event: ReadyEvent ->
            Mono.fromRunnable<Any?> {
                val self = event.self
                println("Logged in as ${self.username}#${self.discriminator}")
            }
        }.then()

        // MessageCreateEvent
        val handlePingCommand = gateway.on<MessageCreateEvent, Message?>(MessageCreateEvent::class.java) { event: MessageCreateEvent ->
            val message = event.message
            if (message.content.equals("!ping", ignoreCase = true)) {
                return@on message.channel.flatMap<Message> { channel: MessageChannel ->
                        channel.createMessage("pong!")
                }
            }

            if (message.content.equals("!도움말", ignoreCase = true)) {
                return@on helpCommand(message)
            }

            // 이부분 추상화 나중에 하기 뮤직봇 듀토리얼에 interface 써서 명령어 실행부분 추상화 하는거 있음 보셈
            if (message.content.equals("!생방송", ignoreCase = true)) {
                val liveInfo = runBlocking {
                    val liveInfo = HoloDexRequest.getLive()
                    return@runBlocking liveInfo
                }

                val embed : EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
                    .color(Color.CYAN)
                    .title("생방송 리스트")
                    .timestamp(Instant.now())

                for(streamer in liveInfo) {
                    embed.addField(streamer.name,"${streamer.url}",true)
                    embed.addField("Category","${streamer.category}\n${streamer.startTime}",true)
                    embed.addField("\u200b", "\u200b",true)
                }

                return@on message.channel.flatMap<Message> { channel: MessageChannel ->
                    channel.createMessage(embed.build())
                    //channel.createMessage(*embedList.toTypedArray())
                }
            }

            if (message.content.equals("!인기차트", ignoreCase = true)) {
                botJoin(event)

                val hotSongs = runBlocking {
                    return@runBlocking HoloDexRequest.getHotSongs()
                }

                MusicManager.playHotSongs(hotSongs)
            }

            if (message.content.contains("!play", ignoreCase = true)) {
                val commandSplit = message.content.split("!play")
                val url = commandSplit[1].replace(" ","")
                botJoin(event)
                MusicManager.playSongWithYoutubeLink(url)
            }

            if (message.content.contains("!info", ignoreCase = true)) {
                preBotMessage?.let {
                    it.delete().block()
                    preBotMessage = null
                }
                val musicInfo = MusicManager.getCurrentMusicInfo()

                val embed : EmbedCreateSpec = EmbedCreateSpec.builder()
                    .color(Color.CYAN)
                    .title("노래 정보")
                    .addField("노래 제목", "${musicInfo?.name}", false)
                    .addField("아티스트", "${musicInfo?.artist}", false)
                    .build()

                preBotMessage = message.channel.block().createMessage(embed).block()
//                return@on message.channel.flatMap<Message> { channel: MessageChannel ->
//                    channel.createMessage(embed)
//                }
            }

            if (message.content.contains("!list", ignoreCase = true)) {
                preBotMessage?.let {
                    it.delete().block()
                    preBotMessage = null
                }

                val playList = MusicManager.getPlayList()

                val embed : EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
                    .color(Color.CYAN)
                    .title("플레이 리스트")

                var nameText = ""
                var artistText = ""
                for(music in playList) {
                    nameText += music.name + "\n"
                    artistText += music.artist + "\n"
                }
                embed.addField("노래 제목", nameText, true)
                    .addField("아티스트", artistText, true)

                preBotMessage = message.channel.block().createMessage(embed.build()).block()
//                return@on message.channel.flatMap<Message> { channel: MessageChannel ->
//                        channel.createMessage(embed.build())
//                }
            }

            if (message.content.contains("!skip", ignoreCase = true)) {
                MusicManager.skip()
            }

            if (message.content.contains("!leave", ignoreCase = true)) {
                botLeave(event)
            }

            Mono.empty()
        }.then()

        printOnLogin.and(handlePingCommand)
    }

    login.block()
}