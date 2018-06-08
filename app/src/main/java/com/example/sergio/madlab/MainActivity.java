package com.example.sergio.madlab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.example.sergio.madlab.Classes.User;

import java.io.File;
import java.io.IOException;
import com.example.sergio.madlab.Classes.*;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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
    private RecyclerView mBookList;

    private User user;

    private SharedPreferences profile;
    private SharedPreferences.Editor editor;


    //
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userID;
    private DatabaseReference database, userDB, booksDB;
    //private StorageReference storageRef;

    FirebaseRecyclerAdapter<Book, BookViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set floating button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "search...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this, SearchBook.class);
                startActivity(intent);
            }
        });


        //set drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //set navigation
        //View header = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);
        //tvNHName = header.findViewById(R.id.nav_header_title);
        //tvNHMail = header.findViewById(R.id.nav_header_mail);
        //tvNHName.setText("");
        //tvNHMail.setText("");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        progressDialog = new ProgressDialog(this);
        //once the activity is Created or Restarted, it will show all books


        // SharedPreferences
        profile = this.getSharedPreferences("profile", MODE_PRIVATE);


        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();
        database = FirebaseDatabase.getInstance().getReference();
        userDB = database.child("users");

        //if the user token is not in the local storage
        if(firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }


        //tvNHName = (TextView) findViewById(R.id.nav_header_title) ;
        //tvNHMail = (TextView)findViewById(R.id.nav_header_mail);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        showAllBooks();
        getUserProfile();

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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_insert_book) {
            Intent intent = new Intent(this, InsertBook.class);
            startActivity(intent);
        } else if (id == R.id.nav_view_profile) {
            Intent intent = new Intent(this, ViewProfile.class);
            startActivity(intent);
        }else if (id == R.id.nav_edit_profile) {
            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        } else if (id == R.id.nav_chat) {
            Intent intent = new Intent(this, AllChats.class);
            intent.putExtra("userDisplayName",  user.getName());
            startActivity(intent);
        } else if (id == R.id.nav_sign_out) {
            firebaseAuth.getInstance().signOut();
            finish();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        } else if(id == R.id.nav_about){
            Intent intent = new Intent(this, AboutUs.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void showAllBooks(){
        //shows all books
        database = FirebaseDatabase.getInstance().getReference();
        booksDB = database.child("books");
        booksDB.keepSynced(true);

        mBookList = (RecyclerView) findViewById(R.id.myrecycleview);
        mBookList.hasFixedSize();
        mBookList.setLayoutManager(new LinearLayoutManager(this));

        getAllBooks();
    }


    private void getAllBooks(){
        progressDialog.setTitle(R.string.fetching_books);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>
                (Book.class, R.layout.cardview_book, BookViewHolder.class, booksDB) {
            @Override
            protected void populateViewHolder(BookViewHolder viewHolder, Book book,final int position) {
                if(!book.getUser().contains(userID)){
                    //first check if the book`s title fits in the text or not?
                    title = book.getTitle();
                    if (title.length() > 40) {
                        title = title.substring(0, 40) + "...";
                    }
                    //isbn = book.getIsbn();
                    viewHolder.setTitle(title);
                    viewHolder.setGenre(book.getGenre());
                    viewHolder.setImage(book.getIsbn());

                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //firebaseRecyclerAdapter.getRef(position).removeValue();
                            String keyISBN = firebaseRecyclerAdapter.getRef(position).getKey();
                            //Toast.makeText(getApplicationContext(),keyISBN,Toast.LENGTH_LONG).show();
                            //Intent intent = new Intent(getBaseContext(), ViewBook.class);
                            Intent intent = new Intent(getBaseContext(), ViewBook.class);
                            intent.putExtra("keyISBN", keyISBN);
                            intent.putExtra("userDisplayName", tvNHName.getText().toString());
                            startActivity(intent);
                        }
                    });
                    viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            //Toast.makeText(getApplicationContext(),isbn,Toast.LENGTH_LONG).show();
                            return false;
                        }
                    });

                    progressDialog.dismiss();
                }else {
                    viewHolder.hideView();
                }
            }
        };


        mBookList.setAdapter(firebaseRecyclerAdapter);


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

        public void setTitle(String title){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cv_bookTitle);
            nameTxt.setText(title);
        }

        public void setGenre(String Genre){
            TextView GenreTxt = (TextView)mView.findViewById(R.id.cv_bookGenre);
            GenreTxt.setText(Genre);
        }

        public void setImage(String keyISBN){
            final ImageView bookThumb = (ImageView)mView.findViewById(R.id.cv_bookImage);

            String bookNameWithISBN = keyISBN + ".jpg";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/books/" + bookNameWithISBN);
            try {
                final File localFile = File.createTempFile("Image", "jpg");
                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        bookThumb.setBackgroundDrawable(null);
                        bookThumb.setImageBitmap(bmp);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Toast.makeText(ViewBook.this, "No book image found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                       // Toast.makeText(ViewBook.this, "Grabbing book photo\nplease wait...", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void viewProfile(View view){
        Intent intent = new Intent(this, ViewProfile.class);
        startActivity(intent);
    }

}