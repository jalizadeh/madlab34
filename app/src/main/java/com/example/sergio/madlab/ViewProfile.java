package com.example.sergio.madlab;

import android.content.Intent;
import android.media.Image;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;

public class ViewProfile extends AppCompatActivity {

    private String name;
    private String mail;
    private String bio;
    //private ImageView image;

    private DatabaseReference mDatabase;
    private static final String TAG = "DatabaseFact";



/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_view_profile);
        setContentView(R.layout.test);
    }
*/





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        //setContentView(R.layout.test);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        mail = intent.getStringExtra("mail");
        bio = intent.getStringExtra("bio");

        TextView textView_name = findViewById(R.id.name_text);
        textView_name.setText(name);
        TextView textView_mail = findViewById(R.id.mail_text);
        textView_mail.setText(mail);
        TextView textView_bio = findViewById(R.id.bio_text);
        textView_bio.setText(bio);

        Button btn_ib = findViewById(R.id.btn_ib);


/*
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //mDatabase.setValue("Hello, World!");

        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        */
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("name", name);
        outState.putString("mail", mail);
        outState.putString("bio", bio);
        super.onSaveInstanceState(outState);
        //TODO does not work
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void editProfile(MenuItem menuItem) {
        Intent intent = new Intent(this, EditProfile.class);
        startActivity(intent);
    }

    public void openInsertBook(View view)
    {
        Intent intent = new Intent(this, InsertBook.class);
        startActivity(intent);
    }
}
