package com.example.sergio.madlab;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        /*
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
        */


        /*
        //create a database with name Users and its child book1

        edit1 = edit1.findViewById(R.id.isbn);
        edit1 = editt2.findViewById(R.id.editText_book_title);

        String isbn1 = edit1.getText().toString();
        String title = editt2.getText().toString();
        Map<String, String> saveDATA = new HashMap<String, String>();
        saveDATA.put("ISBN", isbn1);
        saveDATA.put("title",title);

         myRef.setValue(saveDATA);
         */
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
