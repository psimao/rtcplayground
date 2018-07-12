package com.psimao.rtcplayground.data.datasource

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("LogNotTimber")
class SignallingServerDataSource private constructor() {

    companion object {

        private val TAG = SignallingServerDataSource::class.java.name

        const val EVENT_PING = "ping"
        const val EVENT_JOIN = "join"
        const val EVENT_ONLINE = "online"
        const val EVENT_CREATED = "created"
        const val EVENT_CALL = "call"
        const val EVENT_CALL_ANSWERED = "call-answered"
        const val EVENT_CALL_REJECTED = "call-rejected"
        const val EVENT_CALL_CANCELED = "call-canceled"
        const val EVENT_INCOMING = "incoming"
        const val EVENT_REJECTED = "rejected"
        const val EVENT_READY = "ready"
        const val EVENT_RTCMESSAGE = "rtcmessage"
        const val EVENT_BYE = "bye"
        const val EVENT_LEAVE = "leave"

        val instance by lazy { SignallingServerDataSource() }
    }

    private var socket: Socket? = null

    private var socketHandlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    private val observers: ArrayList<(Event) -> Unit> = ArrayList()

    var user: String? = null
        private set
    var onlineUsers: JSONArray? = null
        private set

    fun addObserver(observer: (Event) -> Unit) {
        observers.add(observer)
    }

    fun removeObserver(observer: (Event) -> Unit) {
        observers.remove(observer)
    }

    fun connect(url: String, user: String) {
        this.user = user
        socketHandlerThread = HandlerThread("socketHandler").apply { start() }
        socketHandler = Handler(socketHandlerThread?.looper)
        socketHandler?.post {
            socket = IO.socket(url)
            socket?.apply {
                // Unregister
                off(EVENT_PING)
                off(EVENT_INCOMING)
                off(EVENT_REJECTED)
                off(EVENT_CREATED)
                off(EVENT_READY)
                off(EVENT_RTCMESSAGE)
                off(EVENT_ONLINE)

                // Register
                on(EVENT_PING) { emitPong() }
                on(EVENT_INCOMING) { postIncomingMessage(it[0] as JSONObject) }
                on(EVENT_REJECTED) { postRejectedMessage() }
                on(EVENT_CREATED) { postCreatedMessage(it[0] as String) }
                on(EVENT_READY) { postReadyMessage() }
                on(EVENT_RTCMESSAGE) { processRtcMessage(it) }
                on(EVENT_ONLINE) { postOnlineUsers(it[0] as JSONArray) }

                // Authenticate
                connect().emit(EVENT_JOIN, user)
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.close()
        observers.clear()
        socketHandlerThread?.quit()
        onlineUsers = null
    }

    fun emitEvent(event: String, args: Any?) {
        Log.i(TAG, "emitting $event: $args")
        socket?.emit(event, args)
    }

    private fun emitPong() {
        Log.i(TAG, "ping pong")
        socket?.emit("pong", null)
    }

    private fun processRtcMessage(data: Array<Any?>) {
        Log.i(TAG, "rtcmessage: ${data.first()}")
        try {
            if (data.first() is String && data.first() as String == "bye") {
                postByeMessage()
            } else {
                val json = data.first() as JSONObject
                when (json.getString("type")) {
                    "offer" -> postOfferMessage(json)
                    "answer" -> postAnswerMessage(json)
                    "candidate" -> postCandidateMessage(json)
                    "bye" -> postByeMessage()
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun postOnlineUsers(users: JSONArray) {
        this.onlineUsers = users
        postMessage(Event(Event.Type.ONLINE, JSONObject().apply { put("onlineUsers", users) }))
    }

    private fun postIncomingMessage(incomingCallInfo: JSONObject) {
        Log.i(TAG, incomingCallInfo.toString())
        postMessage(Event(Event.Type.INCOMING, incomingCallInfo))
    }

    private fun postRejectedMessage() {
        Log.i(TAG, "Call Rejected")
        postMessage(Event(Event.Type.REJECTED, null))
    }

    private fun postCreatedMessage(room: String) {
        Log.i(TAG, "created: $room")
        postMessage(Event(Event.Type.CREATED, JSONObject().apply { put("room", room) }))
    }

    private fun postByeMessage() {
        postMessage(Event(Event.Type.BYE, null))
    }

    private fun postReadyMessage() {
        postMessage(Event(Event.Type.READY, null))
    }

    private fun postOfferMessage(data: JSONObject) {
        postMessage(Event(Event.Type.OFFER, data))
    }

    private fun postAnswerMessage(data: JSONObject) {
        postMessage(Event(Event.Type.ANSWER, data))
    }

    private fun postCandidateMessage(data: JSONObject) {
        postMessage(Event(Event.Type.CANDIDATE, data))
    }

    private fun postMessage(event: Event) {
        observers.forEach {
            it.invoke(event)
        }
    }

    data class Event(val type: Type, val data: JSONObject?) {

        enum class Type { ONLINE, INCOMING, REJECTED, CREATED, READY, OFFER, ANSWER, CANDIDATE, BYE }
    }
}