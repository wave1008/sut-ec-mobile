package com.sutec.mobile

import android.app.Application
import com.sutec.mobile.di.initKoin
import org.koin.android.ext.koin.androidContext

class SutEcApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@SutEcApplication)
        }
    }
}
