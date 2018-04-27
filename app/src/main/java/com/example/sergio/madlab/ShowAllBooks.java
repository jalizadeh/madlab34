package com.example.sergio.madlab;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.widget.Toast.LENGTH_SHORT;


public class ShowAllBooks extends AppCompatActivity{

    DatabaseReference db;
    private RecyclerView mBookList;
    EditText nameEditTxt, propTxt, descTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_books);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //lv = (ListView) findViewById(R.id.lv);

        //INITIALIZE FIREBASE DB
        db = FirebaseDatabase.getInstance().getReference().child("Books");
        //
        db.keepSynced(true);

        mBookList = (RecyclerView) findViewById(R.id.myrecycleview);
        mBookList.hasFixedSize();
        mBookList.setLayoutManager(new LinearLayoutManager(this));

        //helper = new FirebaseHelper(db);

        //ADAPTER
        //adapter = new CustomAdapter(this, helper.retrieve());
        //lv.setAdapter(adapter);


    }

    /*
    //DISPLAY INPUT DIALOG
    private void displayInputDialog() {
        Dialog d = new Dialog(this);
        d.setTitle("Save To Firebase");
        d.setContentView(R.layout.input_dialog);

        nameEditTxt = (EditText) d.findViewById(R.id.nameEditText);
        propTxt = (EditText) d.findViewById(R.id.propellantEditText);
        descTxt = (EditText) d.findViewById(R.id.descEditText);
        Button saveBtn = (Button) d.findViewById(R.id.saveBtn);

        //SAVE
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //GET DATA
                String name = nameEditTxt.getText().toString();
                String propellant = propTxt.getText().toString();
                String desc = descTxt.getText().toString();

                //SET DATA
                Spacecraft s = new Spacecraft();
                s.setName(name);
                s.setPropellant(propellant);
                s.setDescription(desc);

                //SIMPLE VALIDATION
                if (name != null && name.length() > 0) {
                    //THEN SAVE
                    if (helper.save(s)) {
                        //IF SAVED CLEAR EDITXT
                        nameEditTxt.setText("");
                        propTxt.setText("");
                        descTxt.setText("");

                        adapter = new CustomAdapter(MainActivity.this, helper.retrieve());
                        lv.setAdapter(adapter);

                    }
                } else {
                    Toast.makeText(MainActivity.this, "Name Must Not Be Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        d.show();
    }
    */

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Book, BookViewHolder>  firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, BookViewHolder>
                (Book.class, R.layout.book_cardview, BookViewHolder.class, db) {
            @Override
            protected void populateViewHolder(BookViewHolder viewHolder, Book book, int position) {
                viewHolder.setTitle(book.getTitle());
                //viewHolder.setAtuthor(book.getAuthor());
                //viewHolder.setGenre(book.getGenre());
            }
        };

        mBookList.setAdapter(firebaseRecyclerAdapter);
    }


    public static  class BookViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public BookViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title){
            TextView nameTxt = (TextView)mView.findViewById(R.id.cv_bookTitle);
            nameTxt.setText(title);
        }

        /*
        public void setAtuthor(String author){
            TextView propTxt= (TextView) mView.findViewById(R.id.propellantTxt);
            propTxt.setText(author);
        }

        public void setGenre(String genre){
            TextView descTxt= (TextView) mView.findViewById(R.id.descTxt);
            descTxt.setText(genre);
        }
        */


    }

    public void showBookDetails(){
        //Toast.makeText(this, "clicked.", LENGTH_SHORT).show();
    }
}
