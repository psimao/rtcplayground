package com.psimao.rtcplayground.domain.signalling

import com.psimao.rtcplayground.data.datasource.SignallingServerDataSource
import com.psimao.rtcplayground.data.model.User
import com.psimao.rtcplayground.domain.UseCase
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.json.JSONArray

class ObserveOnlineUsersUseCase(private val signallingServerDataSource: SignallingServerDataSource): UseCase<Nothing, List<User>>() {

    private var next: ((List<User>) -> Unit)? = null

    private val observer: (SignallingServerDataSource.Event) -> Unit = {
        if (it.type == SignallingServerDataSource.Event.Type.ONLINE) {
            val onlineUsers = it.data?.getJSONArray("onlineUsers")
            next?.invoke(transformUserList(onlineUsers))
        }
    }

    override fun execute(params: Nothing?): Deferred<List<User>?> {
        return async {
            if (signallingServerDataSource.onlineUsers?.length() ?: 0 > 0) {
                transformUserList(signallingServerDataSource.onlineUsers)
            } else {
                null
            }
        }
    }

    override fun observe(params: Nothing?, next: ((List<User>) -> Unit)?) {
        this.next = next
        launch(UI) {
            signallingServerDataSource.addObserver(observer)
            if (signallingServerDataSource.onlineUsers?.length() ?: 0 > 0) {
                next?.invoke(transformUserList(signallingServerDataSource.onlineUsers))
            }
        }
    }

    override fun dispose() {
        super.dispose()
        signallingServerDataSource.removeObserver(observer)
    }

    private fun transformUserList(onlineUsers: JSONArray?): List<User> {
        val size = onlineUsers?.length() ?: 0
        val onlineUsersList: ArrayList<User> = ArrayList()
        for (i in 0 until size) {
            val jsonUser = onlineUsers?.getJSONObject(i)
            val alias = jsonUser?.getString("alias")
            val id = jsonUser?.getString("id")
            if (id != null && alias != null && alias != signallingServerDataSource.user) {
                onlineUsersList.add(User(id, alias))
            }
        }
        return onlineUsersList
    }
}