package com.psimao.rtcplayground.presentation.call

import android.location.Location
import com.psimao.rtcplayground.data.model.ChatMessage

interface CallView {

    fun showIncomingCallView()
    fun showCallingView()
    fun dismissLoadingView()
    fun closeCallView()
    fun addChatMessage(chatMessage: ChatMessage)
    fun updateLocalLocation(location: Location)
    fun updateRemoteLocation(location: Location)
    fun chatMessageInputText(): String
    fun clearChatMessageInput()
    fun extraRoom(): String?
    fun extraTarget(): String?
}

interface CallPresenter {

    fun create(view: CallView)
    fun start()
    fun onCallCanceled()
    fun onRejected()
    fun onAccepted()
    fun onSendMessageClicked()
    fun onLocationChanged(location: Location)
    fun stop()
}