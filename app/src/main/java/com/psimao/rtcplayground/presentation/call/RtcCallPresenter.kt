package com.psimao.rtcplayground.presentation.call

import android.location.Location
import com.psimao.rtcplayground.data.datasource.RtcConnectionDataSource
import com.psimao.rtcplayground.data.datasource.SignallingServerDataSource
import com.psimao.rtcplayground.data.model.ChatMessage
import com.psimao.rtcplayground.data.model.DataChannelMessage
import com.psimao.rtcplayground.data.model.SignallingEvent
import com.psimao.rtcplayground.domain.rtc.EmitChatMessageOnDataChannelUseCase
import com.psimao.rtcplayground.domain.rtc.EmitLocationOnDataChannelUseCase
import com.psimao.rtcplayground.domain.rtc.ConnectToRtcCallUseCase
import com.psimao.rtcplayground.domain.rtc.ObserveDataChannelMessageUseCase
import com.psimao.rtcplayground.domain.signalling.EmitEventOnSignallingServerUseCase
import kotlinx.coroutines.experimental.launch

class RtcCallPresenter(
        private val connectToRtcCallUseCase: ConnectToRtcCallUseCase,
        private val emitEventOnSignallingServerUseCase: EmitEventOnSignallingServerUseCase,
        private val emitChatMessageOnDataChannelUseCase: EmitChatMessageOnDataChannelUseCase,
        private val emitLocationOnDataChannelUseCase: EmitLocationOnDataChannelUseCase,
        private val observeDataChannelMessageUseCase: ObserveDataChannelMessageUseCase
) : CallPresenter {

    private lateinit var view: CallView

    override fun create(view: CallView) {
        this.view = view
        showLoadingCallView()
        observeSignallingServer()
        observeDataChannelMessage()
    }

    override fun start() {

    }

    override fun onCallCanceled() {
        launch {
            emitEventOnSignallingServerUseCase.execute(SignallingEvent(
                    SignallingServerDataSource.EVENT_CALL_CANCELED,
                    view.extraTarget()
            )).await()
            view.closeCallView()
        }
    }

    override fun onRejected() {
        launch {
            emitEventOnSignallingServerUseCase.execute(SignallingEvent(
                    SignallingServerDataSource.EVENT_CALL_REJECTED,
                    view.extraRoom()
            )).await()
            view.closeCallView()
        }
    }

    override fun onAccepted() {
        launch {
            emitEventOnSignallingServerUseCase.execute(SignallingEvent(
                    SignallingServerDataSource.EVENT_CALL_ANSWERED,
                    view.extraRoom()
            )).await()
        }
    }

    override fun onSendMessageClicked() {
        val message = view.chatMessageInputText()
        if (message.isNotEmpty()) {
            emitChatMessageOnDataChannelUseCase.observe(message) { view.addChatMessage(it) }
            view.clearChatMessageInput()
        }
    }

    override fun onLocationChanged(location: Location) {
        launch {
            emitLocationOnDataChannelUseCase.execute(location).await()
        }
    }

    override fun stop() {
        observeDataChannelMessageUseCase.dispose()
        connectToRtcCallUseCase.dispose()
    }

    private fun showLoadingCallView() {
        view.extraRoom()?.let {
            view.showIncomingCallView()
        } ?: run {
            view.showCallingView()
            call()
        }
    }

    private fun observeSignallingServer() {
        launch {
            connectToRtcCallUseCase.observe {
                when (it) {
                    RtcConnectionDataSource.Status.ANSWER -> view.dismissLoadingView()
                    RtcConnectionDataSource.Status.DISCONNECTED -> view.closeCallView()
                    else -> {}
                }
            }
        }
    }

    private fun observeDataChannelMessage() {
        launch {
            observeDataChannelMessageUseCase.observe {
                when (it.type) {
                    DataChannelMessage.TYPE_LOCATION -> view.updateRemoteLocation(Location("").apply {
                        latitude = it.data.getDouble(DataChannelMessage.KEY_LATITUDE)
                        longitude = it.data.getDouble(DataChannelMessage.KEY_LONGITUDE)
                    })
                    DataChannelMessage.TYPE_CHAT_MESSAGE -> view.addChatMessage(ChatMessage(
                            ChatMessage.Type.REMOTE,
                            it.data.getString(DataChannelMessage.KEY_MESSAGE)
                    ))
                }
            }
        }
    }

    private fun call() {
        launch {
            emitEventOnSignallingServerUseCase.execute(SignallingEvent(
                    SignallingServerDataSource.EVENT_CALL,
                    view.extraTarget()
            )).await()
        }
    }

}