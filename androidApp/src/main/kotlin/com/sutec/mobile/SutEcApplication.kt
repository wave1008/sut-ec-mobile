package com.sutec.mobile

import android.app.Application
import com.sutec.mobile.di.initKoin
import com.sutec.mobile.util.isAndroidDebugBuild
import org.koin.android.ext.koin.androidContext

class SutEcApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        isAndroidDebugBuild = BuildConfig.DEBUG
        initKoin {
            androidContext(this@SutEcApplication)
        }
    }
}
