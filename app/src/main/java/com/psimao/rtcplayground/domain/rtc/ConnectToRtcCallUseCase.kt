package com.psimao.rtcplayground.domain.rtc

import com.psimao.rtcplayground.data.datasource.RtcConnectionDataSource
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

class ConnectToRtcCallUseCase(
        private val rtcConnectionDataSource: RtcConnectionDataSource
) : UseCase<String, RtcConnectionDataSource.Status>() {

    override fun execute(params: String?): Deferred<RtcConnectionDataSource.Status?> {
        return async {
            null
        }
    }

    override fun observe(params: String?, next: ((RtcConnectionDataSource.Status) -> Unit)?) {
        rtcConnectionDataSource.connect {
            next?.invoke(it)
        }
    }

    override fun dispose() {
        rtcConnectionDataSource.dispose()
    }
}