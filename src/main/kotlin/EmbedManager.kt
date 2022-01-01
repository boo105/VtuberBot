import Data.ChannelID
import Data.MusicInfo
import Music.MusicManager
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.runBlocking
import java.time.Instant

// Embed 생성 관리자
object EmbedManager {
    fun getHelpEmbed() : EmbedCreateSpec {
        // 추후 embed 전용 클래스를 따로 만들어서 설정하기
        val helpEmbed: EmbedCreateSpec = EmbedCreateSpec.builder()
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

        return helpEmbed
    }

    fun getLiveListEmbed() : EmbedCreateSpec {
        val liveInfo = runBlocking {
            val liveInfo = HoloDexRequest.getLive()
            return@runBlocking liveInfo
        }

        val liveListEmbed : EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title("생방송 리스트")
            .timestamp(Instant.now())

        for(streamer in liveInfo) {
            liveListEmbed.addField(streamer.name,"${streamer.url}",true)
            liveListEmbed.addField("Category","${streamer.category}\n${streamer.startTime}",true)
            liveListEmbed.addField("\u200b", "\u200b",true)
        }

        return liveListEmbed.build()
    }

    fun getCurrentMusicInfoEmbed() : EmbedCreateSpec {
        val musicInfo = MusicManager.getCurrentMusicInfo()

        val currentMusicInfoEmbed : EmbedCreateSpec = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title("노래 정보")
            .addField("노래 제목", "${musicInfo?.name}", false)
            .addField("아티스트", "${musicInfo?.artist}", false)
            .build()

        return currentMusicInfoEmbed
    }

    fun getMusicQueueListEmbed() : EmbedCreateSpec {
        val playList = MusicManager.getPlayList()

        val musicQueueListEmbed : EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title("플레이 리스트")

        var nameText = ""
        var artistText = ""
        for(music in playList) {
            nameText += music.name + "\n"
            artistText += music.artist + "\n"
        }
        musicQueueListEmbed.addField("노래 제목", nameText, true)
            .addField("아티스트", artistText, true)

        return musicQueueListEmbed.build()
    }

    fun getHoloLiveListEmbed(holoiveChannelIdList : List<ChannelID>) : EmbedCreateSpec {
        val hololiveListEmbed: EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title("홀로라이브 목록")

        var nameText = ""
        var indexText = ""
        for (i in 1..holoiveChannelIdList.size)
            indexText += i.toString() + ".\n"

        for (channel in holoiveChannelIdList)
            nameText += channel.name + "\n"

        hololiveListEmbed.addField("No.", indexText, true)
            .addField("Name", nameText, true)

        return hololiveListEmbed.build()
    }

    fun getPlayNowMusicEmbed(music : MusicInfo, userMention : String) : EmbedCreateSpec {
        val playNowMusicEmbed: EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title(":notes: ${music.name}")
            .url(music.videoLink)
            .description("[${userMention}]")
        return playNowMusicEmbed.build()
    }

    fun getAddedMusicQueueEmbed(musicName : String) : EmbedCreateSpec {
        val playListIndex = MusicManager.getPlayList().size

        val addedMusicQueueEmbed: EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .description("대기열에 추가 : ``${musicName}`` - ${playListIndex} 번째")

        return addedMusicQueueEmbed.build()
    }

    fun getSkippedMusicEmbed(music : MusicInfo) : EmbedCreateSpec {
        val skippedMusicEmbed: EmbedCreateSpec.Builder = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .description("[${music.name}](${music.videoLink}) 을 스킵하였습니다.")

        return skippedMusicEmbed.build()
    }
}