package com.psimao.rtcplayground.modules

import com.psimao.rtcplayground.data.datasource.SettingsPreferencesDataSource
import com.psimao.rtcplayground.domain.preferences.GetServerUrlUseCase
import com.psimao.rtcplayground.domain.preferences.GetUserUseCase
import com.psimao.rtcplayground.domain.preferences.StoreServerUrlUseCase
import com.psimao.rtcplayground.domain.preferences.StoreUserUseCase
import org.koin.dsl.module.applicationContext

val preferencesModule = applicationContext {
    bean { SettingsPreferencesDataSource(get()) }
    bean { GetUserUseCase(get()) }
    bean { GetServerUrlUseCase(get()) }
    bean { StoreUserUseCase(get()) }
    bean { StoreServerUrlUseCase(get()) }
}