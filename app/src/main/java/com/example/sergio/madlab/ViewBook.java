package com.example.sergio.madlab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class ViewBook extends AppCompatActivity {

    //Toolbar
    Toolbar toolbar;


    String keyISBN;
    String userDisplayName;

    private ImageView profileImage;
    private ProgressDialog progressDialog;

    //User class
    private User user;
    private Book book;

    private String isbn = "";
    private String title = "";
    private String author = "";
    private String publisher = "";
    private String editYear = "";
    private String genre = "";
    private String tags = "";
    private String condition = "";


    private Bitmap bitmap;
    private ImageView bookImage;

    private TextView tISBN;
    private TextView tTitle;
    private TextView tAuthor;
    private TextView tPublisher;
    private TextView tEditYear;
    private TextView tGenre;
    private TextView tTags;
    private TextView tCondition;


    //Firebase
    private String userEmail;
    private String userID;
    private FirebaseUser authUser;
    private StorageReference storageRef;
    private DatabaseReference database;
    private DatabaseReference booksDB;
    private DatabaseReference userDB;


    Uri uri;
    File filename;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_book);





        //get the ISBN from MainActivity
        keyISBN = getIntent().getStringExtra("keyISBN");
        userDisplayName = getIntent().getStringExtra("userDisplayName");
        //Toast.makeText(getApplicationContext(),keyISBN,Toast.LENGTH_LONG).show();



        //Firebase
        database = FirebaseDatabase.getInstance().getReference();
        userDB = database.child("users");
        booksDB = database.child("books");

        String bookNameWithISBN = keyISBN + ".jpg";
        storageRef = FirebaseStorage.getInstance().getReference().child("images/books/" + bookNameWithISBN);

        authUser = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = authUser.getEmail().replace(",", ",,").replace(".", ",");


        //prepare Text Views
        setViews();

        getBookData();
        downloadBookImage();

    }


    //get book data from fb
    private void getBookData() {
        booksDB.child(keyISBN).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                book = dataSnapshot.getValue(Book.class);
                setTexts();
                prepareChatButton();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewBook.this, "ERROR fetching book data.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_view_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_edit:
                //Edit user profile
                //Intent edit = new Intent(ViewBook.this, EditProfile.class);
                //startActivity(edit);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //Get all the text views
    private void setViews() {
        tISBN = (TextView) findViewById(R.id.textISBN);
        tTitle = (TextView) findViewById(R.id.textTitle);
        tAuthor = (TextView) findViewById(R.id.textAuthor);
        tPublisher = (TextView) findViewById(R.id.textPublisher);
        tEditYear = (TextView) findViewById(R.id.textEditYear);
        tGenre = (TextView) findViewById(R.id.textGenre);
        tTags = (TextView) findViewById(R.id.textTags);
        tCondition = (TextView) findViewById(R.id.textCondition);

        bookImage = (ImageView) findViewById(R.id.ivBook);
    }

    //Set all the texts
    private void setTexts() {
        tISBN.setText("ISBN: " + book.getIsbn());
        tTitle.setText(book.getTitle());
        tAuthor.setText(book.getAuthor());
        tPublisher.setText(book.getPublisher());
        tEditYear.setText(book.getEdityear());
        tGenre.setText(book.getGenre());
        tTags.setText(book.getTags());
        tCondition.setText(book.getCondition());
        //Toast.makeText(this, book.getUser().toString(), Toast.LENGTH_SHORT).show();
    }

    /*
    private void getUserReference(){
        ref.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                setTexts();
                setTitle(user.getName());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    */


    public void downloadBookImage() {
        try {
            final File localFile = File.createTempFile("Image", "jpg");

            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    bookImage.setImageBitmap(bmp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                   // Toast.makeText(ViewBook.this, "No book image found", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ViewBook.this, "Grabbing book photo\nplease wait...", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void prepareChatButton(){
        //set floating button
        final String bookOwnerId = book.getUser();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_startChat);

        //first check if the Owner and Current user are the same?
        if (bookOwnerId .equals(authUser.getUid())) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ViewBook.this, Chat.class);
                    intent.putExtra("chatWith", bookOwnerId);
                    intent.putExtra("userDisplayName", userDisplayName);
                    startActivity(intent);
                }
            });
        }
    }

}