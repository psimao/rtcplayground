package com.psimao.rtcplayground.domain.rtc

import com.psimao.rtcplayground.data.datasource.RtcConnectionDataSource
import com.psimao.rtcplayground.data.model.ChatMessage
import com.psimao.rtcplayground.data.model.DataChannelMessage
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class EmitChatMessageOnDataChannelUseCase(private val rtcConnectionDataSource: RtcConnectionDataSource): UseCase<String, ChatMessage>() {

    override fun execute(params: String?): Deferred<ChatMessage?> {
        return async {
            params?.let {
                rtcConnectionDataSource.emitOnDataChannel(DataChannelMessage.createChatMessage(it))
                ChatMessage(ChatMessage.Type.LOCAL, it)
            }
        }
    }

    override fun observe(params: String?, next: ((ChatMessage) -> Unit)?) {
        launch(UI) {
            execute(params).await()?.let {
                next?.invoke(it)
            }
        }
    }
}