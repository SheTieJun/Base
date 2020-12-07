package me.shetj.base.tip

import android.content.Context
import android.widget.Toast
import me.shetj.base.weight.AbLoadingDialog

/**
 * Project Name:LiZhiWeiKe
 * Package Name:com.util.toast
 * Created by tom on 2018/1/22 11:43 .
 *
 *
 * Copyright (c) 2016—2017 https://www.lizhiweike.com all rights reserved.
 *
 * 存在以下问题
 * https://bugly.qq.com/v2/crash-reporting/crashes/69bcd1f57e/285881?pid=1
 * https://www.cnblogs.com/newobject/p/11042500.html
 *
 * 使用[AbLoadingDialog] 替代
 */
object ToastUtil {
    /**
     * 标准类型的taost
     *
     * 默认不显示icon，显示时间为Toast.LENGTH_SHORT
     */
    @JvmOverloads
    @JvmStatic
    fun normal(context: Context, message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        TipDialog.showTip(context, message, when (duration) {
            Toast.LENGTH_SHORT -> AbLoadingDialog.LOADING_SHORT
            else -> AbLoadingDialog.LOADING_LONG
        })
    }
    /**
     * 信息类型的taost
     *
     * 默认显示icon，显示时间为Toast.LENGTH_SHORT
     */
    @JvmOverloads
    @JvmStatic
    fun info(context: Context, message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        TipDialog.showTip(context, message, when (duration) {
            Toast.LENGTH_SHORT -> AbLoadingDialog.LOADING_SHORT
            else -> AbLoadingDialog.LOADING_LONG
        })
    }
    /**
     * 成功类型的taost
     *
     * 默认显示icon，显示时间为Toast.LENGTH_SHORT
     */
    @JvmOverloads
    @JvmStatic
    fun success(context: Context, message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        SimLoadingDialog.showTip(context, message,Tip.SUCCESS, when (duration) {
            Toast.LENGTH_SHORT -> AbLoadingDialog.LOADING_SHORT
            else -> AbLoadingDialog.LOADING_LONG
        })
    }
    /**
     * 错误类型的toast
     *
     * 默认显示icon，显示时间为Toast.LENGTH_LONG
     */
    @JvmOverloads
    @JvmStatic
    fun error(context: Context, message: CharSequence, duration: Int = Toast.LENGTH_LONG) {
        SimLoadingDialog.showTip(context, message,Tip.ERROR, when (duration) {
            Toast.LENGTH_SHORT -> AbLoadingDialog.LOADING_SHORT
            else -> AbLoadingDialog.LOADING_LONG
        })
    }
    /**
     * 警告类型的toast
     *
     * 默认显示icon，显示时间为Toast.LENGTH_LONG
     */
    @JvmOverloads
    @JvmStatic
    fun warn(context: Context, message: CharSequence, duration: Int = Toast.LENGTH_LONG) {
        SimLoadingDialog.showTip(context, message,Tip.WARNING, when (duration) {
            Toast.LENGTH_SHORT -> AbLoadingDialog.LOADING_SHORT
            else -> AbLoadingDialog.LOADING_LONG
        })
    }
}