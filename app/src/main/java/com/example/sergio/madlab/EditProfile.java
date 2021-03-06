package com.example.sergio.madlab;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sergio.madlab.Classes.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

;

public class EditProfile extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback {


    private final int PLACE_PICKER_REQUEST = 4;
    private final int OPEN_GALLERY = 5;
    private final int OPEN_CAMERA = 6;

    private final String filename = "profileImage.jpg";
    private static final String filepath = "images";

    //edit texts
    private EditText etName;
    private EditText etEmail;
    private EditText etBio;
    private EditText etCity;
    private ImageView iChangeLocation;

    private Button btnLoadImage;
    private Button btnOpenCamera;

    private String name;
    private String email;
    private String bio;
    private String city;
    private String userEmail;

    //ImageView
    private ImageView profileImage;
    private Bitmap bitmap;
    private String path;
    private String localImage;

    //shared preferences
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //Firebase
    private FirebaseUser authUser;
    private StorageReference storageRef;
    private DatabaseReference database, userLocations;
    private DatabaseReference ref;

    //User class
    private User user;
    private String userID;
    private String userIDfromDB;

    private LatLng chosenLocation;
    private GoogleMap mMap;

    //private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_editProfile);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("profile", Context.MODE_PRIVATE);
        editor = preferences.edit();


        database = FirebaseDatabase.getInstance().getReference();
        ref = database.child("users");
        userLocations = database.child("user_locations");

        storageRef = FirebaseStorage.getInstance().getReference();

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //userEmail = authUser.getEmail().replace(",", ".").replace(".", ",");


        //define Editetxts
        etName = (EditText) findViewById(R.id.editText_name);
        //etEmail = (EditText) findViewById(R.id.editText_mail);
        etBio = (EditText) findViewById(R.id.editText_bio);
        etCity = (EditText) findViewById(R.id.editText_city);


        //set buttons
        btnLoadImage = (Button) findViewById(R.id.btn_loadImage);
        btnOpenCamera = (Button) findViewById(R.id.btn_openCamera);
        btnLoadImage.setOnClickListener((View.OnClickListener) this);
        btnOpenCamera.setOnClickListener((View.OnClickListener) this);
        profileImage = (ImageView)findViewById(R.id.imageView);

        iChangeLocation = (ImageView)findViewById(R.id.changeLocation);
        iChangeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(EditProfile.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        localImage = preferences.getString("imagePath", null);
        if (localImage == null){
            //there is no image saved locally by user
            Toast.makeText(this, "Please provide an image", Toast.LENGTH_SHORT).show();
        } else {
            String localImagePath = localImage;
            File imgFile = new  File(localImagePath, "profileImage.jpg");
            if(imgFile.exists()){
                bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                profileImage.setImageBitmap(bitmap);
            }
        }


        //fetch the users data
        fetchUserProfile();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_profile, menu);
        return true;
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        if (v == btnLoadImage){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, OPEN_GALLERY);
        }
        if (v == btnOpenCamera) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, OPEN_CAMERA);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, OPEN_CAMERA);
                }
            }
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case OPEN_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(photoIntent, OPEN_CAMERA);

                } else {
                    Toast.makeText(EditProfile.this, "No camera permissions granted!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }




    //get the inserted data whenever needed
    public void getEditTextsValues(){
        name = etName.getText().toString();
        //email = etEmail.getText().toString();
        bio = etBio.getText().toString();
        city = etCity.getText().toString();
    }


    //when save button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
            getEditTextsValues();

            if (validateInputs(name, email, city)) {
                //?
                path = saveToInternalStorage(bitmap, filename);

                //save the image path for next use
                editor.putString("imagePath", path);
                editor.commit();

                uploadProfileImage();
                uploadUserData();

                if(chosenLocation != null)
                    insertLocation();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }



    private void uploadProfileImage() {
        StorageReference fileRef = storageRef.child("images").child(userID).child("profile_image");
        fileRef.putFile(getUri(path, filename))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Toast.makeText(EditProfile.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(EditProfile.this, "Upload failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    //before any action we have to check inputs
    private boolean validateInputs(String name, String email, String city) {
        if (name.isEmpty()){
            etName.setError(getString(R.string.error_invalid_name));
            etName.requestFocus();
            return false;
        }
        /*
        if (email.isEmpty()){
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return false;
        }
        */
        if (city.isEmpty()){
            etCity.setError(getString(R.string.error_invalid_city));
            etCity.requestFocus();
            return false;
        }

        return true;
    }


    //which way of inserting photo is chosen?
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == OPEN_GALLERY && resultCode == RESULT_OK && intent != null) {
            Uri uri = intent.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                path = saveToInternalStorage(bitmap, filename);

                editor.putString("imagePath", path);
                editor.commit();
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(EditProfile.this, "loading failed.", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == OPEN_CAMERA && resultCode == RESULT_OK && intent != null) {
            bitmap = (Bitmap) intent.getExtras().get("data");
            path = saveToInternalStorage(bitmap, filename);
            editor.putString("imagePath", path);
            editor.commit();
            profileImage.setImageBitmap(bitmap);

        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(intent, this);
                chosenLocation = place.getLatLng();
                etCity.setText(place.getAddress());
                //Toast.makeText(InsertBook.this, "Location chosen successfully", Toast.LENGTH_SHORT).show();
                onMapReady(mMap);
            }

        }
    }


    private void fetchUserProfile() {
        ref.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                etName.setText(user.getName());
                //etEmail.setText(user.getEmail());
                etBio.setText(user.getBio());
                etCity.setText(user.getCity());
                userIDfromDB = user.getUserID();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void uploadUserData() {
        getEditTextsValues();
        if(validateInputs(name, email,city)){
            user.setName(name);
            //user.setEmail(email);
            user.setBio(bio);
            user.setCity(city);
        }

        ref.child(userID).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish();
            }
        });
    }





    public String saveToInternalStorage(Bitmap bitmapImage, String filename){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir(filepath, Context.MODE_PRIVATE);
        File imagePath = new File(directory, filename);

        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(imagePath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        } catch (Exception e){
            e.getMessage();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.getMessage();
            }
        }

        return directory.getAbsolutePath();
    }


    public Uri getUri(String path, String filename){
        File image = new File(path, filename);
        return Uri.fromFile(image);
    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (chosenLocation == null) {
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
        } else {
            mMap.addMarker(new MarkerOptions().position(chosenLocation).title("Your location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(chosenLocation, 12.0f));
        }
    }





    private void insertLocation() {

        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("locations");
        GeoFire geoFire = new GeoFire(userLocations);
        //TODO do this with current location or location chosen by user
        geoFire.setLocation(userID, new GeoLocation(chosenLocation.latitude, chosenLocation.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });

    }



}