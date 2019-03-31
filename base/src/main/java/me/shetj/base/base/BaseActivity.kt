package me.shetj.base.base

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Message
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View


import com.trello.rxlifecycle3.components.support.RxAppCompatActivity

import org.simple.eventbus.EventBus

import me.shetj.base.R
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.base.tools.app.HideUtil
import me.shetj.base.tools.app.openActivity
import me.shetj.base.tools.json.EmptyUtils
import me.shetj.base.tools.json.GsonKit
import me.shetj.base.view.LoadingDialog
import timber.log.Timber


/**
 * 基础类  view 层
 * @author shetj
 */
@Keep
abstract class BaseActivity<T : BasePresenter<*>> : RxAppCompatActivity(), IView {

    protected var mPresenter: T? = null

    override val rxContext: RxAppCompatActivity
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //如果要使用eventbus请将此方法返回true
        if (useEventBus()) {
            //注册到事件主线
            EventBus.getDefault().register(this)
        }
        HideUtil.init(this)
        startAnimation()
    }

    /**
     * 连接view
     */
    protected abstract fun initView()

    /**
     * 连接数据
     */
    protected abstract fun initData()

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    /**
     * 针对6.0动态请求权限问题
     * 判断是否允许此权限
     *
     * @param permissions  权限
     * @return hasPermission
     */
    protected fun hasPermission(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    /**
     * 是否使用eventBus,默认为使用(true)，
     *
     * @return useEventBus
     */
    protected fun useEventBus(): Boolean {
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        if (useEventBus()) {
            //如果要使用eventbus请将此方法返回true
            EventBus.getDefault().unregister(this)
        }
        if (null != mPresenter) {
            mPresenter!!.onDestroy()
        }
    }

    override fun showLoading(msg: String) {
        LoadingDialog.showLoading(this, true)
    }

    override fun hideLoading() {
        LoadingDialog.hideLoading()
    }

    /**
     * 界面开始动画 (此处输入方法执行任务.)
     */
    protected fun startAnimation() {
        overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out)
    }

    /**
     * 界面回退动画 (此处输入方法执行任务.)
     */
    protected fun endAnimation() {// 开始动画
        overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out)
    }

    override fun finish() {// 设置回退动画
        super.finish()
    }

    /**
     * 返回
     */
    fun back() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    override fun showMessage(message: String) {
        ArmsUtils.makeText(message)
    }

    override fun onBackPressed() {
        HideUtil.hideSoftKeyboard(rxContext)
        super.onBackPressed()
        endAnimation()
        back()
    }

    override fun updateView(message: Message) {
        if (EmptyUtils.isNotEmpty(message)) {
            Timber.i(GsonKit.objectToJson(message))
        }
    }
}