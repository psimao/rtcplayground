package com.psimao.rtcplayground.domain.rtc

import android.location.Location
import com.psimao.rtcplayground.data.datasource.RtcConnectionDataSource
import com.psimao.rtcplayground.data.model.DataChannelMessage
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class EmitLocationOnDataChannelUseCase(private val rtcConnectionDataSource: RtcConnectionDataSource): UseCase<Location, Unit>() {

    override fun execute(params: Location?): Deferred<Unit?> {
        return async {
            params?.let {
                rtcConnectionDataSource.emitOnDataChannel(DataChannelMessage.createLocationMessage(it.latitude, it.longitude))
            }
        }
    }

    override fun observe(params: Location?, next: ((Unit) -> Unit)?) {
        launch(UI) {
            execute(params).await()?.let {
                next?.invoke(it)
            }
        }
    }
}