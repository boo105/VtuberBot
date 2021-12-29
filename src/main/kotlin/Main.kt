import Data.ChannelID
import Data.ClientToken
import Music.*
import com.google.gson.Gson
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.*
import discord4j.rest.util.Color
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono
import java.io.File
import java.time.Instant

fun helpCommand(message : Message) : Mono<Message> {
    return message.channel.flatMap<Message> { channel: MessageChannel ->
        // 추후 embed 전용 클래스를 따로 만들어서 설정하기
        val embed: EmbedCreateSpec = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title("도움말")
            .addField("!생방송","현재 홀로라이브 생방송 목록",false)
            .addField("!인기차트","홀로라이브 인기차트 플레이리스트",false)
            .addField("!홀로라이브","홀로라이브 버튜버 목록",false)
            .addField("!홀로라이브 번호","홀로라이브 버튜버 플레이리스트 재생",false)
            .addField("!play 링크","유튜브 재생",false)
            .addField("!list","노래 대기열 정보",false)
            .addField("!info","현재 노래 정보",false)
            .addField("!skip","현재 노래 스킵",false)
            .addField("!leave","봇 퇴장",false)
            .build()

        channel.createMessage(embed)
    }
}

fun main(args : Array<String>) {
    val clientInfoString = File("./src/main/resources/ClientToken.json").readText(Charsets.UTF_8)
    val clientInfo = Gson().fromJson(clientInfoString, ClientToken::class.java)

    val token = clientInfo.token
    val client = DiscordClient.create(token)

    var preBotMessage : Message? = null

    val holoiveChannelIdList = listOf(
        ChannelID("Hoshimachi Suisei","UC5CwaMl1eIgY8h02uZw7u8A"),
        ChannelID("Mori Calliope", "UCL_qhgtOy0dy1Agp8vkySQg"),
        ChannelID("Tokoyami Towa","UC1uv2Oq6kNxgATlCiez59hw"),
        ChannelID("Tsunomaki Watame","UCqm3BQLlJfvkTsX_hvm0UmA"),
        ChannelID("IRyS","UC8rcEBzJSleTkf_-agPM20g"),
        ChannelID("Shirogane Noel","UCdyqAaZDKHXg4Ahi7VENThQ")
    )

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
                BotVoiceChannelController.join(event)

                val hotSongs = runBlocking {
                    return@runBlocking HoloDexRequest.getHotSongs()
                }

                MusicManager.playSongs(hotSongs)
            }

            if (message.content.contains("!play", ignoreCase = true)) {
                val commandSplit = message.content.split("!play")
                val url = commandSplit[1].replace(" ","")
                BotVoiceChannelController.join(event)

                val music = LinkManager.getMusicForYoutubeLink(url)
                MusicManager.play(music)
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
            }

            if (message.content.contains("!홀로라이브", ignoreCase = true)) {
                preBotMessage?.let {
                    it.delete().block()
                    preBotMessage = null
                }

                val command = message.content.split(" ")
                if (command.size != 2) {
                    val embed: EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
                        .color(Color.CYAN)
                        .title("홀로라이브 목록")

                    var nameText = ""
                    var indexText = ""
                    for (i in 1..holoiveChannelIdList.size)
                        indexText += i.toString() + ".\n"

                    for (channel in holoiveChannelIdList)
                        nameText += channel.name + "\n"

                    embed.addField("No.", indexText, true)
                        .addField("Name", nameText, true)
                    preBotMessage = message.channel.block().createMessage(embed.build()).block()
                }
                else if (command.size.equals(2)) {
                    val playList = runBlocking {
                        val channelID = holoiveChannelIdList[command[1].toInt() - 1].channelId
                        return@runBlocking HoloDexRequest.getPlayList(channelID)
                    }

                    BotVoiceChannelController.join(event)
                    MusicManager.playSongs(playList)
                }
            }

            if (message.content.contains("!skip", ignoreCase = true)) {
                MusicManager.skip()
            }

            if (message.content.contains("!leave", ignoreCase = true)) {
                BotVoiceChannelController.leave()
            }
            Mono.empty()
        }.then()

        printOnLogin.and(handlePingCommand)
    }
    login.block()
}