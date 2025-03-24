package com.dev.kosongdua.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.kosongdua.R
import com.dev.kosongdua.model.MeMassage

class PemesejanAdapter(
    private val messageList: List<MeMassage>,
    private val currentUserUid: String
) : RecyclerView.Adapter<PemesejanAdapter.MessageViewHolder>() {

    abstract class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: MeMassage)
    }

    // Untuk pesan masuk (kiri)
    class ReceivedMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val isiChat: TextView = itemView.findViewById(R.id.isiChatKiri)
        private val jamChat: TextView = itemView.findViewById(R.id.jamChatKiri)
        private val hariChat: TextView = itemView.findViewById(R.id.hariChatKiri)

        override fun bind(message: MeMassage) {
            isiChat.text = message.mesejan
            jamChat.text = message.formattedTime
            hariChat.text = message.dayOfWeek
        }
    }

    // Untuk pesan keluar (kanan)
    class SentMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val isiChat: TextView = itemView.findViewById(R.id.isiChatKanan)
        private val jamChat: TextView = itemView.findViewById(R.id.jamChatKanan)
        private val hariChat: TextView = itemView.findViewById(R.id.hariChatKanan)

        override fun bind(message: MeMassage) {
            isiChat.text = message.mesejan
            jamChat.text = message.formattedTime
            hariChat.text = message.dayOfWeek
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].sender == currentUserUid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> SentMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.sendive_rightchat, parent, false)
            )
            else -> ReceivedMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.receive_leftchat, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemCount() = messageList.size

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}