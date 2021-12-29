import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Handler
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

fun main() {
    val testString = "23m42s"
    val testString2 = "623s"
//
//    val regex = "[0-9]*m[0-9]*s".toRegex()
//
//    if (testString.matches(regex)) {
//        println("매칭 성공")
//    }
//    else {
//        println("매칭 실패")
//    }

    val timeStampSplit = testString.split("m")
    val MINUTE = timeStampSplit[0]
    val SECONDS = timeStampSplit[1].split("s")[0]


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