package com.example.sergio.madlab;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InsertBook extends AppCompatActivity {

    //retrieved data from database
    private long dbISBN=0;
    private String dbTitle="";
    private String dbAuthor="";
    private String dbPublisher="";
    private String dbEditYear= "";
    private String dbGenre="";
    private String dbTags="";

    private static int RESULT_LOAD_IMG = 1;
    private EditText edit1;
    private EditText editt2;

    private DatabaseReference databaseIB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_book);

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

                /*
                textView_name.setText(dbName);
                textView_mail.setText(dbEmail);
                textView_bio.setText(dbBio);
                */
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(),"db fetch failed",Toast.LENGTH_SHORT).show();
            }
        });


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

}
