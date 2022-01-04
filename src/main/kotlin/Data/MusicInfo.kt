package Data

import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MusicInfo(
    val name : String,
    val artist : String,
    val videoLink : String,

    private val startTime : Long? = null,
    private val endTime : Int? = null,
    private var duration : Long? = null
) {
    init {
        endTime?.let {
            setMusicDuration()
        }
    }

    private fun setMusicDuration() {
        startTime?.let {
            endTime?.let {
                duration = endTime - startTime
            }
        }
    }

    fun getStartTime() : Long? {
        return startTime
    }

    fun getEndTime() : Int? {
        return endTime
    }

    fun getMusicDruation() : Long? {
        return duration
    }

    override fun toString() : String {
        return "${name}, ${videoLink}, ${duration?.toDuration(DurationUnit.SECONDS)}"
    }
}