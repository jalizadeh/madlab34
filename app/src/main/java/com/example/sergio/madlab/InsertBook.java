package com.example.sergio.madlab;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InsertBook extends AppCompatActivity {

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

    }


    public void openGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
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
        databaseIB = FirebaseDatabase.getInstance().getReference("Books").child(isbn);
        databaseIB.child("title").setValue(title);
        databaseIB.child("author").setValue(author);
        databaseIB.child("publisher").setValue(publisher);
        databaseIB.child("edityear").setValue(editYear);
        databaseIB.child("genre").setValue(genre);
        databaseIB.child("tags").setValue(tags);

        //show all input
        Toast.makeText(this, isbn+"\n"+title+"\n"+author+"\n"+publisher+"\n"+editYear+"\n"+genre+"\n"+tags,Toast.LENGTH_SHORT).show();

        //Toast.makeText(this, "Book saved successfully!",Toast.LENGTH_SHORT).show();

        finish();
    }

    public void scanCode(View view) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null && (scanningResult.getFormatName().equals("EAN_8") || scanningResult.getFormatName().equals("EAN_13"))) {
            String scanContent = scanningResult.getContents();
            String apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + scanContent;
            FindBookApiTask task = new FindBookApiTask();
            task.execute(apiUrlString);
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
                    TextView textView_isbn = findViewById(R.id.editText_book_isbn);
                    textView_isbn.setText(isbn);

                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("title")) {
                        title = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("title");
                    } else {
                        title ="";
                    }
                    TextView textView_title = findViewById(R.id.editText_book_title);
                    textView_title.setText(title);

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
                    TextView textView_author = findViewById(R.id.editText_book_author);
                    textView_author.setText(author);

                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("publisher")) {
                        publisher = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("publisher");
                    } else {
                        publisher = "";
                    }
                    TextView textView_publisher = findViewById(R.id.editText_book_publisher);
                    textView_publisher.setText(publisher);

                    if (bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").has("publishedDate")) {
                        editYear = bookJson.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("publishedDate").substring(0,4);
                    } else {
                        editYear = "";
                    }
                    TextView textView_editYear = findViewById(R.id.editText_book_editYear);
                    textView_editYear.setText(editYear);

                    Toast.makeText(getApplicationContext(), "Book data imported successfully!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "The scanned book is not in the database!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
