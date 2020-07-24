package me.shetj.base.ktx

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Message
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.base.tools.json.EmptyUtils
import timber.log.Timber
import java.util.*
import kotlin.random.Random

//region 转化成message
@JvmOverloads
inline fun Any?.toMessage(code: Int = 1, crossinline action: (Message.() -> Unit) = {}): Message {
    return Message.obtain().apply {
        what = code
        obj = this@toMessage
        action.invoke(this)
    }
}

/**
 * 获取参数
 */
inline fun <reified T> Bundle?.getDataOrNull(key: String): T? {
    return this?.getSerializable(key) as? T
}

//endregion 转化成message




/**
 * 把1~9 之间加0
 */
fun Int.unitFormat(): String {
    return if (this in 0..9) "0$this" else "" + this
}

fun Float.dp2px() = ArmsUtils.dip2px(this)

fun Float.px2dp() = ArmsUtils.px2dip(this)

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

fun <E>  ArrayList<E>.addIfNotNull(element: E?) {
    element?.let { this.add(it) }
}

fun Context?.delFile(fileUri:Uri){
    this?.contentResolver?.delete(fileUri, null, null);
}