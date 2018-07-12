package com.psimao.rtcplayground.data.datasource

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.psimao.rtcplayground.data.model.DataChannelMessage
import com.psimao.rtcplayground.presentation.call.CallActivity
import org.json.JSONObject
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import org.webrtc.MediaConstraints



class RtcConnectionDataSource(
        private val applicationContext: Context? = null,
        private val signallingServerDataSource: SignallingServerDataSource
) {

    companion object {
        private val TAG: String = RtcConnectionDataSource::class.java.name
    }

    enum class Status { DISCONNECTED, ROOM_CREATED, READY, OFFER, ANSWER }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null

    private var videoCapturer: VideoCapturer? = null

    private val audioManager: AudioManager by lazy { applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private val observer: (SignallingServerDataSource.Event) -> Unit by lazy { createObserver() }

    private var isInitiator: Boolean = false
    private var isChannelReady: Boolean = false
    private var room: String? = null

    var rootEglBase: EglBase? = null
    var surfaceViewRendererLocal: SurfaceViewRenderer? = null
    var surfaceViewRendererRemote: SurfaceViewRenderer? = null

    var statusObserver: ((Status) -> Unit)? = null
    var dataChannelMessageObserver: ((DataChannelMessage) -> Unit)? = null

    private val sdpConstraints: MediaConstraints by lazy {
        MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
    }

    fun connect(statusObserver: (Status) -> Unit) {
        this.statusObserver = statusObserver
        signallingServerDataSource.addObserver(observer)
    }

    fun dispose() {
        peerConnection?.dispose()
        peerConnection = null
        videoCapturer?.stopCapture()
        videoCapturer = null
        videoCapturer?.dispose()
        videoCapturer = null
        dataChannel?.dispose()
        dataChannel = null
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        isChannelReady = false
        isInitiator = false
        videoCapturer = null
        dataChannel = null
        peerConnection = null
        signallingServerDataSource.removeObserver(observer)
        signallingServerDataSource.emitEvent(SignallingServerDataSource.EVENT_BYE, null)
    }

    fun emitOnDataChannel(json: JSONObject) = emitOnDataChannel(json.toString())

    fun emitOnDataChannel(string: String) {
        val byteBuffer = ByteBuffer.wrap(string.toByteArray())
        val dataChannelBuffer = DataChannel.Buffer(byteBuffer, false)
        dataChannel?.send(dataChannelBuffer)
        Log.i(TAG, "Sent \"$string\" on data channel \"${dataChannel?.label()}\"")
    }

    private fun createPeerConnection() {
        initializePeerConnectionFactory()

        val options = PeerConnectionFactory.Options()

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .createPeerConnectionFactory()

        peerConnectionFactory?.setVideoHwAccelerationOptions(rootEglBase?.eglBaseContext, rootEglBase?.eglBaseContext)

        val localMediaStream = peerConnectionFactory?.createLocalMediaStream(UUID.randomUUID().toString())

        // Local Audio Stream
        val audioSource = peerConnectionFactory?.createAudioSource(sdpConstraints)
        val audioTrack = peerConnectionFactory?.createAudioTrack(UUID.randomUUID().toString(), audioSource)
        localMediaStream?.addTrack(audioTrack)

        // Local Video Stream
        videoCapturer = createCameraCapturer(Camera2Enumerator(applicationContext))
        val videoSource = peerConnectionFactory?.createVideoSource(videoCapturer)
        val videoTrack = peerConnectionFactory?.createVideoTrack(UUID.randomUUID().toString(), videoSource)
        videoTrack?.setEnabled(true)
        videoCapturer?.startCapture(720, 1280, 30)
        localMediaStream?.addTrack(videoTrack)

        // Attach to view
        videoTrack?.addSink(surfaceViewRendererLocal)

        // Create Connection
        val stunServer = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        peerConnection = peerConnectionFactory?.createPeerConnection(listOf(stunServer), peerConnectionObserver)

        if (isInitiator) {
            val init = DataChannel.Init()
            dataChannel = peerConnection?.createDataChannel(UUID.randomUUID().toString(), init)
            dataChannel?.registerObserver(dataChannelObserver(dataChannel))
        }

        peerConnection?.addStream(localMediaStream)
    }

    private fun initializePeerConnectionFactory() = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
            .setEnableVideoHwAcceleration(true)
            .createInitializationOptions().let {
                PeerConnectionFactory.initialize(it)
            }

    private fun createObserver(): (SignallingServerDataSource.Event) -> Unit = object : (SignallingServerDataSource.Event) -> Unit {

        override fun invoke(event: SignallingServerDataSource.Event) {
            when (event.type) {
                SignallingServerDataSource.Event.Type.CREATED -> {
                    room = event.data?.getString("room")
                    isInitiator = true
                }
                SignallingServerDataSource.Event.Type.READY -> {
                    createPeerConnection()
                    isChannelReady = true
                    if (isInitiator) {
                        createOffer()
                    }
                }
                SignallingServerDataSource.Event.Type.OFFER -> {
                    SessionDescription(SessionDescription.Type.OFFER, event.data?.getString("sdp")).let {
                        onOffer(it)
                        statusObserver?.invoke(Status.OFFER)
                    }
                }
                SignallingServerDataSource.Event.Type.ANSWER -> {
                    SessionDescription(SessionDescription.Type.ANSWER, event.data?.getString("sdp")).let {
                        onAnswer(it)
                        statusObserver?.invoke(Status.ANSWER)
                    }
                }
                SignallingServerDataSource.Event.Type.CANDIDATE -> {
                    val candidate = IceCandidate(
                            event.data?.getString("id"),
                            event.data?.getInt("label")!!,
                            event.data.getString("candidate")
                    )
                    peerConnection?.addIceCandidate(candidate)
                    Log.d(CallActivity.TAG, "Candidate ${event.data} added")
                }
                SignallingServerDataSource.Event.Type.BYE, SignallingServerDataSource.Event.Type.REJECTED -> {
                    statusObserver?.invoke(Status.DISCONNECTED)
                }
                else -> {
                    /** IGNORE **/
                }
            }
        }
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): CameraVideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(CallActivity.TAG, "Creating other camera capturer.")
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private val peerConnectionObserver = object : PeerConnection.Observer {

        override fun onIceCandidate(candidate: IceCandidate) {
            val json = JSONObject().apply {
                put("type", "candidate")
                put("label", candidate.sdpMLineIndex)
                put("id", candidate.sdpMid)
                put("candidate", candidate.sdp)
            }
            signallingServerDataSource.emitEvent("rtcmessage", json)
        }

        override fun onDataChannel(dc: DataChannel?) {
            Log.i(CallActivity.TAG, "DataChannel: onDataChannel - ${dc?.label()}")
            if (!isInitiator) {
                dataChannel = dc
                dataChannel?.registerObserver(dataChannelObserver(dataChannel))
            }
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            Log.i(CallActivity.TAG, "onIceConnectionReceivingChange")
        }

        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
            Log.i(CallActivity.TAG, "onIceConnectionChange $state")
            if (state == PeerConnection.IceConnectionState.DISCONNECTED) {
                statusObserver?.invoke(Status.DISCONNECTED)
                audioManager.mode = AudioManager.MODE_NORMAL
            }
        }

        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
            Log.i(CallActivity.TAG, "onIceGatheringChange")
        }

        override fun onAddStream(mediaStream: MediaStream?) {
            Log.i(CallActivity.TAG, "onAddStream ${mediaStream?.videoTracks?.size}")
            mediaStream?.videoTracks?.first()?.apply {
                setEnabled(true)
                addSink(surfaceViewRendererRemote)
            }
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
        }

        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
            Log.i(CallActivity.TAG, "onSignalingChange")
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
            Log.i(CallActivity.TAG, "onIceCandidatesRemoved")
        }

        override fun onRemoveStream(p0: MediaStream?) {
            Log.i(CallActivity.TAG, "onRemoveStream")
        }

        override fun onRenegotiationNeeded() {
            Log.i(CallActivity.TAG, "onRenegotiationNeeded")
        }

        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
            Log.i(CallActivity.TAG, "onAddTrack")
        }
    }

    private fun createOffer() {
        peerConnection?.createOffer(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onCreateSuccess(sdp: SessionDescription) {
                setLocalSdp(sdp)
            }
        }, sdpConstraints)
    }

    private fun createAnswer() {
        Log.i(CallActivity.TAG, "creating answer")
        peerConnection?.createAnswer(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onCreateSuccess(sdp: SessionDescription) {
                setLocalSdp(sdp)
            }
        }, sdpConstraints)
    }

    private fun onOffer(sdp: SessionDescription) {
        Log.i(CallActivity.TAG, "setting remote sdp ${sdp.type}/${sdp.description}")
        peerConnection?.setRemoteDescription(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetSuccess() {
                createAnswer()
            }
        }, sdp)
    }

    private fun onAnswer(sdp: SessionDescription) {
        Log.i(CallActivity.TAG, "setting remote sdp ${sdp.type}/${sdp.description}")
        peerConnection?.setRemoteDescription(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetSuccess() {

            }
        }, sdp)
    }

    private fun setLocalSdp(sdp: SessionDescription) {
        Log.i(CallActivity.TAG, "setting local sdp ${sdp.type}/${sdp.description}")
        peerConnection?.setLocalDescription(object : SdpObserver {

            override fun onSetFailure(p0: String?) {
            }

            override fun onSetSuccess() {
                val json = JSONObject().apply {
                    put("type", sdp.type.canonicalForm().toLowerCase())
                    put("sdp", sdp.description)
                }
                signallingServerDataSource.emitEvent("rtcmessage", json)
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onCreateSuccess(sdp: SessionDescription) {
            }

        }, sdp)
    }

    private fun dataChannelObserver(dc: DataChannel?): DataChannel.Observer = object : DataChannel.Observer {

        override fun onBufferedAmountChange(previousAmount: Long) {
            Log.d(CallActivity.TAG, "DataChannel: buffered amount changed: " + dc?.label() + ": " + dc?.state())
        }

        override fun onStateChange() {
            Log.d(CallActivity.TAG, "DataChannel: state changed: " + dc?.label() + ": " + dc?.state())
        }

        override fun onMessage(buffer: DataChannel.Buffer) {
            Log.d(CallActivity.TAG, "DataChannel: message received: " + dc?.label())
            if (buffer.binary) {
                Log.d(CallActivity.TAG, "Received binary msg over $dc")
                return
            }
            val data = buffer.data
            val bytes = ByteArray(data.capacity())
            data.get(bytes)
            val strData = String(bytes, Charset.forName("UTF-8"))
            Log.d(CallActivity.TAG, "Got msg: $strData over $dc")
            try {
                val json = JSONObject(strData)
                val jsonData = json.getJSONObject("data")
                when (json.getString("type")) {
                    DataChannelMessage.TYPE_LOCATION -> onLocation(jsonData)
                    DataChannelMessage.TYPE_CHAT_MESSAGE -> onChatMessage(jsonData)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }

        }

        private fun onChatMessage(data: JSONObject) {
            dataChannelMessageObserver?.invoke(DataChannelMessage(DataChannelMessage.TYPE_CHAT_MESSAGE, data))
        }

        private fun onLocation(data: JSONObject) {
            dataChannelMessageObserver?.invoke(DataChannelMessage(DataChannelMessage.TYPE_LOCATION, data))
        }
    }

}