package com.psimao.rtcplayground.modules

import com.psimao.rtcplayground.presentation.home.dial.DialPresenter
import com.psimao.rtcplayground.presentation.home.dial.HomeDialPresenter
import com.psimao.rtcplayground.presentation.home.settings.PreferencesSettingsPresenter
import com.psimao.rtcplayground.presentation.home.settings.SettingsPresenter
import org.koin.dsl.module.applicationContext

val homeModule = applicationContext {
    bean { PreferencesSettingsPresenter(get(), get(), get(), get()) as SettingsPresenter }
    bean { HomeDialPresenter(get()) as DialPresenter}
}