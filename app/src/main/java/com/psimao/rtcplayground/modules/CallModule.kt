package com.psimao.rtcplayground.modules

import com.psimao.rtcplayground.presentation.call.CallPresenter
import com.psimao.rtcplayground.presentation.call.RtcCallPresenter
import com.psimao.rtcplayground.presentation.home.dial.DialPresenter
import com.psimao.rtcplayground.presentation.home.dial.HomeDialPresenter
import com.psimao.rtcplayground.presentation.home.settings.PreferencesSettingsPresenter
import com.psimao.rtcplayground.presentation.home.settings.SettingsPresenter
import org.koin.dsl.module.applicationContext

val callModule = applicationContext {
    bean { RtcCallPresenter(get(), get(), get(), get(), get()) as CallPresenter }
}