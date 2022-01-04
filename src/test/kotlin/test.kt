import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Handler
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main() {

    val test = 1022
    val test2 = "837s"

    val regex = "[0-9]*m[0-9]*s|[0-9]*s".toRegex()
    println(test2.matches(regex))
    println(regex.find(test2)?.value)

//    val timeStampSplit = test2.split("m")
//    println(timeStampSplit)
//    val MINUTE = timeStampSplit[0]
//    val SECONDS = timeStampSplit[1].split("s")[0]
//    val startTime = (MINUTE.toLong() * 60) + SECONDS.toLong()
    //val date: LocalDate = LocalDate.parse(test2, formatter)
    //println(test.toDuration(DurationUnit.SECONDS))

//    val testString = "23m42s"
//    val testString2 = "623s"
//
//    val regex = "[0-9]*m[0-9]*s".toRegex()
//
//    if (testString.matches(regex)) {
//        println("매칭 성공")
//    }
//    else {
//        println("매칭 실패")
//    }

//    val timeStampSplit = testString.split("m")
//    val MINUTE = timeStampSplit[0]
//    val SECONDS = timeStampSplit[1].split("s")[0]


//    val timer = Timer(false)
//
//    timer.schedule(timerTask{
//        println("타이머 내부 실행")
//    },1000)
//
//    timer.cancel()
//    timer.purge()
//    println("ㅎㅇ")

//    Timer(false).schedule(timerTask{
//        println("타이머 내부 실행")
//    },3000)
}