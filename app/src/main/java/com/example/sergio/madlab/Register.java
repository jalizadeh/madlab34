package com.example.sergio.madlab;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.example.sergio.madlab.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Register extends AppCompatActivity  implements View.OnClickListener {

    private static final String TAG_CREDENTIAL = "Credentials";
    private static final String TAG_ERROR = "Error";
    private EditText txt_email, txt_pass, txt_confirm, txt_name, txt_city;
    private Button reg_btn;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        //setSupportActionBar(toolbar);

        progressDialog=new ProgressDialog(this);



        //Views
        reg_btn=(Button)findViewById(R.id.btn_signup);
        txt_name = (EditText)findViewById(R.id.name);
        txt_email=(EditText)findViewById(R.id.email);
        txt_pass=(EditText)findViewById(R.id.password);
        txt_confirm=(EditText)findViewById(R.id.password2);
        txt_city=(EditText)findViewById(R.id.city);


        //
        reg_btn.setOnClickListener(this);

        //Firebase
        firebaseAuth=FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(txt_name.getText().toString());
        arrayList.add(txt_email.getText().toString());
        arrayList.add(txt_pass.getText().toString());
        arrayList.add(txt_confirm.getText().toString());
        arrayList.add(txt_city.getText().toString());
        outState.putStringArrayList(TAG_CREDENTIAL, arrayList);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> arrayList = savedInstanceState.getStringArrayList(TAG_CREDENTIAL);

        if (arrayList != null) {
            txt_name.setText(arrayList.get(0));
            txt_email.setText(arrayList.get(1));
            txt_pass.setText(arrayList.get(2));
            txt_confirm.setText(arrayList.get(3));
            txt_city.setText(arrayList.get(4));
        }
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_signup:
                registerUser(txt_email.getText().toString(),
                        txt_pass.getText().toString(),
                        txt_confirm.getText().toString(),
                        txt_name.getText().toString(),
                        txt_city.getText().toString());
                break;
        }
    }


    private void registerUser(final String email,
                              final String password,
                              final String confirm,
                              final String name,
                              final String city){
        if (controlStrings(email, password, confirm, name, city)) {
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    txt_pass.setError(getString(R.string.error_invalid_password));
                                    txt_pass.requestFocus();
                                } catch (FirebaseAuthInvalidUserException e){
                                    txt_email.setError(getString(R.string.error_invalid_email));
                                    txt_email.requestFocus();;
                                } catch (FirebaseNetworkException e){
                                    Toast.makeText(Register.this, "Network error", Toast.LENGTH_SHORT).show();
                                } catch(Exception e) {
                                    Toast.makeText(Register.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG_ERROR, e.getMessage());
                                }
                            } else{
                                firebaseAuth.signInWithEmailAndPassword(email, password);

                                User user = new User(name, email, city, "");
                                String userID = email.replace(",",",,").replace(".", ",");
                                db.child("users").child(userID).setValue(user);

                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Register.this);
                                alertDialog.setTitle(R.string.success);
                                alertDialog.setMessage(getString(R.string.register_success));
                                alertDialog.setPositiveButton(R.string.go_to_dashboard, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(new Intent(Register.this, MainActivity.class));
                                        finish();
                                    }
                                }).create().show();
                            }
                        }
                    });
        }

        /*
        String email=txt_email.getText().toString().trim();
        String password=txt_pass.getText().toString().trim();
        if (TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this,"Enter your Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this,"Enter Password",Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Registering User...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            Toast.makeText(Register.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                            Intent mainActivity=new Intent(Register.this, MainActivity.class);
                            startActivity(mainActivity);
                        }else {
                            Toast.makeText(Register.this, "Could not register... please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        */
    }



    private boolean controlStrings(String email, final String password, final String passRepeat, String name, String city){
        if (email.isEmpty() || password.isEmpty() || passRepeat.isEmpty() || name.isEmpty() || city.isEmpty()){
            if (city.isEmpty()){
                txt_city.setError(getString(R.string.error_invalid_name));
                txt_city.requestFocus();
            }
            if (name.isEmpty()){
                txt_name.setError(getString(R.string.error_invalid_name));
                txt_name.requestFocus();
            }
            if (passRepeat.isEmpty()){
                txt_confirm.setError(getString(R.string.error_invalid_repeat_password));
                txt_confirm.requestFocus();
            }
            if (password.isEmpty()){
                txt_pass.setError(getString(R.string.error_invalid_password));
                txt_pass.requestFocus();
            }
            if (email.isEmpty()){
                txt_email.setError(getString(R.string.error_invalid_email));
                txt_email.requestFocus();
            }
            return false;
        } else if (!password.equals(passRepeat)){
            txt_confirm.setError(getString(R.string.error_incorrect_password));
            txt_confirm.requestFocus();
            return false;
        }

        return true;
    }
}