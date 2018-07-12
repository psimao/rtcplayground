package com.psimao.rtcplayground.presentation.call

import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.psimao.rtcplayground.R
import com.psimao.rtcplayground.data.model.ChatMessage
import kotlinx.android.synthetic.main.item_chat_message.view.*

class ChatItemAdapter: RecyclerView.Adapter<ChatItemAdapter.ViewHolder>() {

    private val messages: ArrayList<ChatMessage> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_chat_message,
                        parent,
                        false)
        )
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        val params = holder.view.cardViewMessage.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = R.id.layoutChatMessage
        params.bottomToBottom = R.id.layoutChatMessage
        val color = if (message.type == ChatMessage.Type.LOCAL) {
            params.startToStart = -1
            params.endToEnd = R.id.layoutChatMessage
            R.color.colorAccentInverseLight
        } else {
            params.endToEnd = -1
            params.startToStart = R.id.layoutChatMessage
            R.color.white
        }
        holder.view.cardViewMessage.layoutParams = params
        holder.view.cardViewMessage.setCardBackgroundColor(ContextCompat.getColor(holder.view.context, color))
        holder.view.textViewMessage.text = message.message
    }

    /**
     * Add new entry to adapter
     * @param message entry
     * @return Int position of added entry
     */
    fun addItem(message: ChatMessage): Int {
        messages.add(message)
        val position = messages.size - 1
        notifyItemInserted(position)
        return position
    }

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view)
}