package Music

import Data.MusicInfo

object LinkManager {
    fun getVideoLink(video_id : String, startTime : Int) : String {
        val YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch"
        val startTimeForLink : String = getStartTimeForLink(startTime)
        val videoLink : String = YOUTUBE_VIDEO_BASE_URL + "?v=${video_id}#t=${startTimeForLink}"

        return videoLink
    }

    // 이거 startTime 분 초 형식으로 안줘도 되고 초 형식으로만 던져도 적용되는거 확인했으니까 그냥 s로 넣는게 속도 빠를듯
    private fun getStartTimeForLink(startTime : Int) : String {
        val MINUTE = 60
        val startTimeMinute = startTime / MINUTE
        val startTimeSeconds = startTime - (startTimeMinute * MINUTE)
        val startTimeForLink : String = startTimeMinute.toString() + "m" + startTimeSeconds.toString() + "s"

        return startTimeForLink
    }

    private fun isExistsTimeStamp(length : Int) : Boolean {
        return length == 2
    }

    private fun isTimeStampWithMinuteAndSeconds(timeStamp : String) : Boolean {
        val regex = "[0-9]*m[0-9]*s".toRegex()
        return timeStamp.matches(regex)
    }

    // 1. https://www.youtube.com/watch?v=8o-evQ01qFU&t=643s
    // 2. https://www.youtube.com/watch?v=8o-evQ01qFU&t=6m43s
    // 3. https://www.youtube.com/watch?v=WtU2Yqlwtgs
    // 링크로 타임스탬프 하면  분 초 로 들어옴..... -> 시작 시간을 정할수 있어도 끝나는 시간을 못정함......................................
    fun getMusicForYoutubeLink(link : String) : MusicInfo {
        val linkSplit = link.split("t=")

        if (isExistsTimeStamp(linkSplit.size)) {
            val timeStamp = linkSplit[1]

            if (isTimeStampWithMinuteAndSeconds(timeStamp)) {
                val timeStampSplit = timeStamp.split("m")
                val MINUTE = timeStampSplit[0]
                val SECONDS = timeStampSplit[1].split("s")[0]
                val startTime = (MINUTE.toLong() * 60) + SECONDS.toLong()

                // endTime 없어도 끝까지 끝내겠끔 TrackScheduler 에서  변경 하기!!!!!
                return MusicInfo("미구현", "미구현", link, startTime, null, null)
            }
            else {
                val SECONDS = timeStamp.split("s")[0]
                return MusicInfo("미구현", "미구현", link, SECONDS.toLong(), null, null)
            }
        }
        else {
            return MusicInfo("미구현", "미구현", link, null, null, null)
        }
    }
}