package com.psimao.rtcplayground.presentation.home.dial

import com.psimao.rtcplayground.data.model.User
import com.psimao.rtcplayground.domain.signalling.ObserveOnlineUsersUseCase

class HomeDialPresenter(private val observeOnlineUsersUseCase: ObserveOnlineUsersUseCase) : DialPresenter {

    private lateinit var view: DialView

    override fun create(view: DialView) {
        this.view = view
        observeOnlineUsersUseCase.observe {
            view.updateOnlineUsersList(it)
        }
    }

    override fun destroy() {
        observeOnlineUsersUseCase.dispose()
    }

    override fun onUserSelected(user: User) {
        view.showCallView(user.id, user.alias)
    }
}