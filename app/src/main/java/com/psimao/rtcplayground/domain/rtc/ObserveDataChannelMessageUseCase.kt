package com.psimao.rtcplayground.domain.rtc

import com.psimao.rtcplayground.data.datasource.RtcConnectionDataSource
import com.psimao.rtcplayground.data.model.DataChannelMessage
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

class ObserveDataChannelMessageUseCase(
        private val rtcConnectionDataSource: RtcConnectionDataSource
) : UseCase<String, DataChannelMessage>() {

    override fun execute(params: String?): Deferred<DataChannelMessage?> {
        return async {
            null
        }
    }

    override fun observe(params: String?, next: ((DataChannelMessage) -> Unit)?) {
        rtcConnectionDataSource.dataChannelMessageObserver = {
            next?.invoke(it)
        }
    }

    override fun dispose() {
        rtcConnectionDataSource.dataChannelMessageObserver = null
    }
}