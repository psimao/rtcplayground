package com.psimao.rtcplayground.domain.signalling

import com.psimao.rtcplayground.data.datasource.SignallingServerDataSource
import com.psimao.rtcplayground.data.model.SignallingEvent
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject

class EmitEventOnSignallingServerUseCase(private val signallingServerDataSource: SignallingServerDataSource): UseCase<SignallingEvent?, Unit>() {

    override fun execute(params: SignallingEvent?): Deferred<Unit?> {
        return async {
            params?.let {
                signallingServerDataSource.emitEvent(it.event, it.data)
            }
        }
    }

    override fun observe(params: SignallingEvent?, next: ((Unit) -> Unit)?) {
        launch(UI) {
            execute().await()?.let {
                next?.invoke(it)
            }
        }
    }
}