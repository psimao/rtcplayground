package com.psimao.rtcplayground.presentation.home.settings

import com.psimao.rtcplayground.domain.preferences.GetServerUrlUseCase
import com.psimao.rtcplayground.domain.preferences.GetUserUseCase
import com.psimao.rtcplayground.domain.preferences.StoreServerUrlUseCase
import com.psimao.rtcplayground.domain.preferences.StoreUserUseCase
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class PreferencesSettingsPresenter(
        private val getUserUseCase: GetUserUseCase,
        private val getServerUrlUseCase: GetServerUrlUseCase,
        private val storeUserUseCase: StoreUserUseCase,
        private val storeServerUrlUseCase: StoreServerUrlUseCase
): SettingsPresenter {

    private lateinit var view: SettingsView

    override fun create(view: SettingsView) {
        this.view = view
    }

    override fun start() {
        launch(UI) {
            view.setUser(getUserUseCase.execute().await())
            view.setServerUrl(getServerUrlUseCase.execute().await())
        }
    }

    override fun onServerUrlChanged(url: String) {
        launch {
            storeServerUrlUseCase.execute(url).await()
        }
    }

    override fun onUserChanged(user: String) {
        launch {
            storeUserUseCase.execute(user).await()
        }
    }

    override fun onConnectClicked() {
        view.startSignallingService()
    }
}