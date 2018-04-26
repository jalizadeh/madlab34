package com.example.sergio.madlab;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InsertBook extends AppCompatActivity {

    private static int RESULT_LOAD_IMG = 1;
    private EditText edit1;
    private EditText editt2;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_book);

        edit1 = edit1.findViewById(R.id.isbn);
        edit1 = editt2.findViewById(R.id.editText_book_title);
        // Write a message to the database

        database = FirebaseDatabase.getInstance().getReference("Books");

        /*
        //create a database with name Users and its child book1

        String isbn1;
        isbn1 = edit1.getText().toString();
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
