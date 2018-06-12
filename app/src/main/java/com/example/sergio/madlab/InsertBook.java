package com.example.sergio.madlab;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.example.sergio.madlab.Classes.User;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
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

import com.example.sergio.madlab.Classes.*;


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
//https://www.googleapis.com/books/v1/volumes?q=isbn:9780136123569



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
    private String condition="";


    private Bitmap bitmap;
    private ImageView bookImage;

    private EditText etISBN;
    private EditText etTitle;
    private EditText etAuthor;
    private EditText etPublisher;
    private EditText etEditYear;
    private Spinner spGenre;
    private EditText etTags;
    private EditText etCondition;


    private Button loadImage;
    private Button openCamera;
    private Button openBarcode;
    private Button getBarcodeData;


    //Firebase
    private String userID;
    private FirebaseUser authUser;
    private StorageReference storageRef;
    private DatabaseReference database;
    private DatabaseReference booksDB;


    Uri uri;
    File filename;
    private String path;

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
        etCondition = (EditText) findViewById(R.id.editText_book_condition);


        //get button views
        loadImage = (Button) findViewById(R.id.btnLoadImage);
        openCamera = (Button) findViewById(R.id.btnOpenCamera);
        openBarcode = (Button) findViewById(R.id.btnBarcodeISBN);
        getBarcodeData = (Button) findViewById(R.id.btnSearchISBN);

        loadImage.setOnClickListener((View.OnClickListener) this);
        openCamera.setOnClickListener((View.OnClickListener) this);
        openBarcode.setOnClickListener((View.OnClickListener) this);
        getBarcodeData.setOnClickListener((View.OnClickListener) this);


        //
        database = FirebaseDatabase.getInstance().getReference();
        booksDB = database.child("books");

        storageRef = FirebaseStorage.getInstance().getReference();

        // will be used for storing in book structure
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = authUser.getUid();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_insert_book, menu);
        return super.onCreateOptionsMenu(menu);
    }



    //1. save book data
    //2. save image
    //when save button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
            //prepare data to be stored
            // may change in future: input validity is not checked
            getEditTextsValues();
            if (validateValues()){
                uploadBookData();
                uploadBookImage();
                Toast.makeText(this, "Book data uploaded successfully",Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please check the fields.",Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public void getEditTextsValues(){
        isbn = etISBN.getText().toString();
        title = etTitle.getText().toString();
        author = etAuthor.getText().toString();
        publisher = etPublisher.getText().toString();
        editYear = etEditYear.getText().toString();
        genre = spGenre.getSelectedItem().toString();
        tags = etTags.getText().toString();
        condition = etCondition.getText().toString();
    }

    public boolean validateValues(){
        if (isbn.isEmpty()){
            etISBN.setError(getString(R.string.error_invalid_isbn));
            etISBN.requestFocus();
            return false;
        }
        if (title.isEmpty()){
            etTitle.setError(getString(R.string.error_invalid_title));
            etTitle.requestFocus();
            return false;
        }
        if (author.isEmpty()){
            etAuthor.setError(getString(R.string.error_invalid_author));
            etAuthor.requestFocus();
            return false;
        }
        if (publisher.isEmpty()){
            etPublisher.setError(getString(R.string.error_invalid_publisher));
            etPublisher.requestFocus();
            return false;
        }
        if (editYear.isEmpty()){
            etEditYear.setError(getString(R.string.error_invalid_edityear));
            etEditYear.requestFocus();
            return false;
        }
        if (genre.equals("SELECT")){
            TextView errorText = (TextView) spGenre.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_invalid_genre);//changes the selected item text to this
            errorText.requestFocus();
            return false;
        }
        if (condition.isEmpty()){
            etCondition.setError(getString(R.string.error_invalid_condition));
            etCondition.requestFocus();
            return false;
        }

        return true;
    }



    @Override
    public void onClick(View v) {
        if (v == loadImage){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, OPEN_GALLERY);
        }
        if (v == openCamera) {
            //Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, OPEN_CAMERA);
                }
            }
            //startActivityForResult(photoIntent, OPEN_CAMERA);
        }
        if (v == openBarcode){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
        if (v == getBarcodeData){
            closeKeyboard();
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case OPEN_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(photoIntent, OPEN_CAMERA);

                } else {
                    Toast.makeText(InsertBook.this, "No camera permissions granted!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != this.getCurrentFocus())
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
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
            uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                bookImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(InsertBook.this, "loading failed.", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == OPEN_CAMERA && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
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
                        if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("subtitle")) {
                            title += ", " + bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("subtitle");
                        }
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




    //all books are saved in 1 place so they will be searched by isbn
    private void uploadBookImage() {
        if (uri != null) {
            isbn += ".jpg";
            StorageReference fileRef = storageRef.child("images").child("books").child(isbn);
            fileRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(InsertBook.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                            //saveDataToFirebase();
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(InsertBook.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(InsertBook.this, "No image was picked", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadBookData() {
        //Toast.makeText(this, isbn+"\n"+title+"\n"+author+"\n"+publisher+"\n"+editYear+"\n"+genre+"\n"+tags,Toast.LENGTH_SHORT).show();

        book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setEdityear(editYear);
        book.setGenre(genre);
        book.setTags(tags);
        book.setCondition(condition);
        book.setUser(userID);

        insertLocation();

        booksDB.child(isbn).setValue(book);
    }

    private void insertLocation() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("locations");
        GeoFire geoFire = new GeoFire(ref);
        //TODO do this with current location or location chosen by user
        geoFire.setLocation(isbn, new GeoLocation(45.029739 + 0.084518 * new Random().nextDouble(), 7.615670 + 0.092441 * new Random().nextDouble()), new GeoFire.CompletionListener() {
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
