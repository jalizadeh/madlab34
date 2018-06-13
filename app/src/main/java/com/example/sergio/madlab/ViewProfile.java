package com.example.sergio.madlab;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sergio.madlab.Classes.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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


public class ViewProfile extends AppCompatActivity  implements OnMapReadyCallback {

    private static final String filename = "profileImage.jpeg";
    private static final String TAG = "DatabaseError";

    //TextViews
    private TextView name;
    private TextView mail;
    private TextView bio;
    private TextView location;

    private String userID;

    //SharedPreferences
    private SharedPreferences preferences;
    private FirebaseUser authUser;
    private DatabaseReference database, userLocations;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private User user;
    private String email;

    private ImageView profileImage;
    private ProgressDialog progressDialog;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        database = FirebaseDatabase.getInstance().getReference();
        ref = database.child("users");
        storageRef = FirebaseStorage.getInstance().getReference();
        preferences = getSharedPreferences("Info", Context.MODE_PRIVATE);
        userLocations = database.child("user_locations");

        getTextViews();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_viewProfile);
        setSupportActionBar(toolbar);


        //progressDialog = new ProgressDialog(this);


        authUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = authUser.getUid();

        getUserReference();

        profileImage = (ImageView) findViewById(R.id.imageView);
        setImageView();



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
        downloadToLocalFile(storageRef.child("images").child(userID).child("profile_image"));
    }

    //Get all the text views
    private void getTextViews(){
        name = (TextView)findViewById(R.id.name_text);
        mail = (TextView)findViewById(R.id.mail_text);
        bio = (TextView)findViewById(R.id.bio_text);
        location = (TextView)findViewById(R.id.location_text);
    }

    //Sel all the texts
    private void setTexts(){
        name.setText(user.getName());
        mail.setText(user.getEmail());
        bio.setText(user.getBio());
        location.setText(user.getCity());
    }

    private void getUserReference(){
        ref.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                setTexts();
                setTitle(user.getName());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void downloadToLocalFile(StorageReference fileRef) {
        /*
        progressDialog.setTitle(getString(R.string.downloading));
        progressDialog.setMessage(null);
        progressDialog.show();
        */
        if (fileRef != null) {

            try {
                final File localFile = File.createTempFile("profileImage", "jpeg");

                fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        profileImage.setImageBitmap(bmp);
                        //progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //progressDialog.dismiss();
                        Toast.makeText(ViewProfile.this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(ViewProfile.this, R.string.error_noImage, Toast.LENGTH_LONG).show();
        }
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        userLocations.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //DataSnapshot location = dataSnapshot;

                    double latitude = (double) dataSnapshot.child("l").child("0").getValue();
                    double longitude = (double) dataSnapshot.child("l").child("1").getValue();
                    LatLng locations = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(locations).title("Your location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locations, 12.0f));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        onMapReady(mMap);
    }


}