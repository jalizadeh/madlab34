package com.example.sergio.madlab;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;



public class ViewProfile extends AppCompatActivity {

    private static final String filename = "profileImage.jpeg";
    private static final String TAG = "DatabaseError";

    //Toolbar
    Toolbar toolbar;

    //TextViews
    private TextView name;
    private TextView mail;
    private TextView bio;

    //SharedPreferences
    private SharedPreferences preferences;
    private FirebaseUser authUser;
    private DatabaseReference database;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private User user;
    private String email;

    private ImageView profileImage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        database = FirebaseDatabase.getInstance().getReference();
        ref = database.child("users");
        storageRef = FirebaseStorage.getInstance().getReference();
        preferences = getSharedPreferences("Info", Context.MODE_PRIVATE);

        getTextViews();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_viewProfile);
        setSupportActionBar(toolbar);


        progressDialog = new ProgressDialog(this);


        authUser = FirebaseAuth.getInstance().getCurrentUser();
        email = authUser.getEmail().replace(",",",,").replace(".", ",");
        getUserReference();
        profileImage = (ImageView) findViewById(R.id.imageView);
        setImageView();



    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setImageView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserReference();
        setImageView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_edit:
                //Edit user profile
                Intent edit = new Intent(ViewProfile.this, EditProfile.class);
                startActivity(edit);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void setImageView() {
        downloadToLocalFile(storageRef.child("images").child(email).child("profile image"));
    }

    //Get all the text views
    private void getTextViews(){
        name = (TextView)findViewById(R.id.name_text);
        mail = (TextView)findViewById(R.id.mail_text);
        bio = (TextView)findViewById(R.id.bio_text);
    }

    //Sel all the texts
    private void setTexts(){
        name.setText(user.getUsername());
        mail.setText(user.getEmail());
        bio.setText(user.getBio());
    }

    private void getUserReference(){
        ref.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                setTexts();
                setTitle(user.getUsername());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void downloadToLocalFile(StorageReference fileRef) {
        if (fileRef != null) {
            progressDialog.setTitle(getString(R.string.downloading));
            progressDialog.setMessage(null);
            progressDialog.show();

            try {
                final File localFile = File.createTempFile("profileImage", "jpeg");

                fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        profileImage.setImageBitmap(bmp);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(ViewProfile.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        // percentage in progress dialog
                        progressDialog.setMessage(getString(R.string.downloaded) + ((int) progress) + getString(R.string.perc));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(ViewProfile.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
        }
    }
}



/*
public class ViewProfile extends AppCompatActivity {

    //retrieved data from database
    private String dbName="";
    private String dbEmail="";
    private String dbBio="";
    private String dbImage="";


    private TextView textView_name;
    private TextView textView_mail;
    private TextView textView_bio;
    private ImageView imageView;

    private SharedPreferences profile;
    private SharedPreferences.Editor editor;

    private DatabaseReference database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences
        profile = this.getSharedPreferences("profile", MODE_PRIVATE);

        setContentView(R.layout.activity_view_profile);
        textView_name = findViewById(R.id.name_text);
        textView_mail = findViewById(R.id.mail_text);
        textView_bio = findViewById(R.id.bio_text);
        imageView = findViewById(R.id.imageView);

        // load user`s data from local database
        // may change in future

            textView_name.setText(profile.getString("name", textView_name.getText().toString()));
            textView_mail.setText(profile.getString("mail", textView_mail.getText().toString()));
            textView_bio.setText(profile.getString("bio", textView_bio.getText().toString()));
        //}


        //read from database -> Users
        database = FirebaseDatabase.getInstance().getReference("Users");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    dbName = (String) messageSnapshot.child("name").getValue();
                    dbEmail = (String) messageSnapshot.child("email").getValue();
                    dbBio = (String) messageSnapshot.child("bio").getValue();
                    dbImage = (String) messageSnapshot.child("image").getValue();
                }
                //Toast.makeText(getApplicationContext(),dbName+'-'+dbEmail+'-'+dbBio,Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"db fetched successfully",Toast.LENGTH_SHORT).show();

                textView_name.setText(dbName);
                textView_mail.setText(dbEmail);
                textView_bio.setText(dbBio);

                Uri imageUri = Uri.parse(dbImage);
                imageView.setImageURI(null);
                imageView.setImageURI(imageUri);


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


    //may change in future
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", textView_name.getText().toString());
        outState.putString("mail", textView_mail.getText().toString());
        outState.putString("bio", textView_bio.getText().toString());
    }

    //may change in future
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
        textView_bio.setText(savedInstanceState.getString("bio"));
    }

    //may change in future
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


    public void openMyBooks(View view){
        //Intent intent = new Intent(this, InsertBook.class);
        //startActivity(intent);
    }

}
*/