package Data

data class MusicInfo(
    val name : String,
    val artist : String,
    val videoLink : String,
    val startTime : Long? = null,
    val endTime : Int? = null,
    val duration : Int? = null
)