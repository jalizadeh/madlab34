package com.example.sergio.madlab;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class InsertBook extends AppCompatPermissionActivity {

    private static final int GET_FROM_GALLERY = 5;
    private static final int PHOTO_REQUEST_CODE = 6;
    private static final int UPLOAD_IMAGE = 10;
    private static final int RELOAD_IMAGE = 11;


    //ImageView
    private static final String filename = "profileImage.jpeg"; //used when the save button is clicked
    private static final String tempFilename= "profileImage(temp).jpeg"; //used when the save button has not yet been cliked (temporarly upload)
    private ImageView ConditionImageView;
    private ConditionImageManager imageManager;
    private Bitmap bitmap;
    private String path;
    private boolean isChanged = false;
    private int finish;

    //UserInterface
    private Button retrieveButton;
    private Button scanButton;
    private EditText EditTextAuthor;
    private EditText EditTextTitle;
    private EditText EditTextISBN;
    private EditText EditTextPublisher;
    private EditText EditTextPublishedYear;
    private EditText EditTextCondition;

    private String ISBNCode;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String author;
    private String title;
    private String publisher;
    private String publishYear;
    private String condition;

    private FirebaseUser authUser;
    private DatabaseReference database;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private Book book;
    private String email;

    private ProgressDialog progressDialog;

    //Flow: Main activity with possibility to fill in the fields manually or to press the button "Scan a barcode"
    // "Scan a barcode -> Intent to the "ScannerActivity" page: surface view that shows the camera relevation and in result of pointing a barcode print n the screen the code
    //                    If you press the button "Import", this will  return to Main Activity the code using the putExtra of intent
    //                    InsertBook: save the code in the field ISBN and (if ISBN field is setted with the right number of cipher or pushing the button retrieve infos) start an Async Task that will retrieve frome googleapi/books some data about the book using the code just scanned
    //                    Note that the Async Task will start if the isbn is filled manually or scanning the code

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_book);

        retrieveButton = findViewById(R.id.startRetrieveTask);
        scanButton = findViewById(R.id.btn_book_isbn);
        EditTextAuthor=findViewById(R.id.editText_book_author);
        EditTextTitle=findViewById(R.id.editText_book_title);
        EditTextISBN=findViewById(R.id.editText_book_isbn);
        EditTextPublisher=findViewById(R.id.editText_book_publisher);
        EditTextPublishedYear=findViewById(R.id.editText_book_editYear);
        EditTextCondition = findViewById(R.id.editText_book_condition);
        progressDialog = new ProgressDialog(this);
        //set photo condition image view
        preferences = getSharedPreferences("ISBN", Context.MODE_PRIVATE);
        editor = preferences.edit();
        path = preferences.getString("path", null);
        storageRef = FirebaseStorage.getInstance().getReference();

        setProfileImageListener();
        setImageView(UPLOAD_IMAGE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_insert_book);
        setSupportActionBar(toolbar);


        database = FirebaseDatabase.getInstance().getReference();
        ref = database.child("books");


        ISBNCode = getIntent().getStringExtra("ISBNCode");
        EditTextISBN.setText(ISBNCode);
        makeRetrieveButtonClickable();

        //When Scan Code is clicked is started an intent that bring to the scanner activity
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent toScanner_intent = new Intent(InsertBook.this, BarcodeReader.class); //intent that leads to the ScannerActivity
                startActivity(toScanner_intent);
            }
        });

        retrieveButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //Put a message in the textView
                EditTextAuthor.setText(R.string.search_info);
                EditTextTitle.setText(R.string.search_info);
                EditTextPublisher.setText(R.string.search_info);
                EditTextPublishedYear.setText(R.string.search_info);

                // Start the AsyncTask.
                // The AsyncTask has a callback that will update the text view.
                ISBNCode = EditTextISBN.getText().toString(); //ISBN Scanned or if manually modified, the last inserted
                makeRetrieveButtonClickable();
                new AsyncTaskQueryISBN(EditTextAuthor, EditTextTitle, EditTextPublisher, EditTextPublishedYear).execute(ISBNCode); //Insert here the ISBN code scanned
                //Note: The execute() method is where you pass in the parameters (separated by commas)
                //that are then passed into doInBackground() by the system. Since this AsyncTask has no parameters, you will leave it blank.
            }
        });

        authUser = FirebaseAuth.getInstance().getCurrentUser();
        email = authUser.getEmail().replace(",",",,").replace(".", ",");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        String isbn = preferences.getString("isbnNumber", "");
        EditTextISBN.setText(isbn);
    }

    //Implementing the upload or instant camera of a photo to document the book condition
    // After the permission (givenor not given by user on request) to access the camera or the gallery
    //The correspondent intent are started to 1) take a photo or 2) upload it from the gallery using the "start activity for result"
    // depending on if the permission is given or not ("onActivityResult")

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
        if(path == null){
            Log.d("PathNull", "Path is null");
        }
    }

    //Linked to the startActivityForResult of "onPermissionGranted"
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                //in case of upload from gallery, the image is taken opening the uri of the resource as a stream,saved with a
                // temporary filename (to retrieve it later)  and setted in the imageview
                case GET_FROM_GALLERY:
                    Uri uri = data.getData();
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        path = imageManager.saveToInternalStorage(bitmap, tempFilename, getApplicationContext());
                        preferences.edit().putString("path", path).commit();
                        isChanged = true;
                        ConditionImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e("bitmap", "Failure: error on bitmap upload");
                    }
                    break;

                //in case of new photo taken -> the photo is saved in internal storage with a
                // temporary filename (to retrieve it later), and the image view is setted with it
                case PHOTO_REQUEST_CODE:
                    bitmap = (Bitmap) data.getExtras().get("data");
                    path = imageManager.saveToInternalStorage(bitmap, tempFilename, getApplicationContext());
                    //editor.commit();
                    isChanged = true;
                    ConditionImageView.setImageBitmap(bitmap);
                    break;
            }
        }
    }

    //insert an "onClickListener" on the image view, that displays the alert dialog questioning if the user want to upload or take the photo
    //The user answer is inserted in the int which (0 for using the camera, 1 for accessing the gallery)
    //The method proceeds then to request the correspondent permissions -> "onPermissionGranted"
    private void setProfileImageListener(){
        ConditionImageView = findViewById(R.id.imageView);
        imageManager = new ConditionImageManager();

        ConditionImageView.setOnClickListener(new ImageView.OnClickListener(){

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InsertBook.this);
                builder.setItems(new String[]{getString(R.string.camera), getString(R.string.gallery)}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                requestAppPermission(new String[]{android.Manifest.permission.CAMERA}, R.string.permission_msg, PHOTO_REQUEST_CODE);
                                break;
                            case 1:
                                requestAppPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},R.string.permission_msg, GET_FROM_GALLERY);
                                break;
                        }
                    }
                }).create().show();
            }
        });

    }

    //Restore the picture uploaded, taking the path in which is saved from preferences
    private void setImageView(int action) {
        // path = preferences.getString("imagePath", null);
        switch (action) {
            case UPLOAD_IMAGE:
                if (path != null) {
                    Log.d("PathNotNull", "Upload: success");
                    bitmap = imageManager.loadImageFromInternalStorage(path, filename);
                    if (bitmap != null)
                        ConditionImageView.setImageBitmap(bitmap);
                }
                break;
            case RELOAD_IMAGE:
                if (path != null) {
                    Log.d("PathNotNull", "Reload: success");
                    bitmap = imageManager.loadImageFromInternalStorage(path, tempFilename);
                    if (bitmap != null)
                        ConditionImageView.setImageBitmap(bitmap);
                }
                break;
        }

    }

    /*
    //Set app Toolbar
    private void setToolbar(){
        Toolbar toolbar;
        toolbar =  findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);
        setTitle("Share a Book");
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_done:
                getTextFromEditTextView();

                if(checkEditTextViewInput()){
                    path = imageManager.saveToInternalStorage(bitmap, filename, getApplicationContext());
                    editor.putString("imagePath", path);
                    editor.commit();
                    if(isChanged)
                        uploadToFirebase(storageRef.child("images").child(email).child("book images").child(ISBNCode));
                    else
                        saveDataToFirebase();
                }

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

    private boolean checkEditTextViewInput(){
        Toast missing;

        if (ISBNCode.isEmpty() && !ISBNCode.equals(getString(R.string.no_data))) {
            missing = Toast.makeText(this, R.string.isbn_missing, Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }

        if (author.isEmpty() && !author.equals(getString(R.string.no_data))) {
            missing = Toast.makeText(this, R.string.author_missing, Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }

        if (title.isEmpty() && !title.equals(getString(R.string.no_data))) {
            missing = Toast.makeText(this, R.string.title_missing, Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }

        if (publisher.isEmpty() && !publisher.equals(getString(R.string.no_data))) {
            missing = Toast.makeText(this, R.string.publ_missing, Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }
        if (publishYear.isEmpty() && !publisher.equals(getString(R.string.no_data))) {
            missing = Toast.makeText(this, R.string.year_missing, Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }
        if (condition.isEmpty() && !condition.equals(getString(R.string.no_data))) {
            missing = Toast.makeText(this, R.string.condition_missing, Toast.LENGTH_SHORT);
            missing.show();
            return false;
        }

        book = new Book(ISBNCode, title, author, publisher, publishYear, "", condition);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("image", path);
        outState.putBoolean("isChanged", isChanged);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isChanged = savedInstanceState.getBoolean("isChanged");
        path = savedInstanceState.getString("image");

        if(isChanged)
            setImageView(RELOAD_IMAGE);
        else
            setImageView(UPLOAD_IMAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // setTexts();
    }

    private void makeRetrieveButtonClickable()
    {
        if(EditTextISBN.getText().toString().length() == 13 || EditTextISBN.getText().toString().length()==10)
            retrieveButton.isClickable();

    }

    private void getTextFromEditTextView(){
        ISBNCode=EditTextISBN.getText().toString();
        author=EditTextAuthor.getText().toString();
        title = EditTextTitle.getText().toString();
        publisher = EditTextPublisher.getText().toString();
        publishYear= EditTextPublishedYear.getText().toString();
        condition=EditTextCondition.getText().toString();
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
                    Toast.makeText(InsertBook.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
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
            Toast.makeText(InsertBook.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
        }
    }

    private void saveDataToFirebase(){
        finish = 0;
        database.child("title").child(book.getTitle().replaceAll("\\.", ",").toLowerCase()).child(email).push().setValue(book, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish++;
                if(finish == 5){
                    finish();
                }
            }
        });
        database.child("author").child(book.getAuthor().replaceAll("\\.", ",").toLowerCase()).child(email).push().setValue(book, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish++;
                if(finish == 5){
                    finish();
                }
            }
        });
        database.child("publisher").child(book.getPublisher().replaceAll("\\.", ",").toLowerCase()).child(email).push().setValue(book, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish++;
                if(finish == 5){
                    finish();
                }
            }
        });
        database.child("genre").child(book.getGenre().replaceAll("\\.", ",").toLowerCase()).child(email).push().setValue(book, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish++;
                if(finish == 5){
                    finish();
                }
            }
        });
        database.child("users").child(email).push().setValue(book, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish++;
                if(finish == 5){
                    finish();
                }
            }
        });
    }
}




/*
public class InsertBook extends AppCompatActivity {


    //retrieved data from database
    private String dbISBN="";
    private String dbTitle="";
    private String dbAuthor="";
    private String dbPublisher="";
    private String dbEditYear= "";
    private String dbGenre="";
    private String dbTags="";



    private String isbn="";
    private String title="";
    private String author="";
    private String publisher="";
    private String editYear= "";
    private String genre="";
    private String tags="";


    private static int RESULT_LOAD_IMG = 1;
    private EditText etISBN;
    private EditText etTitle;
    private EditText etAuthor;
    private EditText etPublisher;
    private EditText etEditYear;
    private Spinner spGenre;
    private EditText etTags;

    private DatabaseReference databaseIB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_book);

        etISBN = (EditText) findViewById(R.id.editText_book_isbn);
        etTitle = (EditText) findViewById(R.id.editText_book_title);
        etAuthor = (EditText) findViewById(R.id.editText_book_author);
        etPublisher = (EditText) findViewById(R.id.editText_book_publisher);
        etEditYear = (EditText) findViewById(R.id.editText_book_editYear);
        spGenre = (Spinner) findViewById(R.id.spinner_bookGenre);
        etTags = (EditText) findViewById(R.id.editText_book_tags);


        // read from Books
        databaseIB = FirebaseDatabase.getInstance().getReference("Books");
        databaseIB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    dbISBN =  messageSnapshot.child("isbn").getValue(Long.class);
                    dbTitle = (String) messageSnapshot.child("title").getValue();
                    dbAuthor = (String) messageSnapshot.child("bio").getValue();
                }
                //Toast.makeText(getApplicationContext(),dbName+'-'+dbEmail+'-'+dbBio,Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),dbISBN+"-"+dbTitle,Toast.LENGTH_SHORT).show();


                //textView_name.setText(dbName);
                //textView_mail.setText(dbEmail);
                //textView_bio.setText(dbBio);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"db fetch failed",Toast.LENGTH_SHORT).show();
            }
        });




        //create a database with name Users and its child book1

        edit1 = edit1.findViewById(R.id.isbn);
        edit1 = editt2.findViewById(R.id.editText_book_title);

        String isbn1 = edit1.getText().toString();
        String title = editt2.getText().toString();
        Map<String, String> saveDATA = new HashMap<String, String>();
        saveDATA.put("ISBN", isbn1);
        saveDATA.put("title",title);

         myRef.setValue(saveDATA);

    }


    public void openGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_insert_book, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    public void saveData(MenuItem menuItem) {

        //prepare data to be stored
        // may change in future: input validity is not checked
        isbn = etISBN.getText().toString();
        title = etTitle.getText().toString();
        author = etAuthor.getText().toString();
        publisher = etPublisher.getText().toString();
        editYear = etEditYear.getText().toString();
        genre = spGenre.getSelectedItem().toString();
        tags = etTags.getText().toString();


        //create a new child with isbn as its unique ID
        if (isbn != null && isbn.length() > 0){
            databaseIB = FirebaseDatabase.getInstance().getReference("Books").child(isbn);

            databaseIB.child("title").setValue(title);
            databaseIB.child("author").setValue(author);
            databaseIB.child("publisher").setValue(publisher);
            databaseIB.child("edityear").setValue(editYear);
            databaseIB.child("genre").setValue(genre);
            databaseIB.child("tags").setValue(tags);

            //show all input
            //Toast.makeText(this, isbn+"\n"+title+"\n"+author+"\n"+publisher+"\n"+editYear+"\n"+genre+"\n"+tags,Toast.LENGTH_SHORT).show();

            Toast.makeText(this, "Book saved successfully!",Toast.LENGTH_SHORT).show();

            finish();
        } else {
            Toast.makeText(this, "ISBN is not valid.\nno data saved",Toast.LENGTH_SHORT).show();
            finish();
        }

    }

}
*/