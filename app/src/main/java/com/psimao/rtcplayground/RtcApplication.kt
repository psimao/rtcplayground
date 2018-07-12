package com.psimao.rtcplayground

import android.app.Application
import com.psimao.rtcplayground.modules.*
import org.koin.android.ext.android.startKoin

class RtcApplication: Application() {

    private val modules = listOf(
            preferencesModule,
            signallingModule,
            rtcModule,
            homeModule,
            callModule
    )

    override fun onCreate() {
        super.onCreate()
        startKoin(this, modules)
    }
}