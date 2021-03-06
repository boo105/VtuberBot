import Music.TrackScheduler
import com.google.gson.Gson
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.VoiceChannelJoinSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono
import java.io.File
import java.time.Instant
import java.util.*
import Music.*


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

fun main(args : Array<String>) {
    val clientInfoString = File("./src/main/resources/ClientToken.json").readText(Charsets.UTF_8)
    val clientInfo = Gson().fromJson(clientInfoString, ClientToken::class.java)

    val token = clientInfo.token
    val client = DiscordClient.create(token)

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
                MusicManager.playSong(hotSongs)
                println(hotSongs.videoLink[1])
                println(hotSongs.startTime[1])
                println(hotSongs.endTime[1])
            }

            if (message.content.contains("!play", ignoreCase = true)) {
                val commandSplit = message.content.split("!play")
                val url = commandSplit[1].replace(" ","")
                botJoin(event)

                MusicManager.playSongWithYoutubeLink(url)
            }

            Mono.empty()
        }.then()

        printOnLogin.and(handlePingCommand)
    }
    login.block()
}