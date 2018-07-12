package com.psimao.rtcplayground.presentation.socket

import com.psimao.rtcplayground.data.datasource.SignallingServerDataSource
import com.psimao.rtcplayground.domain.preferences.GetServerUrlUseCase
import com.psimao.rtcplayground.domain.preferences.GetUserUseCase
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class WebSocketSignallingPresenter(
        private val getUserUseCase: GetUserUseCase,
        private val getServerUrlUseCase: GetServerUrlUseCase,
        private val signallingServerDataSource: SignallingServerDataSource
) : SignallingPresenter {

    private lateinit var view: SignallingView

    override fun create(view: SignallingView) {
        this.view = view
        view.showNotification()
        launch(UI) {
            val url = getServerUrlUseCase.execute().await()
            val user = getUserUseCase.execute().await()
            signallingServerDataSource.let {
                it.connect(url, user)
                it.addObserver { onWebSocketEvent(it) }
            }
        }
    }

    override fun destroy() {
        signallingServerDataSource.emitEvent(SignallingServerDataSource.EVENT_LEAVE, null)
        signallingServerDataSource.disconnect()
        view.dismissNotification()
    }

    private fun onWebSocketEvent(event: SignallingServerDataSource.Event) {
        if (event.type == SignallingServerDataSource.Event.Type.INCOMING) {
            val room = event.data?.getString("room") ?: ""
            val alias = event.data?.getString("alias") ?: ""
            view.openCallView(room, alias)
        }
    }
}