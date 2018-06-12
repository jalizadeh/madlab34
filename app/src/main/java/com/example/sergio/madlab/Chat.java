package com.example.sergio.madlab;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.example.sergio.madlab.Classes.*;

public class Chat extends AppCompatActivity {

    private User user;

    private ImageView sendButton;
    private EditText messageArea;

    private RecyclerView mChatList;
    private FirebaseRecyclerAdapter<Message, ChatViewHolder> firebaseRecyclerAdapter;

    //
    private String currentUserId, bookOwnerId;
    private String userDisplayName,requesterDisplayName, bookOwnerName,bookRequesterID;
    private FirebaseUser firebaseUser;
    private DatabaseReference userChats, dbChats, dbChatHistory, database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendButton = (ImageView) findViewById(R.id.sendButton);
        messageArea = (EditText) findViewById(R.id.messageArea);


        //only need this part of the database
        database = FirebaseDatabase.getInstance().getReference();
        dbChats = database.child("chats");
        dbChatHistory = database.child("chat_history");


        //manage users - currentUser & bookOwnerId
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = firebaseUser.getUid();

        //get the book Owner ID from viewBook / AllChats
        bookOwnerId = getIntent().getStringExtra("bookOwnerId");
        //bookOwnerName comes from AllChats
        //bookOwnerName = getIntent().getStringExtra("bookOwnerName");
        bookRequesterID = getIntent().getStringExtra("bookRequesterID");


        //Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        //toolbar.setTitle("Chatting: " + requesterDisplayName);
        toolbar.setTitle("Chatting");



        showAllChats();
        getUserProfile();
        getRequesterProfile();


        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if (!messageText.equals("")) {
                    final Date date = new Date();

                    Message msg = new Message();
                    msg.setTime(formatter.format(date));
                    msg.setMessage(userDisplayName + ":\n"+messageText);
                    if (currentUserId.contains(bookOwnerId)){
                        msg.setWho("1");
                    } else {
                        msg.setWho("0");
                    }

                    userChats.push().setValue(msg);

                    //always update the last chat
                    ChatHistory ch = new ChatHistory();
                    ch.setChatWith(bookOwnerId);
                    ch.setLastMessage(formatter.format(date));
                    dbChatHistory.child(bookRequesterID).child(bookOwnerId).setValue(ch);


                    //ch = new ChatHistory();
                    ch.setChatWith(bookRequesterID);
                    ch.setLastMessage(formatter.format(date));
                    dbChatHistory.child(bookOwnerId).child(bookRequesterID).setValue(ch);


                    messageArea.setText("");
                }
            }
        });

    }



    private void getUserProfile(){
        database.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                userDisplayName = user.getName();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRequesterProfile(){
        database.child("users").child(bookRequesterID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                requesterDisplayName = user.getName();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void getAllChats() {
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, ChatViewHolder>
                (Message.class, R.layout.cardview_chat, ChatViewHolder.class, userChats) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Message msg, final int position) {
                String message =msg.getMessage().toString();
                String time = msg.getTime().toString();
                String who = msg.getWho().toString();

                if(who.equals("1")){
                    viewHolder.setOwner();
                    viewHolder.setMessage(message);
                }else {
                    viewHolder.setMessage(message);
                }

                viewHolder.setTime(time);
            }
        };


       mChatList.setAdapter(firebaseRecyclerAdapter);
    }



    public void showAllChats(){
        /*
            I assume that there is no previous chat between this(requester) & that(owner)
            so, this chat is going to be the first chat

            (A)userChats = database.child("currentUserId").child("bookOwnerId");
            , but before this happens, lets check if there is any previous chat or not?
            so at first check this

            (B)userChats = database.child("bookOwnerId").child("currentUserId");
            if above doesnt, exist, change the database to the (A) and start chat.
         */

        //(B)
        userChats = dbChats.child(bookOwnerId).child(bookRequesterID);
        userChats.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    // so there is already one chat between these
                    //Toast.makeText(Chat.this, "NO SNAPSHOT\nowner->id", Toast.LENGTH_SHORT).show();
                    getAllChats();
                } else {
                    //there isnt any chat, so lets start
                    userChats = dbChats.child(bookRequesterID).child(bookOwnerId);
                    getAllChats();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mChatList = (RecyclerView) findViewById(R.id.chatRecycleView);
        mChatList.hasFixedSize();
        mChatList.setLayoutManager(new LinearLayoutManager(this));
    }



    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;

        }


        public void setOwner(){
            mView.findViewById(R.id.chat_cardview).setBackgroundColor(Color.GRAY);
        }

        public void setMessage(String title) {
            TextView nameTxt = (TextView) mView.findViewById(R.id.cvs_message);
            nameTxt.setText(title);
        }

        public void setTime(String time) {
            TextView propTxt = (TextView) mView.findViewById(R.id.cvs_time);
            propTxt.setText(time);

        }
    }
}