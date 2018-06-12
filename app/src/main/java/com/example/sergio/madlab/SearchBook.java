package com.example.sergio.madlab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sergio.madlab.Classes.Book;
import com.example.sergio.madlab.Classes.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



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
    private RecyclerView sBookList;

    private String userEmail;
    private User user;

    FloatingActionButton fab;



    //
    private String userID;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database, userDB, booksDB;

    //private StorageReference storageRef;

    FirebaseRecyclerAdapter<Book, BookViewHolder> firebaseRecyclerAdapter;

    private HashMap<String, MarkerOptions> markers = new HashMap<String, MarkerOptions>();
    private LatLng currentMarkerLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book);

        //Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_searchBook);
        setSupportActionBar(toolbar);


        //set floating button
        fab = (FloatingActionButton) findViewById(R.id.showMap);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchBook.this, MapsActivity.class);
                intent.putExtra("markers", markers);
                startActivity(intent);
            }
        });


        database = FirebaseDatabase.getInstance().getReference();
        userDB = database.child("users");

        //booksDB = database.child("books");
        //booksDB.keepSynced(true);



        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = firebaseUser.getUid();
        //userEmail = firebaseUser.getEmail().replace(",",",,").replace(".", ",");


        /*
        DatabaseReference bDB = database.push();
        final String refID = bDB.getKey();
        bDB.child("test").setValue(refID);
        bDB.child("test3").setValue(firebaseUser.getUid().toString());
        */

        //setViews();
        spSearchFactor = (Spinner) findViewById(R.id.sp_searchFactor);
        etSearchValue = (EditText) findViewById(R.id.searchBar);
        //btnDoSearch = (Button) findViewById(R.id.button);


        //make recView ready
        findBooks();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_book, menu);
        return super.onCreateOptionsMenu(menu);
    }




    //verify if the fields are set well
    public boolean validateValues(){
        if (spSearchFactor.getSelectedItem().toString().equals("Search by")){
            TextView errorText = (TextView) spSearchFactor.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_invalid_search_factor);//changes the selected item text to this
            errorText.requestFocus();
            return false;
        }

        if (etSearchValue.getText().toString().isEmpty()){
            etSearchValue.setError(getString(R.string.error_empty_searchvalue));
            etSearchValue.requestFocus();
            return false;
        }

        closeKeyboard();

        return true;
    }




    //it will search for the searchValue
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(validateValues()) {
            doSearch();
        }


        return super.onOptionsItemSelected(item);
    }



    public void findBooks(){
        //shows all books
        sBookList = (RecyclerView) findViewById(R.id.searchrecycleview);
        sBookList.hasFixedSize();
        sBookList.setLayoutManager(new LinearLayoutManager(this));
    }



    //Read and save each book data and create a separate view for it
    // prepare for CardView
    public static  class BookViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public BookViewHolder(final View itemView){
            super(itemView);
            mView = itemView;
        }

        public void hideView(){
            mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            mView.setVisibility(View.GONE);

        }

        public void setTitle(String title){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cvs_bookTitle);
            nameTxt.setText(title);
        }

        public void setPublisher(String publisher){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cvs_bookPublisher);
            nameTxt.setText(publisher);
        }

        public void setAuthor(String author){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cvs_bookAuthor);
            nameTxt.setText(author);
        }

        public void setGenre(String genre){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cvs_bookGenre);
            nameTxt.setText(genre);
        }

        public void setImage(String keyISBN){
            final ImageView bookThumb = (ImageView)mView.findViewById(R.id.cvs_bookImage);

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






    public void doSearch(){
        searchFactor = spSearchFactor.getSelectedItem().toString().toLowerCase();
        searchValue = etSearchValue.getText().toString();

        booksDB = database.child("books");
        booksDB.keepSynced(true);
        //my temporary solution
        //because the project is small enough, i will fetch all the data from "books"
        //then, i will compare book.getTitle() with searchValue
        //if yes, i will show it, else won`t


        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>(
                Book.class,
                R.layout.cardview_search,
                BookViewHolder.class,
                booksDB) {

            @Override
            protected void populateViewHolder(BookViewHolder viewHolder, Book book,final int position) {
                title = book.getTitle();
                author = book.getAuthor();
                publisher = book.getPublisher();

                switch (searchFactor) {
                    case "> title":
                        if(title.toLowerCase().contains(searchValue.toLowerCase())
                                && !book.getUser().contains(userID)){
                            isbn = book.getIsbn();
                            viewHolder.setTitle(title);
                            viewHolder.setAuthor(book.getAuthor());
                            viewHolder.setPublisher(book.getPublisher());
                            viewHolder.setGenre(book.getGenre());
                            viewHolder.setImage(isbn);

                            getBookPosition(isbn, title);


                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String keyISBN = firebaseRecyclerAdapter.getRef(position).getKey();
                                    Intent intent = new Intent(getBaseContext(), ViewBook.class);
                                    intent.putExtra("keyISBN", keyISBN);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            viewHolder.hideView();
                        }
                        break;

                    case "> author":
                        if(author.toLowerCase().contains(searchValue.toLowerCase())
                                && !book.getUser().contains(userID)){
                            isbn = book.getIsbn();
                            viewHolder.setTitle(title);
                            viewHolder.setAuthor(book.getAuthor());
                            viewHolder.setPublisher(book.getPublisher());
                            viewHolder.setGenre(book.getGenre());
                            viewHolder.setImage(isbn);

                            getBookPosition(isbn, title);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String keyISBN = firebaseRecyclerAdapter.getRef(position).getKey();
                                    Intent intent = new Intent(getBaseContext(), ViewBook.class);
                                    intent.putExtra("keyISBN", keyISBN);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            viewHolder.hideView();
                        }
                        break;


                    case "> publisher":
                        if(publisher.toLowerCase().contains(searchValue.toLowerCase())
                                && !book.getUser().contains(userID)){
                            isbn = book.getIsbn();
                            viewHolder.setTitle(title);
                            viewHolder.setAuthor(book.getAuthor());
                            viewHolder.setPublisher(book.getPublisher());
                            viewHolder.setGenre(book.getGenre());
                            viewHolder.setImage(isbn);

                            getBookPosition(isbn, title);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String keyISBN = firebaseRecyclerAdapter.getRef(position).getKey();
                                    Intent intent = new Intent(getBaseContext(), ViewBook.class);
                                    intent.putExtra("keyISBN", keyISBN);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            viewHolder.hideView();
                        }
                        break;
                }

            }
        };

        sBookList.setAdapter(firebaseRecyclerAdapter);
    }

    private void getBookPosition(final String bookId, final String title) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("locations/" + bookId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("locations").hasChild(bookId)) {
                    double latitude = (double) dataSnapshot.child("l").child("0").getValue();
                    double longitude = (double) dataSnapshot.child("l").child("1").getValue();
                    Toast.makeText(SearchBook.this, latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                    currentMarkerLocation = new LatLng(latitude, longitude);
                    markers.put(isbn, new MarkerOptions().position(currentMarkerLocation).title(title));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != this.getCurrentFocus())
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
    }

}
