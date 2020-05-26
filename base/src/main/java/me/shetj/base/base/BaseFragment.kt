package me.shetj.base.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import com.trello.rxlifecycle3.components.support.RxFragment
import me.shetj.base.kt.toJson
import me.shetj.base.s
import me.shetj.base.tools.json.EmptyUtils
import org.simple.eventbus.EventBus
import timber.log.Timber

/**
 * fragment基类
 * @author shetj
 * 不需要懒加载，viewPager2 执行顺序就是
 * 开始必须可见才会初始化:[onCreateView]-> [Lifecycle.Event.ON_CREATE] -> [onViewCreated]-> [Lifecycle.Event.ON_START] -> [Lifecycle.Event.ON_RESUME]
 * 结束前现会:[Lifecycle.Event.ON_PAUSE] -> [Lifecycle.Event.ON_STOP] -> [Lifecycle.Event.ON_DESTROY]
 * 不可见:   [Lifecycle.Event.ON_RESUME]  ->[Lifecycle.Event.ON_PAUSE]
 * 可见:     [Lifecycle.Event.ON_PAUSE] -> [Lifecycle.Event.ON_RESUME]
 */
@Keep
abstract class BaseFragment<T : BasePresenter<*>> : RxFragment(), IView, LifecycleObserver {
    protected var mActivity: Context? = null
    protected var mPresenter: T? = null

    /**
     * 返回当前的activity
     * @return RxAppCompatActivity
     */
    override val rxContext: RxAppCompatActivity
        get() = (mActivity as RxAppCompatActivity?)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mActivity = activity
        if (useEventBus()) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (useEventBus()) {
            EventBus.getDefault().unregister(this)
        }
        this.mActivity = null
    }

    /**
     * 是否使用eventBus,默认为使用(true)，
     *
     * @return boolean
     */
    open fun useEventBus(): Boolean {
        return true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden)
    }

    override fun onAttach(context: Context) {
        this.mActivity = context
        super.onAttach(context)
    }

    /**
     * On visible.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onVisible() {
    }

    /**
     * On invisible.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onInvisible() {
    }

    /**
     * Init event and data.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected abstract fun initEventAndData()


    override fun onDestroyView() {
        mPresenter?.onDestroy()
        super.onDestroyView()
    }

    @SuppressLint("unchecked")
    override fun updateView(message: Message) {
        if (s.isDebug && EmptyUtils.isNotEmpty(message)) {
            Timber.i(message.toJson())
        }
    }

    companion object {
        private const val STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN"
    }
}
