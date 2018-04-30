package com.example.sergio.madlab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;;
import android.graphics.Bitmap;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class EditProfile extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static int RESULT_LOAD_IMG = 1;
    private  static  int REQUEST_IMAGE_CAPTURE =1;
    private ImageView imageView;
    private Uri imageUri; //save image uri
    String imgDecodableString;

    private SharedPreferences profile;
    private SharedPreferences.Editor editor;

    private DatabaseReference db;

    //retrieved data from database
    private String dbName="";
    private String dbEmail="";
    private String dbBio="";

    EditText editText_name;
    EditText editText_mail;
    EditText editText_bio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences
        profile = this.getSharedPreferences("profile", MODE_PRIVATE);

        setContentView(R.layout.activity_edit_profile);
        this.imageView = (ImageView)this.findViewById(R.id.imageView);

        Button btnOpenCamera = (Button) this.findViewById(R.id.btn_openCamera);
        btnOpenCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                //Bitmap photo = (Bitmap) cameraIntent.getExtras().get("data");
                //imageView.setImageBitmap(photo);
            }
        });

        editText_name = (EditText) findViewById(R.id.editText_name);
        editText_mail = (EditText) findViewById(R.id.editText_mail);
        editText_bio = (EditText) findViewById(R.id.editText_bio);



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


                editText_name.setText(dbName);
                editText_mail.setText(dbEmail);
                editText_bio.setText(dbBio);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"db fetch failed",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void openGallery(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);

                Toast.makeText(this, imageUri.toString(), Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }




    public void saveData(MenuItem menuItem) {
        //connect and save new data into database
        db = FirebaseDatabase.getInstance().getReference().child("Users").child("main");
        db.child("name").setValue(editText_name.getText().toString());
        db.child("email").setValue(editText_mail.getText().toString());
        db.child("bio").setValue(editText_bio.getText().toString());
        db.child("image").setValue(imageUri.toString());
        finish();
    }


}
