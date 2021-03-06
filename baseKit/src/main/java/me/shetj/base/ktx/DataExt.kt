package me.shetj.base.ktx

import android.os.Bundle
import android.os.Message
import androidx.annotation.IntRange
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.base.tools.json.EmptyUtils
import timber.log.Timber
import java.util.*
import kotlin.random.Random

//region 转化成message
@JvmOverloads
fun Any?.toMessage(code: Int = 1): Message {
    return Message.obtain().apply {
        what = code
        obj = this@toMessage
    }
}

/**
 * 获取参数
 */
inline fun <reified T> Bundle?.getDataOrNull(key: String): T? {
    return this?.getSerializable(key) as? T
}

//endregion 转化成message


inline fun <T,R> T.runCatch(crossinline run: T.() -> R):R? {
   return try {
        run()
    } catch (e: Exception) {
        e.printStackTrace()
       null
    }
}

/**
 * 执行代码，并获取执行需要的时间
 */
inline fun runTimeMillis(crossinline run: () -> Unit): Long {
    val time = System.currentTimeMillis()
    run.invoke()
    return System.currentTimeMillis() - time
}

/**
 * 把1~9 之间加0
 */
fun Int.unitFormat(): String {
    return if (this in 0..9) "0$this" else "" + this
}

/**
 * 16进制 255 以内
 */
fun intToHStr(@IntRange(from = 0,to = 255) x: Int): String {
    return if (x > 15) {
        Integer.toHexString(x)
    } else {
        "0" + Integer.toHexString(x)
    }
}

val Float.dp2px
    get()= ArmsUtils.dp2px(this)

val Float.px2dp
    get() = ArmsUtils.px2dp(this)

fun Any?.isEmpty() = EmptyUtils.isEmpty(this)

fun Throwable?.log() {
    Timber.i(this)
}

//region 获取随机数
fun getRandomString(num: Int): String {
    var linkNo = ""
    // 用字符数组的方式随机
    val model = "0aAbBc1CdDeE2fFgGh3HiIjJ4kKlLm5MnNoO6pPqQr7RsStT8uUvVw9WxXyY0zZ"
    val m = model.toCharArray()
    var j = 0
    while (j < num) {
        val c = m[Random.nextInt(62)]
        //随机数之间没有重复的
        if (linkNo.contains(c.toString())) {
            continue
        }
        linkNo += c
        j++
    }
    return linkNo
}
//endregion

fun <E> ArrayList<E>.addIfNotNull(element: E?) {
    element?.let { this.add(it) }
}

/**
 * 获取一个带名字的 Thread
 */
fun getTagThread(tag: String, run: Runnable): Thread {
    val t = Thread(run)
    t.name = tag
    return t
}