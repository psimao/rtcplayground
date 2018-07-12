package com.psimao.rtcplayground.presentation.call

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.psimao.rtcplayground.R
import kotlinx.android.synthetic.main.fragment_loading_call.*

class CallLoadingFragment : DialogFragment() {

    companion object {
        val TAG: String = CallLoadingFragment::class.java.name

        private const val KEY_INCOMING = "incoming"
        private const val KEY_ALIAS = "alias"

        fun newInstance(incoming: Boolean, alias: String? = null): CallLoadingFragment {
            return CallLoadingFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_INCOMING, incoming)
                    putString(KEY_ALIAS, alias)
                }
            }
        }
    }

    private var callStatusListener: ((accepted: Boolean) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loading_call, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonRejectCall.setOnClickListener { rejectCall() }
        val message = if (arguments?.containsKey(KEY_INCOMING) == true && arguments?.getBoolean(KEY_INCOMING) == true) {
            buttonAcceptCall.visibility = View.VISIBLE
            buttonAcceptCall.setOnClickListener { acceptCall() }
            getString(R.string.receiving_a_call, arguments?.getString(KEY_ALIAS))
        } else {
            getString(R.string.calling, arguments?.getString(KEY_ALIAS))
        }
        textViewCallLoadingMessage.text = message
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window.setLayout(width, height)
        }
    }

    fun setOnCallStatusListener(listener: (accepted: Boolean) -> Unit) {
        callStatusListener = listener
    }

    private fun rejectCall() {
        callStatusListener?.invoke(false)
        dismiss()
    }

    private fun acceptCall() {
        callStatusListener?.invoke(true)
        dismiss()
    }
}