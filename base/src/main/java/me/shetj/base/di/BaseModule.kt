package me.shetj.base.di

import me.shetj.base.saver.SaverDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dbModule = module() {

    single { SaverDatabase.getInstance(androidApplication()) }

    //try to override existing definition. 覆盖其他实例
    //(override = true) -> FIX:Please use override option or check for definition '[Single:'me.shetj.base.saver.SaverDao']'
    single(override = true) { get<SaverDatabase>().saverDao() }

}