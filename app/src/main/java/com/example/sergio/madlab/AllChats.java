package com.example.sergio.madlab;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.sergio.madlab.Classes.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.sergio.madlab.Classes.*;


public class AllChats extends AppCompatActivity {

    private String chatWith;
    private String bookOwnerName;
    private User user;
    private String userDisplayName;


    private RecyclerView mChatList;
    private FirebaseRecyclerAdapter<ChatHistory, ChatViewHolder> firebaseRecyclerAdapter;

    private String currentUserId;
    private DatabaseReference dbAllChats, allUserDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_chats);

        //Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_allChats);
        setSupportActionBar(toolbar);

        //comes from MainActivity
        userDisplayName = getIntent().getStringExtra("userDisplayName");


        //manage users - currentUser & bookOwnerId
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        allUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        showAllChats();
    }


    private void getAllChats() {
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatHistory, ChatViewHolder>
                (ChatHistory.class, R.layout.cardview_all_chats, ChatViewHolder.class, dbAllChats) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, ChatHistory history, final int position) {
                // get the name of the bookOwner = chatWith
                chatWith = history.getChatWith();
                allUserDatabase.child(chatWith).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(User.class);
                        bookOwnerName = user.getName();
                        viewHolder.setChatWith(bookOwnerName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                viewHolder.setLastTime("Last Message: " + history.getLastMessage());


                //onClick opens the chat + sends some data
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String bookOwnerId = firebaseRecyclerAdapter.getRef(position).getKey();
                        Intent intent = new Intent(getBaseContext(), Chat.class);
                        intent.putExtra("chatWith", bookOwnerId);
                        intent.putExtra("bookOwnerName", bookOwnerName);
                        intent.putExtra("userDisplayName", userDisplayName);
                        startActivity(intent);
                    }
                });
            }
        };


       mChatList.setAdapter(firebaseRecyclerAdapter);
    }



    public void showAllChats(){
        //only need this part of the database
        dbAllChats = FirebaseDatabase.getInstance().getReference().child("chat_history").child(currentUserId);
        dbAllChats.keepSynced(true);

        mChatList = (RecyclerView) findViewById(R.id.allChatsRecycleView);
        mChatList.hasFixedSize();
        mChatList.setLayoutManager(new LinearLayoutManager(this));

        getAllChats();
    }



    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setChatWith(String title) {
            TextView nameTxt = (TextView) mView.findViewById(R.id.cac_user);
            nameTxt.setText(title);
        }

        public void setLastTime(String time) {
            TextView propTxt = (TextView) mView.findViewById(R.id.cac_time);
            propTxt.setText(time);
        }
    }
}