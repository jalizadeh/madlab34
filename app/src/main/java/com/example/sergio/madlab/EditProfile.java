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

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.storage.UploadTask;
import com.example.sergio.madlab.AppCompatPermissionActivity;
import com.example.sergio.madlab.ProfileImageManager;
import com.example.sergio.madlab.User;

import java.io.File;
import java.io.IOException;

public class EditProfile extends AppCompatPermissionActivity {

    private static final int GET_FROM_GALLERY = 5;
    private static final int PHOTO_REQUEST_CODE = 6;
    private static final int UPLOAD_IMAGE = 10;
    private static final int RELOAD_IMAGE = 11;

    private static final String filename = "profileImage.jpeg";
    private static final String tempFilename = "profileImage(temp).jpeg";
    private static final String TAG = "DatabaseError";

    //edit texts
    private EditText name;
    private EditText mail;
    private EditText bio;
    private String nameText;
    private String mailText;
    private String bioText;

    //ImageView
    private ImageView profileImage;
    private ProfileImageManager imageManager;
    private Bitmap bitmap;
    private String path;
    private boolean isChanged = false;

    //shared preferences
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private FirebaseUser authUser;
    private StorageReference storageRef;
    private DatabaseReference database;
    private DatabaseReference ref;
    private User user;
    private String email;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        preferences = getSharedPreferences("Info", Context.MODE_PRIVATE);
        editor = preferences.edit();

        database = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        ref = database.child("users");
        authUser = FirebaseAuth.getInstance().getCurrentUser();

        //define Editetxts
        name = (EditText) findViewById(R.id.editText_name);
        mail = (EditText) findViewById(R.id.editText_mail);
        bio = (EditText) findViewById(R.id.editText_bio);

        progressDialog = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_editProfile);
        setSupportActionBar(toolbar);

        //set image view
        email = authUser.getEmail().replace(",", ".").replace(".", ",");
        setProfileImageListener();

        if (savedInstanceState != null)
            isChanged = savedInstanceState.getBoolean("isChanged");

        if (!isChanged)
            setImageView(UPLOAD_IMAGE);

        //set character counter
        getUserReference();
    }


    @Override
    public void onPermissionGranted(int requestCode) {
        switch (requestCode) {
            case PHOTO_REQUEST_CODE:
                Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(photoIntent, PHOTO_REQUEST_CODE);
                break;
            case GET_FROM_GALLERY:
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GET_FROM_GALLERY);
                break;
        }
        if (path == null) {
            Log.d("PathNull", "Path is null");
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isChanged = savedInstanceState.getBoolean("isChanged");

        if (isChanged)
            setImageView(RELOAD_IMAGE);
        else
            setImageView(UPLOAD_IMAGE);

        getUserReference();

        //get previous cursor focus
        getCursorFocus(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getTextFromEditTextView();
        user.setUsername(nameText);
        user.setBio(bioText);
        user.setEmail(email);
        ref.child(email).setValue(user);
        outState.putBoolean("isChanged", isChanged);

        //set cursor position
        setCursorFocus(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_profile, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_done:
                getTextFromEditTextView();

                if (checkEditTextViewInput()) {
                    path = imageManager.saveToInternalStorage(bitmap, filename, getApplicationContext());
                    editor.putString("imagePath", path);
                    editor.commit();
                    if (isChanged)
                        uploadToFirebase(storageRef.child("images").child(email).child("profile image"));
                    else
                        saveDataToFirebase();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_FROM_GALLERY:
                    Uri uri = data.getData();
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        path = imageManager.saveToInternalStorage(bitmap, tempFilename, getApplicationContext());
                        editor.putString("imagePath", path);
                        editor.commit();
                        isChanged = true;
                        profileImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e("bitmap", "Failure: error on bitmap upload");
                    }
                    break;

                case PHOTO_REQUEST_CODE:
                    bitmap = (Bitmap) data.getExtras().get("data");
                    path = imageManager.saveToInternalStorage(bitmap, tempFilename, getApplicationContext());
                    editor.putString("imagePath", path);
                    editor.commit();
                    isChanged = true;
                    profileImage.setImageBitmap(bitmap);
                    break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    private void setProfileImageListener() {
        profileImage = (ImageView) findViewById(R.id.imageView);
        imageManager = new ProfileImageManager();

        profileImage.setOnClickListener(new ImageView.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder.setItems(new String[]{"camera", "gallery"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                requestAppPermission(new String[]{Manifest.permission.CAMERA}, R.string.permission_msg, PHOTO_REQUEST_CODE);
                                break;
                            case 1:
                                requestAppPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, R.string.permission_msg, GET_FROM_GALLERY);
                                break;
                        }
                    }
                }).create().show();
            }

        });

    }

    private void setImageView(int action) {
        path = preferences.getString("imagePath", null);
        switch (action) {
            case UPLOAD_IMAGE:
                if (path != null) {
                    Log.d("PathNotNull", "Upload: success");
                    downloadToLocalFile(storageRef.child("images").child(email).child("profile image"));
                }
                break;
            case RELOAD_IMAGE:
                if (path != null) {
                    Log.d("PathNotNull", "Reload: success");
                    bitmap = imageManager.loadImageFromInternalStorage(path, tempFilename);
                    if (bitmap != null)
                        profileImage.setImageBitmap(bitmap);
                }
                break;
        }

    }

    private boolean checkEditTextViewInput() {
        Toast missing;

        if (nameText.isEmpty()) {
            missing = Toast.makeText(this, "insert name", Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }

        if (mailText.isEmpty()) {
            missing = Toast.makeText(this, "insert mail", Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }

        user.setUsername(nameText);
        user.setBio(bioText);
        user.setEmail(mailText);

        return true;
    }

    private void getTextFromEditTextView() {
        nameText = name.getText().toString();
        mailText = mail.getText().toString();
        bioText = bio.getText().toString();
    }



    private void setCursorFocus(Bundle outState) {
        View focusedChild = getCurrentFocus();

        if (focusedChild != null) {
            int focusID = focusedChild.getId();
            int cursorLoc = 0;

            if (focusedChild instanceof EditText) {
                cursorLoc = ((EditText) focusedChild).getSelectionStart();
            }

            outState.putInt("focusID", focusID);
            outState.putInt("cursorLoc", cursorLoc);
        }
    }

    private void getCursorFocus(Bundle savedInstanceState) {
        int focusID = savedInstanceState.getInt("focusID", View.NO_ID);

        View focusedChild = findViewById(focusID);
        if (focusedChild != null) {
            focusedChild.requestFocus();

            if (focusedChild instanceof EditText) {
                int cursorLoc = savedInstanceState.getInt("cursorLoc", 0);
                ((EditText) focusedChild).setSelection(cursorLoc);
            }
        }
    }

    private void getUserReference() {
        ref.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                name.setText(user.getUsername());
                mail.setText(user.getEmail());
                bio.setText(user.getBio());
                //counterView.setText(String.format("%s/200", String.valueOf(user.getBio().length())));
                //bio.addTextChangedListener(characterWatcher);
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
                        Toast.makeText(EditProfile.this, exception.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(EditProfile.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
        }
    }

    private void uploadToFirebase(StorageReference fileRef) {
        if (fileRef != null) {
            progressDialog.setTitle(getString(R.string.uploading));
            progressDialog.setMessage(null);
            progressDialog.show();

            fileRef.putFile(imageManager.getUri(path, filename))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            saveDataToFirebase();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfile.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    // percentage in progress dialog
                    progressDialog.setMessage(getString(R.string.uploaded) + ((int) progress) + getString(R.string.perc));
                }
            });
        } else {
            Toast.makeText(EditProfile.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
        }
    }

    private void saveDataToFirebase() {
        ref.child(email).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish();
            }
        });
    }


}