package com.psimao.rtcplayground.modules

import com.psimao.rtcplayground.data.datasource.RtcConnectionDataSource
import com.psimao.rtcplayground.domain.rtc.ConnectToRtcCallUseCase
import com.psimao.rtcplayground.domain.rtc.EmitChatMessageOnDataChannelUseCase
import com.psimao.rtcplayground.domain.rtc.EmitLocationOnDataChannelUseCase
import com.psimao.rtcplayground.domain.rtc.ObserveDataChannelMessageUseCase
import org.koin.dsl.module.applicationContext

val rtcModule = applicationContext {
    bean { RtcConnectionDataSource(get(), get()) }
    bean { ConnectToRtcCallUseCase(get()) }
    bean { EmitLocationOnDataChannelUseCase(get()) }
    bean { EmitChatMessageOnDataChannelUseCase(get()) }
    bean { ObserveDataChannelMessageUseCase(get()) }
}