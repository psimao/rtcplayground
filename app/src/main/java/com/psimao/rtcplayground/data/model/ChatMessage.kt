package com.psimao.rtcplayground.data.model

data class ChatMessage(val type: Type, val message: String) {
    enum class Type { LOCAL, REMOTE }
}