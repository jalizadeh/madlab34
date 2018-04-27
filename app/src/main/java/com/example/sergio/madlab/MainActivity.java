package com.example.sergio.madlab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //retrieved data from database
    private String dbName="";
    private String dbEmail="";
    private String dbBio="";

    //nav header
    private TextView tvNHName;
    private TextView tvNHMail;

    private SharedPreferences profile;
    private SharedPreferences.Editor editor;

    DatabaseReference db;
    private RecyclerView mBookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "search...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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

        if (id == R.id.nav_send) {
            //
        } else if (id == R.id.nav_insert_book) {
            Intent intent = new Intent(this, InsertBook.class);
            startActivity(intent);
        } else if (id == R.id.nav_edit_profile) {
            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Book, ShowAllBooks.BookViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, ShowAllBooks.BookViewHolder>
                (Book.class, R.layout.book_cardview, ShowAllBooks.BookViewHolder.class, db) {
            @Override
            protected void populateViewHolder(ShowAllBooks.BookViewHolder viewHolder, Book book, int position) {
                viewHolder.setTitle(book.getTitle());
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


    public void showAllBooks(){
        //shows all books
        db = FirebaseDatabase.getInstance().getReference().child("Books");
        db.keepSynced(true);

        mBookList = (RecyclerView) findViewById(R.id.myrecycleview);
        mBookList.hasFixedSize();
        mBookList.setLayoutManager(new LinearLayoutManager(this));
        //----
    }


    public void getUserProfile(){
        //read from database -> Users
        db = FirebaseDatabase.getInstance().getReference().child("Users");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    dbName = (String) messageSnapshot.child("name").getValue();
                    dbEmail = (String) messageSnapshot.child("email").getValue();
                    dbBio = (String) messageSnapshot.child("bio").getValue();
                }
                //Toast.makeText(getApplicationContext(),dbName+'-'+dbEmail+'-'+dbBio,Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"db fetched successfully",Toast.LENGTH_SHORT).show();


                //nav header
                tvNHName = findViewById(R.id.nav_header_title);
                tvNHMail = findViewById(R.id.nav_header_mail);

                tvNHName.setText(dbName);
                tvNHMail.setText(dbEmail);
                //textView_bio.setText(dbBio);

                //save the last data if any change happens
                editor = profile.edit();
                editor.putString("name", dbName);
                editor.putString("mail", dbEmail);
                editor.putString("bio", dbBio);
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"db fetch failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Read and save each book data and create a separate view for it
    // prepare for CardView
    public static  class BookViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public BookViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cv_bookTitle);
            nameTxt.setText(title);
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
}
