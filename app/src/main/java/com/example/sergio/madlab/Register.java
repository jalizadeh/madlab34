package com.example.sergio.madlab;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

public class Register extends AppCompatActivity  implements View.OnClickListener, OnMapReadyCallback {


    private final int PLACE_PICKER_REQUEST = 4;

    private EditText etEmail, etPass, etConfirm, etName, etCity;
    private Button btnSignup;
    private ImageView btnChangeLocation;

    private  String uID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference db, userLocations;

    private LatLng chosenLocation;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Views
        btnSignup=(Button)findViewById(R.id.btnSignup);
        btnChangeLocation=(ImageView) findViewById(R.id.changeLocation);
        etName = (EditText)findViewById(R.id.name);
        etEmail=(EditText)findViewById(R.id.email);
        etPass=(EditText)findViewById(R.id.password);
        etConfirm=(EditText)findViewById(R.id.password2);
        etCity=(EditText)findViewById(R.id.city);


        //
        btnSignup.setOnClickListener(this);
        btnChangeLocation.setOnClickListener(this);

        //Firebase
        firebaseAuth=FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(etName.getText().toString());
        arrayList.add(etEmail.getText().toString());
        arrayList.add(etPass.getText().toString());
        arrayList.add(etConfirm.getText().toString());
        arrayList.add(etCity.getText().toString());
        outState.putStringArrayList("user input", arrayList);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> arrayList = savedInstanceState.getStringArrayList("user input");

        if (arrayList != null) {
            etName.setText(arrayList.get(0));
            etEmail.setText(arrayList.get(1));
            etPass.setText(arrayList.get(2));
            etConfirm.setText(arrayList.get(3));
            etCity.setText(arrayList.get(4));
        }
    }


    private void registerUser(){
        final String name = etName.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPass.getText().toString();
        final String confirm = etConfirm.getText().toString();
        final String city = etCity.getText().toString();

        if (validateInputs(name, email, password, confirm, city)) {
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    etPass.setError(getString(R.string.error_invalid_password));
                                    etPass.requestFocus();
                                } catch (FirebaseAuthInvalidUserException e){
                                    etEmail.setError(getString(R.string.error_invalid_email));
                                    etEmail.requestFocus();;
                                } catch (FirebaseNetworkException e){
                                    Toast.makeText(Register.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                                } catch(Exception e) {
                                    Toast.makeText(Register.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            } else{
                                firebaseAuth.signInWithEmailAndPassword(email, password);
                                uID = firebaseAuth.getUid();
                                insertLocation(uID);
                                User user = new User(uID, name, email, city, "");
                                //String userID = email.replace(",",",,").replace(".", ",");
                                db.child("users").child(uID).setValue(user);

                                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(mainActivity);
                                finish();
                            }
                        }
                    });
        }

    }



    //check if all input values are valid
    private boolean validateInputs(String name, String email, String password, String passRepeat, String city){
        if (name.isEmpty()){
            etName.setError(getString(R.string.error_invalid_name));
            etName.requestFocus();
            return false;
        }
        if (email.isEmpty()){
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()){
            etPass.setError(getString(R.string.error_invalid_password));
            etPass.requestFocus();
            return false;
        }
        if (passRepeat.isEmpty()){
            etConfirm.setError(getString(R.string.error_invalid_repeat_password));
            etConfirm.requestFocus();
            return false;
        }
        if (city.isEmpty()){
            etCity.setError(getString(R.string.error_invalid_name));
            etCity.requestFocus();
            return false;
        }
        if (!password.equals(passRepeat)){
            etConfirm.setError(getString(R.string.error_incorrect_password));
            etConfirm.requestFocus();
            return false;
        }

        return true;
    }


    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSignup:
                registerUser();
                break;

            case R.id.changeLocation:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Register.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
        }
    }



    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (chosenLocation == null) {
            //do nothing
        } else {
            mMap.addMarker(new MarkerOptions().position(chosenLocation).title("Your location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(chosenLocation, 12.0f));
        }
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(intent, this);
                chosenLocation = place.getLatLng();
                etCity.setText(place.getAddress());
                //Toast.makeText(InsertBook.this, "Location chosen successfully", Toast.LENGTH_SHORT).show();
                onMapReady(mMap);
            }

        }
    }



    private void insertLocation(String uID) {
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("locations");
        userLocations = db.child("user_locations");
        GeoFire geoFire = new GeoFire(userLocations);
        //TODO do this with current location or location chosen by user
        geoFire.setLocation(uID, new GeoLocation(chosenLocation.latitude, chosenLocation.longitude), new GeoFire.CompletionListener() {
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
