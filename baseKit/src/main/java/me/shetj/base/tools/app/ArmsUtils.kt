package me.shetj.base.tools.app

import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.Message
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.shetj.base.S
import me.shetj.base.ktx.setClicksAnimate
import me.shetj.base.ktx.setSwipeRefresh
import me.shetj.base.ktx.toMessage
import me.shetj.base.tools.file.EnvironmentStorage
import me.shetj.base.tools.qmui.QMUINotchHelper
import me.shetj.base.tools.qmui.QMUIStatusBarHelper
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest


/**
 * ================================================
 * 一些框架常用的工具
 */
@Keep
class ArmsUtils private constructor() {

    init {
        throw IllegalStateException("you can't instantiate me!")
    }

    companion object {
        var mToast: Toast? = null

        /**
         * 全面屏幕检查
         * @param activity
         */
        @JvmStatic
        fun checkIsNotchScreen(activity: Activity): Boolean {
            return  QMUINotchHelper.needFixLandscapeNotchAreaFitSystemWindow(activity.window.decorView)
        }


        /**
         * 为 View 添加点击态
         * @param view
         */
        @JvmStatic
        fun addScaleTouchEffect(vararg view: View) {
            for (v in view) {
                v.setClicksAnimate()
            }
        }

        /**
         * 获得资源
         */
        @JvmStatic
        fun getResources(context: Context): Resources {
            return context.resources
        }

        /**
         * 得到字符数组
         */
        @JvmStatic
        fun getStringArray(context: Context, id: Int): Array<String> {
            return getResources(context).getStringArray(id)
        }

        /**
         * 从 dimens 中获得尺寸
         * @param context
         * @param id
         * @return
         */
        @JvmStatic
        fun getDimens(context: Context, id: Int): Int {
            return getResources(context).getDimension(id).toInt()
        }

        /**
         * 从 dimens 中获得尺寸
         *
         * @param context
         * @param dimenName
         * @return
         */
        @JvmStatic
        fun getDimens(context: Context, dimenName: String): Float {
            return getResources(context).getDimension(getResources(context).getIdentifier(dimenName, "dimen", context.packageName))
        }

        /**
         * 从String 中获得字符
         *
         * @return
         */
        @JvmStatic
        fun getString(context: Context, stringID: Int): String {
            return getResources(context).getString(stringID)
        }

        /**
         * 从String 中获得字符
         *
         * @return
         */
        @JvmStatic
        fun getString(context: Context, strName: String): String {
            return getString(context, getResources(context).getIdentifier(strName, "string", context.packageName))
        }

        /**
         * 获取随机数字
         */
        @JvmStatic
        fun getRandomString(): String {
            var linkNo = ""
            // 用字符数组的方式随机
            val model = "0aAbBc1CdDeE2fFgGh3HiIjJ4kKlLm5MnNoO6pPqQr7RsStT8uUvVw9WxXyY0zZ"
            val m = model.toCharArray()
            var j = 0
            while (j < 9) {
                val c = m[(Math.random() * 62).toInt()]
                //随机数之间没有重复的
                if (linkNo.contains(c.toString())) {
                    j--
                    j++
                    continue
                }
                linkNo += c
                j++
            }
            return linkNo
        }

        /**
         * findview
         *
         * @param view
         * @param viewName
         * @param <T>
         * @return
        </T> */
        @JvmStatic
        fun <T : View> findViewByName(context: Context, view: View, viewName: String): T {
            val id = getResources(context).getIdentifier(viewName, "id", context.packageName)
            return view.findViewById(id)
        }

        /**
         * findview
         *
         * @param activity
         * @param viewName
         * @param <T>
         * @return
        </T> */
        @JvmStatic
        fun <T : View> findViewByName(context: Context, activity: Activity, viewName: String): T {
            val id = getResources(context).getIdentifier(viewName, "id", context.packageName)
            return activity.findViewById(id)
        }

        /**
         * 根据 layout 名字获得 id
         *
         * @param layoutName
         * @return
         */
        @JvmStatic
        fun findLayout(context: Context, layoutName: String): Int {
            return getResources(context).getIdentifier(layoutName, "layout", context.packageName)
        }

        /**
         * 填充view
         *
         * @param detailScreen
         * @return
         */
        @JvmStatic
        fun inflate(context: Context, detailScreen: Int): View {
            return View.inflate(context, detailScreen, null)
        }

        /**
         * 单例 toast
         * @param string
         */
        @JvmStatic
        @MainThread
        fun makeText(string: String) {
            if (mToast == null) {
                mToast = Toast.makeText(Utils.app.applicationContext, string, Toast.LENGTH_SHORT)
            }
            mToast!!.setText(string)
            mToast!!.show()
        }

        /**
         * 通过资源id获得drawable
         *
         * @param rID
         * @return
         */
        @JvmStatic
        fun getDrawableByResource(context: Context, rID: Int): Drawable? {
            return ContextCompat.getDrawable(context, rID)
        }

        /**
         * 跳转界面 3
         *
         * @param activity
         * @param homeActivityClass
         */
        @JvmStatic
        fun startActivity(activity: Activity, homeActivityClass: Class<*>) {
            val intent = Intent(activity.applicationContext, homeActivityClass)
            startActivity(activity, intent)
        }

        /**
         * 跳转界面 4
         *
         * @param
         */
        @JvmStatic
        fun startActivity(activity: Activity, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
            } else {
                activity.startActivity(intent)
            }
        }

        @JvmStatic
        fun getScreenWidth(): Int {
            return getScreenWidth(Utils.app.applicationContext)
        }

        /**
         * 获得屏幕的高度
         *
         * @return
         */
        @JvmStatic
        fun getScreenHeight(): Int {
            return getScreenHeight(Utils.app.applicationContext)
        }

        /**
         * 获得屏幕的宽度
         *
         * @return
         */
        @JvmStatic
        fun getScreenWidth(context: Context): Int {
            return getResources(context).displayMetrics.widthPixels
        }

        /**
         * 获得屏幕的高度
         *
         * @return
         */
        @JvmStatic
        fun getScreenHeight(context: Context): Int {
            return getResources(context).displayMetrics.heightPixels
        }


        /**
         * 获得颜色
         */
        @JvmStatic
        fun getColor(context: Context, rid: Int): Int {
            return ContextCompat.getColor(context, rid)
        }

        fun resolve(context: Context, @AttrRes attributeResId: Int): TypedValue? {
            val typedValue = TypedValue()
            return if (context.theme.resolveAttribute(attributeResId, typedValue, true)) {
                typedValue
            } else null
        }

        /**
         * 移除孩子
         *
         * @param view
         */
        @JvmStatic
        fun removeChild(view: View) {
            val parent = view.parent
            if (parent is ViewGroup) {
                parent.removeView(view)
            }
        }


        /**
         * MD5
         *
         * @param string
         * @return
         * @throws Exception
         */
        @JvmStatic
        fun encodeToMD5(string: String): String {
            var hash = ByteArray(0)
            try {
                hash = MessageDigest.getInstance("MD5").digest(
                        string.toByteArray(charset("UTF-8")))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val hex = StringBuilder(hash.size * 2)
            for (b in hash) {
                if (b.toInt() and 0xFF < 0x10) {
                    hex.append("0")
                }
                hex.append(Integer.toHexString((b.toInt() and 0xFF)))
            }
            return hex.toString()
        }

        /**
         * 设置透明状态栏与导航栏
         *
         * @param navi true不设置导航栏|false设置导航栏
         */
        @JvmStatic
        fun setStatusBar(activity: Activity, navi: Boolean) {
            //api>21,全透明状态栏和导航栏;api>19,半透明状态栏和导航栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
                if (navi) {
                    //状态栏不会被隐藏但activity布局会扩展到状态栏所在位置
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//导航栏不会被隐藏但activity布局会扩展到导航栏所在位置

                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_FULLSCREEN)
                    window.navigationBarColor = Color.TRANSPARENT
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }

            } else {
                if (navi) {
                    //半透明导航栏
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                }
                //半透明状态栏
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }


        /**
         * 全屏,并且沉侵式状态栏
         * @param activity
         * @param isBlack 是否是黑色的字体和icon
         */
        @JvmStatic
        @JvmOverloads
        fun statuInScreen2(activity: Activity, isBlack: Boolean = false) {
            activity.statuInScreen(isBlack)
        }

        @JvmStatic
        @JvmOverloads
        fun Activity.statuInScreen(isBlack: Boolean = false) {
            // 沉浸式状态栏
            QMUIStatusBarHelper.translucent(this)
            if (isBlack) {
                QMUIStatusBarHelper.setStatusBarLightMode(this)
            } else {
                QMUIStatusBarHelper.setStatusBarDarkMode(this)
            }
        }

        /**
         * 全屏
         * {@see [theme:EdgeStyle]} 填充刘海屏幕
         */
        @JvmStatic
        fun fullScreencall(activity: Activity) {
            //STATUS_BARS, NAVIGATION_BARS, CAPTION_BAR, IME, WINDOW_DECOR,
            //                SYSTEM_GESTURES, MANDATORY_SYSTEM_GESTURES, TAPPABLE_ELEMENT, DISPLAY_CUTOUT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.insetsController?.hide(WindowInsets.Type.systemBars())
            }else {
                val decorView = activity.window.decorView
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                if (Build.VERSION.SDK_INT >= 21) {
                    activity.window.statusBarColor = Color.TRANSPARENT
                    activity.window.navigationBarColor = Color.TRANSPARENT
                }
            }
        }

        @JvmStatic
        fun setSwipeRefresh(mSwipeRefreshLayout: SwipeRefreshLayout,
                            them2Color: Int, listener: SwipeRefreshLayout.OnRefreshListener) {
            mSwipeRefreshLayout.setSwipeRefresh(them2Color, listener)
        }


        @Throws(IOException::class)
        @JvmStatic
        fun getAssetsFile(fileName: String): InputStream {
            return Utils.app.applicationContext.assets.open(fileName)
        }

        private var density = -1f

        private fun getDensity(): Float {
            if (density <= 0f) {
                density = S.app.resources.displayMetrics.density
            }
            return density
        }

        @JvmStatic
        fun dp2px(dpValue: Float): Int {
            return (dpValue * getDensity() + 0.5f).toInt()
        }

        @JvmStatic
        fun px2dp(pxValue: Float): Int {
            return (pxValue / getDensity() + 0.5f).toInt()
        }

        @JvmStatic
        @NonNull
        fun getMessage(code: Int, obj: Any): Message {
            return obj.toMessage(code)
        }

        @JvmStatic
        fun getActivityHeight(context: Context?): Int {
            if (null == context) {
                return getScreenHeight()
            }
            val outRect1 = Rect()
            try {
                (context as Activity).window.decorView.getWindowVisibleDisplayFrame(outRect1)
            } catch (e: ClassCastException) {
                e.printStackTrace()
                return getScreenHeight(context)
            }
            return outRect1.height()
        }

        /**
         * 为app创建保存路径，
         * 如果不存在需要makdir
         */
        @JvmStatic
        fun getAppPath(): String {
            val sb = StringBuilder()
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                sb.append(EnvironmentStorage.sdCardPath)
            } else {
                sb.append(Environment.getDataDirectory().path)
            }
            sb.append(File.separator)
            sb.append(AppUtils.appPackageName)
            sb.append(File.separator)
            return sb.toString()
        }

        fun copyText(context: Context, text: String) {
            val cm: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            // 创建普通字符型ClipData
            val mClipData = ClipData.newPlainText("Label", text)
            // 将ClipData内容放到系统剪贴板里。
            cm?.setPrimaryClip(mClipData)
        }

        /**
         * 获取粘贴的文字
         * Android 10 开始需要延迟去获取
         */
        fun paste(context: Context): String? {
            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (manager != null) {
                if (manager.hasPrimaryClip() && manager.primaryClip!!.itemCount > 0) {
                    val addedText = manager.primaryClip!!.getItemAt(0).text
                    return addedText.toString()
                }
            }
            return null
        }
    }


}