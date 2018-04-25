package com.example.sergio.madlab;

import android.content.Intent;
import android.media.Image;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;

public class ViewProfile extends AppCompatActivity {

    private String name;
    private String mail;
    private String bio;
    //private ImageView image;




/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_view_profile);
        setContentView(R.layout.test);
    }
*/


    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("https://madlab34-b64ac.firebaseio.com");


    myRef.setValue("Hello, World!");





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

        /*
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("https://madlab34-b64ac.firebaseio.com/my/data");
        */

        /*
        FileInputStream serviceAccount =
                new FileInputStream("path/to/serviceAccountKey.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://madlab34-b64ac.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
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


}
