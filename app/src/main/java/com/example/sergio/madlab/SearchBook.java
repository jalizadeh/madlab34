package com.example.sergio.madlab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class SearchBook extends AppCompatActivity {

    //retrieved data from database
    private String dbName="";
    private String dbEmail="";
    private String dbBio="";

    //nav header
    private Spinner spSearchFactor;
    private EditText etSearchValue;
    private Button btnDoSearch;

    private String isbn="";
    private String title="";
    private String author="";
    private String publisher="";
    private String editYear= "";
    private String genre="";
    private String tags="";
    private String condition="";


    private String searchFactor;
    private String searchValue;

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
        setContentView(R.layout.activity_search_book);

        //Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_searchBook);
        setSupportActionBar(toolbar);




        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        //booksDB = database.child("books");
        userDB = database.child("users");


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = firebaseUser.getEmail().replace(",",",,").replace(".", ",");



        //setViews();
        spSearchFactor = (Spinner) findViewById(R.id.sp_searchFactor);
        etSearchValue = (EditText) findViewById(R.id.searchBar);
        btnDoSearch = (Button) findViewById(R.id.btnSearchBook);




    }




    public void findBooks(){
        //shows all books
        //database = FirebaseDatabase.getInstance().getReference();
        booksDB = database.child("books");
        booksDB.keepSynced(true);

        mBookList = (RecyclerView) findViewById(R.id.searchrecycleview);
        mBookList.hasFixedSize();
        mBookList.setLayoutManager(new LinearLayoutManager(this));
        //----
    }



    //Read and save each book data and create a separate view for it
    // prepare for CardView
    public static  class BookViewHolder extends RecyclerView.ViewHolder{
        View mView;

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




    public void doSearch(View view){
        searchFactor = spSearchFactor.getSelectedItem().toString();
        searchValue = etSearchValue.getText().toString();

        findBooks();

        Query firebaseSearchQuery = booksDB.orderByChild(searchFactor)
                .startAt(searchValue)
                .endAt(searchValue + "\uf8ff");


        firebaseSearchQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>(
                        Book.class,
                        R.layout.search_cardview,
                        BookViewHolder.class,
                        //booksDB.orderByChild(searchFactor).startAt(searchValue).endAt(searchValue + "\uf8ff")) {
                        booksDB) {
                    @Override
                    protected void populateViewHolder(BookViewHolder viewHolder, Book book,final int position) {
                        title = book.getTitle();
                        isbn = book.getIsbn();
                        viewHolder.setTitle(title);
                        viewHolder.setImage(isbn);

                        Toast.makeText(getApplicationContext(),searchFactor + searchValue,Toast.LENGTH_LONG).show();

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
                                Toast.makeText(getApplicationContext(),isbn,Toast.LENGTH_LONG).show();
                                return false;
                            }
                        });
                        //viewHolder.setAtuthor(book.getAuthor());
                        //viewHolder.setGenre(book.getGenre());
                    }
                };
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        mBookList.setAdapter(firebaseRecyclerAdapter);



    }





}

