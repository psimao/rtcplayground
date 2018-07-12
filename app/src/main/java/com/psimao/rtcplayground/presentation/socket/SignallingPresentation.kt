package com.psimao.rtcplayground.presentation.socket

interface SignallingView {

    fun showNotification()
    fun dismissNotification()
    fun openCallView(room: String, alias: String)
}

interface SignallingPresenter {

    fun create(view: SignallingView)
    fun destroy()
}