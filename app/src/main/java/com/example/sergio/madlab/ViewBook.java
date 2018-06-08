package com.example.sergio.madlab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sergio.madlab.Classes.User;
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

import com.example.sergio.madlab.Classes.*;


public class ViewBook extends AppCompatActivity {

    //for startchat btn in menu
    private Menu menu;
    private FloatingActionButton fab;
    private Boolean showChatButton = false;

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
    private TextView tBookOwner;



    //Firebase
    private String userEmail;
    private String userID;
    private String bookOwnerId, bookOwnerName;
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


        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_viewBook);
        setSupportActionBar(mToolbar);

        fab = (FloatingActionButton) findViewById(R.id.btn_startChat);

        //get the ISBN from MainActivity
        keyISBN = getIntent().getStringExtra("keyISBN");
        userDisplayName = getIntent().getStringExtra("userDisplayName");


        //Firebase
        database = FirebaseDatabase.getInstance().getReference();
        userDB = database.child("users");
        booksDB = database.child("books");



        String bookNameWithISBN = keyISBN + ".jpg";
        storageRef = FirebaseStorage.getInstance().getReference().child("images/books/" + bookNameWithISBN);

        authUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = authUser.getUid();
        //userEmail = authUser.getEmail().replace(",", ",,").replace(".", ",");


        //prepare Text Views
        setViews();

        getBookData();
        downloadBookImage();


        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle(" ");

        //it manages showing or hiding chat button on Toolbar
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;

                    if (showChatButton)
                        showOption(R.id.action_start_chat);

                    collapsingToolbarLayout.setTitle(getString(R.string.title_activity_view_book));
                } else if (isShow) {
                    isShow = false;

                    if(showChatButton)
                        hideOption(R.id.action_start_chat);

                    //carefull there should a space between double quote otherwise it wont work
                    collapsingToolbarLayout.setTitle(" ");
                }
            }
        });

    }



    private void getBookOwner() {
        userDB.child(bookOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                bookOwnerName = user.getName();

                tBookOwner.setText(bookOwnerName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_view_book, menu);
        hideOption(R.id.action_start_chat);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start_chat) {
            if(showChatButton){
                Intent intent = new Intent(ViewBook.this, Chat.class);
                intent.putExtra("chatWith", bookOwnerId);
                intent.putExtra("userDisplayName", userDisplayName);
                intent.putExtra("bookOwnerName", bookOwnerName);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
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
        tBookOwner = (TextView) findViewById(R.id.textBookOwner);

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
        bookOwnerId = book.getUser();
        getBookOwner();

        //first check if the Owner and Current user are the same?
        if (bookOwnerId .equals(userID)) {
            fab.setVisibility(View.GONE);
            showChatButton = false;
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ViewBook.this, Chat.class);
                    intent.putExtra("chatWith", bookOwnerId);
                    intent.putExtra("userDisplayName", userDisplayName);
                    intent.putExtra("bookOwnerName", bookOwnerName);
                    startActivity(intent);
                }
            });
            showChatButton = true;
        }
    }

}