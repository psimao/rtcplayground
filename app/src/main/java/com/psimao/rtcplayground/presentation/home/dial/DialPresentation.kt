package com.psimao.rtcplayground.presentation.home.dial

import com.psimao.rtcplayground.data.model.User

interface DialPresenter {

    fun create(view: DialView)
    fun onUserSelected(user: User)
    fun destroy()
}

interface DialView {

    fun showCallView(targetId: String, targetAlias: String)
    fun updateOnlineUsersList(onlineUsers: List<User>)
    fun showNotConnected()
}