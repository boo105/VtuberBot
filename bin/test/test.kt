import java.util.*
import java.util.logging.Handler
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

fun main() {


    Timer(false).schedule(timerTask{
        println("ㅎㅇ")
    },1000)
}