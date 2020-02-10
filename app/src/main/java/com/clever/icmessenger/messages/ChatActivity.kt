package com.clever.icmessenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.clever.icmessenger.R
import com.clever.icmessenger.models.ChatMessage
import com.clever.icmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerChat.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        buttonSendChat.setOnClickListener {
            Log.d("CHAT","Tentando enviar mensagem")
            performSendMessage()
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d("CHAT", chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }

                recyclerChat.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        val text = textChat.text.toString()
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromId = FirebaseAuth.getInstance().uid ?: return
        val toId = user.uid

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("CHAT", "Mensagem salva! ${reference.key}")
                textChat.text.clear()
                recyclerChat.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        latestMessageRef.setValue(chatMessage)
        latestMessageToRef.setValue(chatMessage)

    }
}

class ChatFromItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textFromRow.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageChatFromMessage
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textToRow.text = text

        //imagem do usuario ao lado
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageChatToMessage
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}