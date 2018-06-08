package com.example.sergio.madlab;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sergio.madlab.Classes.Book;
import com.example.sergio.madlab.Classes.BookRequest;
import com.example.sergio.madlab.Classes.Notification;
import com.example.sergio.madlab.Classes.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class AllRequests extends AppCompatActivity {

    //retrieved data from database
    private String dbName="";
    private String dbEmail="";
    private String dbBio="";

    //nav header
    private TextView tvNHName;
    private TextView tvNHMail;

    private String isbn="";
    private String title="";
    private String author="";
    private String publisher="";
    private String editYear= "";
    private String genre="";
    private String tags="";
    private String condition="";


    private ProgressDialog progressDialog;

    //
    ImageView bookThumbnail;
    private RecyclerView mRequestList;

    private User user;

    private SharedPreferences profile;
    private SharedPreferences.Editor editor;


    private BookRequest bookRequestObject;
    private Notification notif;

    //
    private String userID;
    private String bookOwnerId, bookOwnerName;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database, userDB, booksDB;
    private DatabaseReference notificationsDB;
    private DatabaseReference requestsDB;

    //private StorageReference storageRef;

    FirebaseRecyclerAdapter<BookRequest, BookViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_requests);

        //Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_allRequests);
        setSupportActionBar(toolbar);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        database = FirebaseDatabase.getInstance().getReference();
        userDB = database.child("users");
        notificationsDB = database.child("notifications");
//        requestsDB = database.child("requests");

        bookRequestObject = new BookRequest();
        notif = new Notification();

        showAllRequests();
        //getUserProfile();



    }



    private void getUserProfile(){
        String uID = firebaseAuth.getUid();
        userDB.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                tvNHName = (TextView) findViewById(R.id.nav_header_title);
                tvNHMail = (TextView) findViewById(R.id.nav_header_mail);


                tvNHName.setText(user.getName());
                tvNHMail.setText(user.getEmail());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void showAllRequests(){
        //shows all books
        requestsDB = database.child("requests").child(userID);
        requestsDB.keepSynced(true);

        mRequestList = (RecyclerView) findViewById(R.id.allRequestsRecycleview);
        mRequestList.hasFixedSize();
        mRequestList.setLayoutManager(new LinearLayoutManager(this));

        getAllRequests();
    }


    private void getAllRequests(){
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<BookRequest, BookViewHolder>
                (BookRequest.class, R.layout.cardview_all_requests, BookViewHolder.class, requestsDB) {
            @Override
            protected void populateViewHolder(BookViewHolder viewHolder, final BookRequest bookRequest, final int position) {

                viewHolder.setTitle(bookRequest.getBookName());

                //if I am the requester
                if(bookRequest.getRequesterID().contains(userID)){
                    //1. i have to wait for status
                    if (bookRequest.getStatus().contains("pending")){
                        viewHolder.setStatus(bookRequest.getStatus().toUpperCase()+"\nWait for response from other user");
                        viewHolder.setPending();
                    } else if (bookRequest.getStatus().contains("active")){
                        viewHolder.setStatus(bookRequest.getStatus().toUpperCase() + "\nTap to chat");
                        viewHolder.setActive();

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(AllRequests.this, Chat.class);
                                intent.putExtra("bookOwnerId", bookRequest.getBookOwnerID());
                                intent.putExtra("bookOwnerName", bookOwnerName);
                                intent.putExtra("userDisplayName", "USER AF");
                                intent.putExtra("bookISBN", isbn);
                                intent.putExtra("bookTitle", title);
                                startActivity(intent);
                            }
                        });
                    }else if (bookRequest.getStatus().contains("reject")){
                        viewHolder.setStatus(bookRequest.getStatus().toUpperCase()+"ED\nYour request is rejected");
                        viewHolder.setReject();
                    }
                } else if(bookRequest.getBookOwnerID().contains(userID)){
                    //if i am the owner of the book
                    //1. i have to wait for status
                    if (bookRequest.getStatus().contains("pending")){
                        viewHolder.setStatus(bookRequest.getStatus().toUpperCase()+"\nTap to decide");
                        viewHolder.setPending();

                        bookRequestObject.setBookOwnerID(bookRequest.getBookOwnerID());
                        bookRequestObject.setBookISBN(bookRequest.getBookISBN());
                        bookRequestObject.setBookName(bookRequest.getBookName());
                        bookRequestObject.setBookOwnerID(bookRequest.getBookOwnerID());
                        bookRequestObject.setRequesterID(bookRequest.getRequesterID());
                        bookRequestObject.setStatus("active");

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                askToDecide(bookRequest.getRequesterID(), bookRequestObject);
                            }
                        });

                    } else if (bookRequest.getStatus().contains("active")){
                        viewHolder.setStatus(bookRequest.getStatus().toUpperCase()+"\nTap to chat");
                        viewHolder.setActive();

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(AllRequests.this, Chat.class);
                                intent.putExtra("bookOwnerId", bookRequest.getBookOwnerID());
                                intent.putExtra("bookOwnerName", bookOwnerName);
                                intent.putExtra("userDisplayName", "USER AF");
                                intent.putExtra("bookISBN", isbn);
                                intent.putExtra("bookTitle", title);
                                startActivity(intent);
                            }
                        });
                    }else if (bookRequest.getStatus().contains("reject")){
                        viewHolder.setStatus(bookRequest.getStatus().toUpperCase()+"ED\nYou rejected the request");
                        viewHolder.setReject();
                    }
                }
            }
        };


        mRequestList.setAdapter(firebaseRecyclerAdapter);


    }

    public void askToDecide(final String bookRequesterID, final BookRequest bookRequestObject){
        new AlertDialog.Builder(this)
                .setTitle(R.string.request_book)
                .setMessage("Do you accept to lend the book?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        //set a notification
                        //and send to the requester
                        notif.setUserID(bookRequesterID);
                        notif.setIsRead("0");
                        notif.setType("book_request");
                        notificationsDB.child(bookRequesterID).push().setValue(notif);

                        requestsDB.child(bookRequestObject.getBookISBN()).setValue(bookRequestObject);
                        database.child("requests").child(bookRequesterID).child(bookRequestObject.getBookISBN()).setValue(bookRequestObject);

                    }})
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set a notification
                        //and send to the requester
                        notif.setUserID(bookRequesterID);
                        notif.setIsRead("0");
                        notif.setType("book_request");
                        notificationsDB.child(bookRequesterID).push().setValue(notif);

                        bookRequestObject.setStatus("reject");
                        requestsDB.child(bookRequestObject.getBookISBN()).setValue(bookRequestObject);
                        database.child("requests").child(bookRequesterID).child(bookRequestObject.getBookISBN()).setValue(bookRequestObject);
                    }
                }).setCancelable(true).show();
    }


    //Read and save each book data and create a separate view for it
    // prepare for CardView
    public static  class BookViewHolder extends RecyclerView.ViewHolder{
        View mView;
        //Item currentItem;

        public BookViewHolder(final View itemView){
            super(itemView);
            mView = itemView;
        }


        public void hideView(){
            mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            mView.setVisibility(View.GONE);
        }

        public void setPending(){
            mView.findViewById(R.id.btnStatus).setBackgroundColor(Color.YELLOW);
        }
        public void setActive(){
            mView.findViewById(R.id.btnStatus).setBackgroundColor(Color.GREEN);
        }
        public void setReject(){
            mView.findViewById(R.id.btnStatus).setBackgroundColor(Color.RED);
        }





        public void setTitle(String title){
            TextView nameTxt = (TextView)mView.findViewById(R.id.car_bookTitle);
            nameTxt.setText(title);
        }

        public void setStatus(String status){
            TextView statusTxt = (TextView)mView.findViewById(R.id.car_bookStatus);
            statusTxt.setText(status);
        }

    }

}