package com.example.sergio.madlab;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.sergio.madlab.Msg;


public class Chat extends AppCompatActivity {

    private LinearLayout layout;
    private RelativeLayout layout_2;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;


    private Msg msg;
    private List<Msg> allMsgs;


    private RecyclerView mChatList;
    private FirebaseRecyclerAdapter<Msg, Chat.ChatViewHolder> firebaseRecyclerAdapter;

    //
    private FirebaseUser firebaseUser;
    private DatabaseReference userChats, database, reference1, reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //layout = (LinearLayout) findViewById(R.id.layout1);
        //layout_2 = (RelativeLayout) findViewById(R.id.layout2);
        sendButton = (ImageView) findViewById(R.id.sendButton);
        messageArea = (EditText) findViewById(R.id.messageArea);
        //scrollView = (ScrollView) findViewById(R.id.scrollView);

        allMsgs = new ArrayList<>();

        //Firebase.setAndroidContext(this);
        //firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //userEmail = firebaseUser.getEmail().replace(",",",,").replace(".", ",");

        database = FirebaseDatabase.getInstance().getReference();
        //userChats = database.child("chats").child(firebaseUser.getUid().toString()).child("tmXdMfEDmqa3QFcNUmLJpzqYikL2");
//        userChats = database.child("chats").child("KftIHK7gVNTOihgEyUHNJEQZOQj1").child("tmXdMfEDmqa3QFcNUmLJpzqYikL2");

       // reference1 = new Firebase("https://androidchatapp-76776.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        //reference2 = new Firebase("https://androidchatapp-76776.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);


        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date();
        //System.out.println();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if (!messageText.equals("")) {
                    Msg msg = new Msg();
                    msg.setTime(formatter.format(date));
                    msg.setMessage(messageText);
                    msg.setWho("0");
                    //msg.setMessage(messageText);
                    //reference1.push().setValue(map);
                    //reference2.push().setValue(map);
                    //userChats.setValue(formatter.format(date)+"|"+messageText);
                    userChats.push().setValue(msg);
                    messageArea.setText("");
                }
            }
        });

        /*
        userChats.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot :dataSnapshot.getChildren())
                {
                    //msg = snapshot.getValue(Msg.class);
                    //allMsgs.add(msg);
                    String key= snapshot.getKey();
                    String value=snapshot.getValue().toString();
                    Toast.makeText(Chat.this,snapshot.child("message").getValue().toString(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Chat.this,databaseError.toString(),Toast.LENGTH_SHORT).show();
            }
        });
*/

        /*
        userChats.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                msg = dataSnapshot.getValue(Msg.class);
                allMsgs.add(msg);
                Toast.makeText(Chat.this, msg.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(Chat.this, msg.getTime(), Toast.LENGTH_LONG).show();
                Toast.makeText(Chat.this, msg.getWho(), Toast.LENGTH_SHORT).show();

                for(Msg m : allMsgs){
                    addMessageBox(m.getMessage(), m.getWho());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

*/



        showAllBooks();


    }


    private void getAllChats() {

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Msg, ChatViewHolder>
                (Msg.class, R.layout.chat_cardview, ChatViewHolder.class, userChats) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Msg msg, final int position) {
                String message = msg.getMessage();
                String time = msg.getTime();
                viewHolder.setMessage(message);
                viewHolder.setTime(time);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //firebaseRecyclerAdapter.getRef(position).removeValue();
                        String keyISBN = firebaseRecyclerAdapter.getRef(position).getKey();
                        //Toast.makeText(getApplicationContext(),keyISBN,Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getBaseContext(), ViewBook.class);
                        intent.putExtra("keyISBN", keyISBN);
                        startActivity(intent);
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(getApplicationContext(), "long click", Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
                //viewHolder.setAtuthor(book.getAuthor());
                //viewHolder.setGenre(book.getGenre());
            }

        };

        mChatList.setAdapter(firebaseRecyclerAdapter);

    }



    public void showAllBooks(){
        //shows all books
        userChats = database.child("chats").child("KftIHK7gVNTOihgEyUHNJEQZOQj1").child("tmXdMfEDmqa3QFcNUmLJpzqYikL2");
        userChats.keepSynced(true);

        mChatList = (RecyclerView) findViewById(R.id.chatRecycleView);
        mChatList.hasFixedSize();
        mChatList.setLayoutManager(new LinearLayoutManager(this));
        //----

//        getAllChats();
    }





    //Read and save each book data and create a separate view for it
    // prepare for CardView
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View mView;
        //Item currentItem;

        public ChatViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;

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



    /*
    public void addMessageBox(String message, String who){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(who.equals("1")) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
*/



}