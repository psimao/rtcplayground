package com.psimao.rtcplayground.presentation.home.settings

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.psimao.rtcplayground.R
import com.psimao.rtcplayground.extensions.onTextChanged
import com.psimao.rtcplayground.presentation.socket.SocketService
import kotlinx.android.synthetic.main.fragment_settings.*
import org.koin.android.ext.android.inject

class SettingsFragment : Fragment(), SettingsView {

    companion object {
        val TAG: String = SettingsFragment::class.java.name
    }

    private val presenter: SettingsPresenter by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.create(this)
        buttonConnect.setOnClickListener {
            presenter.onConnectClicked()
        }
        editWebSocketUrl.onTextChanged {
            if(it.isNotEmpty()) {
                presenter.onServerUrlChanged(it)
            }
        }
        editWebSocketUser.onTextChanged {
            if(it.isNotEmpty()) {
                presenter.onUserChanged(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onResume() {
        super.onResume()
        if (SocketService.isConnected) {
            connectedUI()
        } else {
            disconnectedUI()
        }
    }

    override fun setServerUrl(url: String) {
        editWebSocketUrl.setText(url)
    }

    override fun setUser(user: String) {
        editWebSocketUser.setText(user)
    }

    override fun startSignallingService() {
        val intent = Intent(context?.applicationContext, SocketService::class.java)
        if (SocketService.isConnected) {
            context?.stopService(intent)
            disconnectedUI()
        } else {
            context?.startService(intent)
            connectedUI()
        }
    }

    private fun connectedUI() {
        editWebSocketUrl.isEnabled = false
        editWebSocketUser.isEnabled = false
        buttonConnect.setText(R.string.disconnect)
    }

    private fun disconnectedUI() {
        editWebSocketUrl.isEnabled = true
        editWebSocketUser.isEnabled = true
        buttonConnect.setText(R.string.connect)
    }
}