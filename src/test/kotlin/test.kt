import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Handler
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

fun main() {


    val timer = Timer(false)

    timer.schedule(timerTask{
        println("타이머 내부 실행")
    },1000)

    timer.cancel()
    timer.purge()
    println("ㅎㅇ")

//    Timer(false).schedule(timerTask{
//        println("타이머 내부 실행")
//    },3000)
}