package com.psimao.rtcplayground.presentation.home.settings

interface SettingsView {

    fun setServerUrl(url: String)
    fun setUser(user: String)
    fun startSignallingService()
}

interface SettingsPresenter {

    fun create(view: SettingsView)
    fun start()
    fun onServerUrlChanged(url: String)
    fun onUserChanged(user: String)
    fun onConnectClicked()
}