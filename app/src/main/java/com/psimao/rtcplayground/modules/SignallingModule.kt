package com.psimao.rtcplayground.modules

import com.psimao.rtcplayground.data.datasource.SignallingServerDataSource
import com.psimao.rtcplayground.domain.signalling.EmitEventOnSignallingServerUseCase
import com.psimao.rtcplayground.domain.rtc.ConnectToRtcCallUseCase
import com.psimao.rtcplayground.domain.signalling.ObserveOnlineUsersUseCase
import com.psimao.rtcplayground.presentation.socket.SignallingPresenter
import com.psimao.rtcplayground.presentation.socket.WebSocketSignallingPresenter
import org.koin.dsl.module.applicationContext

val signallingModule = applicationContext {
    bean { SignallingServerDataSource.instance }
    bean { EmitEventOnSignallingServerUseCase(get()) }
    bean { ObserveOnlineUsersUseCase(get()) }
    bean { WebSocketSignallingPresenter(get(), get(), get()) as SignallingPresenter }
}