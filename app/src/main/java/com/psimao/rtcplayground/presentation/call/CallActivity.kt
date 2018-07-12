package com.psimao.rtcplayground.presentation.call

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.psimao.rtcplayground.R
import com.psimao.rtcplayground.data.datasource.RtcConnectionDataSource
import com.psimao.rtcplayground.data.model.ChatMessage
import com.psimao.rtcplayground.extensions.createBitmap
import com.psimao.rtcplayground.modules.homeModule
import kotlinx.android.synthetic.main.activity_call.*
import org.koin.android.ext.android.inject
import org.webrtc.EglBase
import org.webrtc.RendererCommon

class CallActivity : AppCompatActivity(), OnMapReadyCallback, CallView {

    companion object {
        val TAG: String = CallActivity::class.java.name
        private const val EXTRA_ROOM = "extra-room"
        private const val EXTRA_ALIAS = "extra-alias"
        private const val EXTRA_TARGET_ID = "extra-target-id"
        private const val EXTRA_TARGET_ALIAS = "extra-target-alias"

        fun createIntent(context: Context, room: String?, alias: String?): Intent {
            return Intent(context, CallActivity::class.java).apply {
                putExtra(EXTRA_ROOM, room)
                putExtra(EXTRA_ALIAS, alias)
            }
        }

        fun createIntentForInitiator(context: Context, targetId: String?, targetAlias: String?): Intent {
            return Intent(context, CallActivity::class.java).apply {
                putExtra(EXTRA_TARGET_ID, targetId)
                putExtra(EXTRA_TARGET_ALIAS, targetAlias)
            }
        }
    }

    private val presenter: CallPresenter by inject()
    private val rtcDataSource: RtcConnectionDataSource by inject()

    private val rootEglBase: EglBase by lazy { EglBase.create() }

    private var room: String? = null
    private var targetId: String? = null
    private var alias: String? = null

    private val chatAdapter = ChatItemAdapter()

    private var map: GoogleMap? = null
    private var locationClient: FusedLocationProviderClient? = null
    private val locationCallback: LocationCallback by lazy { createLocationCallback() }
    private var localMarker: Marker? = null
    private var remoteMarker: Marker? = null

    private var loadingFragment: CallLoadingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        initViews()
        setExtraData()
        presenter.create(this)
        rtcDataSource.apply {
            rootEglBase = this@CallActivity.rootEglBase
            surfaceViewRendererLocal = this@CallActivity.surfaceViewRendererLocal
            surfaceViewRendererRemote = this@CallActivity.surfaceViewRendererRemote
        }
    }

    override fun onDestroy() {
        presenter.stop()
        rootEglBase.release()
        surfaceViewRendererLocal?.release()
        surfaceViewRendererRemote?.release()
        locationClient?.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        locationClient = LocationServices.getFusedLocationProviderClient(this).apply {
            val locationRequest = LocationRequest().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 2500
                fastestInterval = 1000
            }
            requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun showIncomingCallView() {
        CallLoadingFragment.newInstance(true, alias).apply {
            loadingFragment = this
            setOnCallStatusListener {
                if (it && room != null) {
                    presenter.onAccepted()
                } else {
                    presenter.onRejected()
                }
            }
            show(supportFragmentManager, CallLoadingFragment.TAG)
        }
    }

    override fun showCallingView() {
        CallLoadingFragment.newInstance(false, alias).apply {
            loadingFragment = this
            setOnCallStatusListener {
                if (!it) {
                    presenter.onCallCanceled()
                }
            }
            show(supportFragmentManager, CallLoadingFragment.TAG)
        }
    }

    override fun dismissLoadingView() {
        loadingFragment?.dismiss()
    }

    override fun closeCallView() {
        finish()
    }

    override fun addChatMessage(chatMessage: ChatMessage) {
        Handler(mainLooper).post {
            val position = chatAdapter.addItem(chatMessage)
            recyclerViewChat.smoothScrollToPosition(position)
        }
    }

    override fun updateLocalLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (localMarker == null) {
            localMarker = map?.addMarker(MarkerOptions().position(latLng).title("Local"))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        } else {
            localMarker?.position = latLng
        }
    }

    override fun updateRemoteLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (remoteMarker == null) {
            val icon = BitmapDescriptorFactory.fromBitmap(ContextCompat.getDrawable(
                    this@CallActivity,
                    R.drawable.ic_android)
                    ?.createBitmap())
            Handler(mainLooper).post {
                remoteMarker = map?.addMarker(MarkerOptions().position(latLng).title("Remote").icon(icon))
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        } else {
            Handler(mainLooper).post {
                remoteMarker?.position = latLng
            }
        }
    }

    override fun chatMessageInputText(): String = editTextMessage.text.toString()

    override fun clearChatMessageInput() = editTextMessage.setText("")

    override fun extraRoom(): String? = room

    override fun extraTarget(): String? = targetId

    private fun initViews() {
        initChatRecyclerView()
        initSendMessageButton()
        initKeyboardActions()
        initMapsFragment()
        initSurfaces()
    }

    private fun initChatRecyclerView() {
        with(recyclerViewChat) {
            layoutManager = LinearLayoutManager(this@CallActivity).apply {
                //reverseLayout = true
                stackFromEnd = true
            }
            adapter = chatAdapter
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val position = chatAdapter.itemCount - 1
                if (position >= 0) {
                    recyclerViewChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }
            }
        }
    }

    private fun initSendMessageButton() {
        buttonSendMessage.setOnClickListener {
            presenter.onSendMessageClicked()
        }
    }

    private fun initKeyboardActions() {
        editTextMessage.setOnEditorActionListener { _, actionId, event ->
            if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_SEND) {
                presenter.onSendMessageClicked()
            }
            true
        }
    }

    private fun initMapsFragment() {
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).apply {
            getMapAsync(this@CallActivity)
        }
    }

    private fun initSurfaces() {
        surfaceViewRendererLocal.init(rootEglBase.eglBaseContext, null)
        surfaceViewRendererLocal.setEnableHardwareScaler(true)
        surfaceViewRendererLocal.setMirror(true)
        surfaceViewRendererLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

        surfaceViewRendererRemote.init(rootEglBase.eglBaseContext, null)
        surfaceViewRendererRemote.setEnableHardwareScaler(false)
        surfaceViewRendererRemote.setMirror(true)
        surfaceViewRendererRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
    }

    private fun createLocationCallback(): LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            locationResult.locations.first().let {
                updateLocalLocation(it)
                presenter.onLocationChanged(it)
            }
        }
    }

    private fun setExtraData() {
        targetId = intent.getStringExtra(EXTRA_TARGET_ID)
        room = intent.getStringExtra(EXTRA_ROOM)
        alias = intent.getStringExtra(EXTRA_ALIAS) ?: intent.getStringExtra(EXTRA_TARGET_ALIAS)
    }
}
