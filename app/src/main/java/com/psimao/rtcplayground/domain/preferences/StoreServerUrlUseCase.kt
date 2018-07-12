package com.psimao.rtcplayground.domain.preferences

import com.psimao.rtcplayground.data.datasource.SettingsPreferencesDataSource
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class StoreServerUrlUseCase(private val dataSource: SettingsPreferencesDataSource): UseCase<String, Unit?>() {

    override fun execute(params: String?): Deferred<Unit?> {
        return async(CommonPool) {
            params?.let {
                dataSource.put(SettingsPreferencesDataSource.KEY_URL, it)
            }
        }
    }

    override fun observe(params: String?, next: ((Unit?) -> Unit)?) {
        launch(UI) {
            params?.also {
                val result = execute().await()
                next?.invoke(result)
            }
        }
    }
}