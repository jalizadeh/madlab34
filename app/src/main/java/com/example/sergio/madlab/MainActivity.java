package com.example.sergio.madlab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.example.sergio.madlab.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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


    //
    ImageView bookThumbnail;
    private RecyclerView mBookList;

    private String userEmail;
    private User user;

    private SharedPreferences profile;
    private SharedPreferences.Editor editor;


    //
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
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
        //navigationView.setNavigationItemSelectedListener(this);

//        tvNHName.setText("");
//        tvNHMail.setText("");
        //once the activity is Created or Restarted, it will show all books
        showAllBooks();

        // SharedPreferences
        profile = this.getSharedPreferences("profile", MODE_PRIVATE);

        /*
        // load user`s data from local database
        // may change in future
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            tvNHName.setText(intent.getStringExtra("name"));
            tvNHMail.setText(intent.getStringExtra("mail"));
            //textView_bio.setText(intent.getStringExtra("bio"));
        } else {
            //tvNHName.setText(profile.getString("name", tvNHName.getText().toString()));
            //tvNHMail.setText(profile.getString("mail", tvNHMail.getText().toString()));
            //textView_bio.setText(profile.getString("bio", textView_bio.getText().toString()));
            tvNHName.setText(profile.getString("name", ""));
            tvNHMail.setText(profile.getString("mail", ""));
            tvNHMail.setText(profile.getString("bio", ""));
            //textView_bio.setText(profile.getString("bio", textView_bio.getText().toString()));
        }
        */



        firebaseAuth = FirebaseAuth.getInstance();
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
        userEmail = firebaseUser.getEmail().replace(",",",,").replace(".", ",");


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            //
        } else if (id == R.id.nav_insert_book) {
            Intent intent = new Intent(this, InsertBook.class);
            startActivity(intent);
        } else if (id == R.id.nav_edit_profile) {
            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        } else if (id == R.id.nav_chat) {
            Intent intent = new Intent(this, AllChats.class);
            startActivity(intent);
        } else if (id == R.id.nav_sign_out) {
            firebaseAuth.getInstance().signOut();
            finish();
            Intent intent = new Intent(this, Login.class);
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
        //----
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



        public void setTitle(String title){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cv_bookTitle);
            nameTxt.setText(title);
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

        /*
        public void setAtuthor(String author){
            TextView propTxt= (TextView) mView.findViewById(R.id.propellantTxt);
            propTxt.setText(author);
        }

        public void setGenre(String genre){
            TextView descTxt= (TextView) mView.findViewById(R.id.descTxt);
            descTxt.setText(genre);
        }
        */
    }



    public void viewProfile(View view){
        Intent intent = new Intent(this, ViewProfile.class);
        startActivity(intent);
    }



    @Override
    protected void onStart() {
        super.onStart();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>
                (Book.class, R.layout.cardview_book, BookViewHolder.class, booksDB) {
            @Override
            protected void populateViewHolder(BookViewHolder viewHolder, Book book,final int position) {
                title = book.getTitle();
                isbn = book.getIsbn();
                viewHolder.setTitle(title);
                viewHolder.setImage(isbn);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //firebaseRecyclerAdapter.getRef(position).removeValue();
                        String keyISBN = firebaseRecyclerAdapter.getRef(position).getKey();
                        //Toast.makeText(getApplicationContext(),keyISBN,Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getBaseContext(), ViewBook.class);
                        intent.putExtra("keyISBN", keyISBN);
                        intent.putExtra("userDisplayName", tvNHName.getText().toString());
                        startActivity(intent);
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(getApplicationContext(),isbn,Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
                //viewHolder.setAtuthor(book.getAuthor());
                //viewHolder.setGenre(book.getGenre());
            }
        };


        mBookList.setAdapter(firebaseRecyclerAdapter);

        getUserProfile();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        showAllBooks();
        //getUserProfile();
    }



}



/*
private void updateUI(FirebaseUser user) {
        if (user != null) {
            ((TextView) findViewById(R.id.text_sign_in_status)).setText(
                    "User ID: " + user.getUid());
        } else {
            ((TextView) findViewById(R.id.text_sign_in_status)).setText(
                    "Error: sign in failed.");
        }
}
 */