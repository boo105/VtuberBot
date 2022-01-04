import Api.APIKeyManager
import Api.YoutubeRequest
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
import org.apache.commons.io.Charsets
import reactor.core.publisher.Mono
import java.io.File
import java.time.Instant

/*
* 해야 할거
* 이전 메세지 삭제 코드 계속 중복 되니까 메세지 관리하는 객체를 하나 만들기!!!!!!
* 사용자 즐겨찾기 목록 ( 자기가 즐겨찾기 추가 하는데 사용자별로 존재)
* 커맨드(명령어) 목록 리팩토링
* 링크로 play 시 duration때문에 다음노래 실행안됨
* */

fun main(args : Array<String>) {
    val clientInfoString = File("./src/main/resources/ClientToken.json").readText(Charsets.UTF_8)
    val clientInfo = Gson().fromJson(clientInfoString, ClientToken::class.java)

    val token = clientInfo.token
    val client = DiscordClient.create(token)

    var preBotMessage : Message? = null

    // 나중에 JP, EN 등등 따로 분리 해보자
    // 그리고 이름 순대로 정렬 해야할 듯
    val holoiveChannelIdList = listOf(
        ChannelID("Hoshimachi Suisei","UC5CwaMl1eIgY8h02uZw7u8A"),
        ChannelID("Mori Calliope", "UCL_qhgtOy0dy1Agp8vkySQg"),
        ChannelID("Tokoyami Towa","UC1uv2Oq6kNxgATlCiez59hw"),
        ChannelID("Tsunomaki Watame","UCqm3BQLlJfvkTsX_hvm0UmA"),
        ChannelID("IRyS","UC8rcEBzJSleTkf_-agPM20g"),
        ChannelID("Shirogane Noel","UCdyqAaZDKHXg4Ahi7VENThQ"),
        ChannelID("Murasaki Shion","UCXTpFs_3PqI41qX2d9tL2Rw"),
        ChannelID("Minato Aqua","UC1opHUrw8rvnsadT-iGp7Cg"),
        ChannelID("Oozora Subaru","UCvzGlP9oQwU--Y0r9id_jnA"),
        ChannelID("Nekomata Okayu","UCvaTdHTWBGv3MKj3KVqJVCw"),
        ChannelID("Shirakami Fubuki","UCdn5BQ06XqgXoAxIhbqw5Rg"),
        ChannelID("Usada Pekora","UC1DCedRgGHBdm81E1llLhOQ"),
        ChannelID("Uruha Rushia","UCl_gCybOJRIgOXw6Qb4qJzQ"),
        ChannelID("Houshou Marine","UCCzUftO8KOVkV4wQG1vkUvg"),
        ChannelID("Momosuzu Nene","UCAWSyEs_Io8MtpY3m-zqILA"),
        ChannelID("La+ Darknesss","UCENwRMx5Yh42zWpzURebzTw"),
        ChannelID("Hakui Koyori","UC6eWCld0KwmyHFbAqK3V-Rw"),
        ChannelID("Sakamata Chloe","UCIBY1ollUsauvVi4hW4cumw"),
        ChannelID("Gawr Gura","UCoSrY_IQQVpmIRZ9Xf-y93g"),
        ChannelID("Watson Amelia","UCyl1z3jo3XHR1riLFKG5UAg"),
        ChannelID("Takanashi Kiara","UCHsx4Hqa-1ORjQTh9TYDhww"),
        ChannelID("Ouro Kronii","UCmbs8T6MWqUHP1tIQvSgKrg")
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
                BotMessageController.deletePreMessage()

                val helpEmbed = EmbedManager.getHelpEmbed()
                BotMessageController.sendMessage(message.channel.block(), helpEmbed)
            }

            if (message.content.equals("!생방송", ignoreCase = true)) {
                BotMessageController.deletePreMessage()

                val liveListEmbed = EmbedManager.getLiveListEmbed()
                BotMessageController.sendMessage(message.channel.block(), liveListEmbed)
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

                val music = runBlocking {
                    return@runBlocking YoutubeRequest.getVideoInfo(url)
                }
                music?.let {
                    val playMode = MusicManager.play(music)

                    if(playMode == 2) {
                        val addedMusicQueueEmbed = EmbedManager.getAddedMusicQueueEmbed(music.name)
                        preBotMessage = message.channel.block().createMessage(addedMusicQueueEmbed).block()
                    }
                    else if(playMode == 1) {
                        val playNowMusicEmbed = EmbedManager.getPlayNowMusicEmbed(music, message.author.get().mention)
                        preBotMessage = message.channel.block().createMessage(playNowMusicEmbed).block()
                    }
                }
            }

            if (message.content.contains("!info", ignoreCase = true)) {
                BotMessageController.deletePreMessage()

                val currentMusicInfoEmbed = EmbedManager.getCurrentMusicInfoEmbed()
                BotMessageController.sendMessage(message.channel.block(), currentMusicInfoEmbed)
            }

            if (message.content.contains("!list", ignoreCase = true)) {
                BotMessageController.deletePreMessage()

                val musicQueueListEmbed = EmbedManager.getMusicQueueListEmbed()
                BotMessageController.sendMessage(message.channel.block(), musicQueueListEmbed)
            }

            if (message.content.contains("!홀로라이브", ignoreCase = true)) {
                BotMessageController.deletePreMessage()

                val command = message.content.split(" ")
                if (command.size != 2) {
                    val hololiveListEmbed = EmbedManager.getHoloLiveListEmbed(holoiveChannelIdList)
                    BotMessageController.sendMessage(message.channel.block(), hololiveListEmbed)
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
                BotMessageController.deletePreMessage()

                val skippedMusic = MusicManager.skip()
                val playNowMusicEmbed = EmbedManager.getSkippedMusicEmbed(skippedMusic)
                BotMessageController.sendMessage(message.channel.block(), playNowMusicEmbed)
            }

            if (message.content.contains("!stop", ignoreCase = true)) {
                MusicManager.stop()
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