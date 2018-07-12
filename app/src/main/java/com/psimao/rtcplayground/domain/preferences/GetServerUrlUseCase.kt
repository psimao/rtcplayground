package com.psimao.rtcplayground.domain.preferences

import com.psimao.rtcplayground.BuildConfig
import com.psimao.rtcplayground.data.datasource.SettingsPreferencesDataSource
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class GetServerUrlUseCase(private val dataSource: SettingsPreferencesDataSource): UseCase<Nothing, String?>() {

    companion object {
        const val DEFAULT_URL = BuildConfig.SIGNALLING_SERVER_URL
    }

    override fun execute(params: Nothing?): Deferred<String> {
        return async(CommonPool) {
            dataSource.get(SettingsPreferencesDataSource.KEY_URL) ?: DEFAULT_URL
        }
    }

    override fun observe(params: Nothing?, next: ((String?) -> Unit)?) {
        launch(UI) {
            params?.also {
                val result = execute().await()
                next?.invoke(result)
            }
        }
    }
}