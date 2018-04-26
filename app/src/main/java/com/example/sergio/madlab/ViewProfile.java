package com.example.sergio.madlab;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private TextView textView_name;
    private TextView textView_mail;
    private TextView textView_bio;


    private SharedPreferences profile;

    private DatabaseReference mDatabase;
    private static final String TAG = "DatabaseFact";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_profile);
        textView_name = findViewById(R.id.name_text);
        textView_mail = findViewById(R.id.mail_text);
        textView_bio = findViewById(R.id.bio_text);

    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("https://madlab34-b64ac.firebaseio.com");


    myRef.setValue("Hello, World!");

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            textView_name.setText(intent.getStringExtra("name"));
            textView_mail.setText(intent.getStringExtra("mail"));
            textView_bio.setText(intent.getStringExtra("bio"));
        } else {
            profile = this.getSharedPreferences("profile", MODE_PRIVATE);
            textView_name.setText(profile.getString("name", textView_name.getText().toString()));
            textView_mail.setText(profile.getString("mail", textView_mail.getText().toString()));
            textView_bio.setText(profile.getString("bio", textView_bio.getText().toString()));
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("name", textView_name.getText().toString());
        outState.putString("mail", textView_mail.getText().toString());
        outState.putString("bio", textView_bio.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView textView_name = findViewById(R.id.name_text);
        textView_name.setText(savedInstanceState.getString("name"));
        TextView textView_mail = findViewById(R.id.mail_text);
        textView_mail.setText(savedInstanceState.getString("mail"));
        TextView textView_bio = findViewById(R.id.bio_text);
        textView_bio.setText(savedInstanceState.getString("bio"));

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

        textView_bio.setText(savedInstanceState.getString("bio"));

    }


    @Override
    protected void onPause() {
        super.onPause();

        profile = this.getSharedPreferences("profile", MODE_PRIVATE);
        SharedPreferences.Editor editor = profile.edit();
        editor.putString("name", textView_name.getText().toString());
        editor.putString("mail", textView_mail.getText().toString());
        editor.putString("bio", textView_bio.getText().toString());
        editor.commit();
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
