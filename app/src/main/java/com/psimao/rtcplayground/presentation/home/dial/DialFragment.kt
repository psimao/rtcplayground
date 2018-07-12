package com.psimao.rtcplayground.presentation.home.dial

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.psimao.rtcplayground.R
import com.psimao.rtcplayground.data.model.User
import com.psimao.rtcplayground.presentation.call.CallActivity
import com.psimao.rtcplayground.presentation.socket.SocketService
import kotlinx.android.synthetic.main.fragment_dial.*
import kotlinx.android.synthetic.main.item_chat_message.view.*
import kotlinx.android.synthetic.main.item_user.view.*
import org.koin.android.ext.android.inject
import java.util.*

class DialFragment : Fragment(), DialView {

    companion object {
        val TAG: String = DialFragment::class.java.name
    }

    private val presenter: DialPresenter by inject()
    private val adapter: OnlineUserAdapter by lazy { OnlineUserAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.create(this)
        recyclerViewOnlineUsers.layoutManager = LinearLayoutManager(context)
        recyclerViewOnlineUsers.adapter = adapter
        adapter.onItemSelected = {
            presenter.onUserSelected(it)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!SocketService.isConnected) {
            showNotConnected()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun updateOnlineUsersList(onlineUsers: List<User>) {
        Handler(context?.mainLooper).post {
            textNotConnected.visibility = View.GONE
            adapter.updateDataSet(onlineUsers)
        }
    }

    override fun showCallView(targetId: String, targetAlias: String) {
        startActivity(CallActivity.createIntentForInitiator(context!!, targetId, targetAlias))
    }

    override fun showNotConnected() {
        adapter.clear()
        textNotConnected.visibility = View.VISIBLE
    }

    inner class OnlineUserAdapter : RecyclerView.Adapter<OnlineUserAdapter.ViewHolder>() {

        private val onlineUsers: ArrayList<User> = ArrayList()
        private val random: Random = Random()
        var onItemSelected: ((User) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))
        }

        override fun getItemCount(): Int = onlineUsers.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(onlineUsers[position]) {
                holder.itemView.textViewUserAlias.text = alias
                holder.itemView.textViewUserId.text = id
                val red = random.nextInt(255)
                val green = random.nextInt(255)
                val blue = random.nextInt(255)
                holder.itemView.imageUser.backgroundTintList = ColorStateList.valueOf(Color.rgb(red, green, blue))
                holder.itemView.setOnClickListener {
                    onItemSelected?.invoke(this)
                }
            }
        }

        fun updateDataSet(users: List<User>) {
            onlineUsers.clear()
            onlineUsers.addAll(users)
            notifyDataSetChanged()
        }

        fun clear() {
            onlineUsers.clear()
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}