package com.example.sergio.madlab;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InsertBook extends AppCompatActivity implements View.OnClickListener{

    /*
    //retrieved data from database
    private String dbISBN="";
    private String dbTitle="";
    private String dbAuthor="";
    private String dbPublisher="";
    private String dbEditYear= "";
    private String dbGenre="";
    private String dbTags="";

    */

    private final int OPEN_GALLERY = 1;
    private final int OPEN_CAMERA = 2;
    private final int OPEN_BARCODE_READER = 3;
    private final String GOOGLE_ISBN_LINK = "https://www.googleapis.com/books/v1/volumes?q=isbn:";




    //User class
    private User user;
    private Book book;

    private String isbn="";
    private String title="";
    private String author="";
    private String publisher="";
    private String editYear= "";
    private String genre="";
    private String tags="";


    private Bitmap bitmap;
    private ImageView bookImage;

    private EditText etISBN;
    private EditText etTitle;
    private EditText etAuthor;
    private EditText etPublisher;
    private EditText etEditYear;
    private Spinner spGenre;
    private EditText etTags;
    private EditText etBookCondition;


    private Button loadImage;
    private Button openCamera;
    private Button openBarcode;
    private Button getBarcodeData;


    //Firebase
    private FirebaseUser authUser;
    private StorageReference storageRef;
    private DatabaseReference database;
    private DatabaseReference ref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_book);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_insert_book);
        setSupportActionBar(toolbar);


        bookImage = (ImageView) findViewById(R.id.bookImage);
        etISBN = (EditText) findViewById(R.id.editText_book_isbn);
        etTitle = (EditText) findViewById(R.id.editText_book_title);
        etAuthor = (EditText) findViewById(R.id.editText_book_author);
        etPublisher = (EditText) findViewById(R.id.editText_book_publisher);
        etEditYear = (EditText) findViewById(R.id.editText_book_editYear);
        spGenre = (Spinner) findViewById(R.id.spinner_bookGenre);
        etTags = (EditText) findViewById(R.id.editText_book_tags);
        etBookCondition = (EditText) findViewById(R.id.editText_book_condition);


        //get button views
        loadImage = (Button) findViewById(R.id.btnLoadImage);
        openCamera = (Button) findViewById(R.id.btnOpenCamera);
        openBarcode = (Button) findViewById(R.id.btnBarcodeISBN);
        getBarcodeData = (Button) findViewById(R.id.btnSearchISBN);

        loadImage.setOnClickListener((View.OnClickListener) this);
        openCamera.setOnClickListener((View.OnClickListener) this);
        openBarcode.setOnClickListener((View.OnClickListener) this);
        getBarcodeData.setOnClickListener((View.OnClickListener) this);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_insert_book, menu);
        return super.onCreateOptionsMenu(menu);
    }



    //when save button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
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
            database = FirebaseDatabase.getInstance().getReference("Books").child(isbn);
            database.child("title").setValue(title);
            database.child("author").setValue(author);
            database.child("publisher").setValue(publisher);
            database.child("edityear").setValue(editYear);
            database.child("genre").setValue(genre);
            database.child("tags").setValue(tags);

            //show all input
            Toast.makeText(this, isbn+"\n"+title+"\n"+author+"\n"+publisher+"\n"+editYear+"\n"+genre+"\n"+tags,Toast.LENGTH_SHORT).show();

            //Toast.makeText(this, "Book saved successfully!",Toast.LENGTH_SHORT).show();

            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (v == loadImage){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, OPEN_GALLERY);
        }
        if (v == openCamera) {
            Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(photoIntent, OPEN_CAMERA);
        }
        if (v == openBarcode){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        if (v == getBarcodeData){
            if (etISBN.getText().toString().isEmpty()){
                etISBN.setError("Insert the ISBN");
                etISBN.requestFocus();
            } else {
                Toast.makeText(getApplicationContext(), "Trying to get book`s data...", Toast.LENGTH_LONG).show();
                FindBookApiTask task = new FindBookApiTask();
                task.execute(GOOGLE_ISBN_LINK + etISBN.getText().toString());
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null && (scanningResult.getFormatName().equals("EAN_8") || scanningResult.getFormatName().equals("EAN_13"))) {
            String scanContent = scanningResult.getContents();
            String apiUrlString = GOOGLE_ISBN_LINK + scanContent;
            FindBookApiTask task = new FindBookApiTask();
            task.execute(apiUrlString);

        } else if (requestCode == OPEN_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                bookImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(InsertBook.this, "loading failed.", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == OPEN_CAMERA && resultCode == RESULT_OK && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            bookImage.setImageBitmap(bitmap);

        } else {
            Toast.makeText(getApplicationContext(), "Invalid barcode!", Toast.LENGTH_SHORT).show();
        }
    }


    private class FindBookApiTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(urls[0]);
                HttpResponse response = client.execute(request);

                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                in.close();
                String html = str.toString();
                return html;
            } catch (IOException e) {
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject bookJson = new JSONObject(result);
                if (bookJson.getInt("totalItems") == 1) {
                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("industryIdentifiers").getJSONObject(0).has("identifier")) {
                        isbn = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier");
                    } else {
                        isbn = "";
                    }

                    etISBN.setText(isbn);

                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("title")) {
                        title = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("title");
                    } else {
                        title ="";
                    }
                    etTitle.setText(title);

                    author = "";
                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("authors")) {
                        JSONArray authorsArray = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("authors");
                        for (int i = 0; i < authorsArray.length(); i++) {
                            author = author + authorsArray.getString(i);
                            if (authorsArray.length() != i + 1) {
                                author = author + ", ";
                            }
                        }
                    }
                    etAuthor.setText(author);

                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("publisher")) {
                        publisher = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("publisher");
                    } else {
                        publisher = "";
                    }
                    etPublisher.setText(publisher);

                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("publishedDate")) {
                        editYear = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("publishedDate").substring(0,4);
                    } else {
                        editYear = "";
                    }
                    etEditYear.setText(editYear);

                    Toast.makeText(getApplicationContext(), "Book data imported successfully!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "The scanned book is not in the database!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



/*
    private void uploadBookImage() {
        StorageReference fileRef = storageRef.child("images").child(userEmail).child("profile_image");
        if (fileRef != null) {
            fileRef.putFile(getUri(path, filename))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditProfile.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                            //saveDataToFirebase();
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(EditProfile.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(EditProfile.this, R.string.profile_not_exists, Toast.LENGTH_LONG).show();
        }
    }

    private void uploadBookData() {
        getEditTextsValues();
        if(validateInputs(name, email,city)){
            user.setName(name);
            user.setEmail(email);
            user.setBio(bio);
            user.setCity(city);
        }

        ref.child(userEmail).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish();
            }
        });
    }
    */
}
