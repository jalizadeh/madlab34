package com.example.sergio.madlab;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Register extends AppCompatActivity  implements View.OnClickListener {

    private EditText etEmail, etPass, etConfirm, etName, etCity;
    private Button btnSignup;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Views
        btnSignup=(Button)findViewById(R.id.btnSignup);
        etName = (EditText)findViewById(R.id.name);
        etEmail=(EditText)findViewById(R.id.email);
        etPass=(EditText)findViewById(R.id.password);
        etConfirm=(EditText)findViewById(R.id.password2);
        etCity=(EditText)findViewById(R.id.city);


        //
        btnSignup.setOnClickListener(this);

        //Firebase
        firebaseAuth=FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(etName.getText().toString());
        arrayList.add(etEmail.getText().toString());
        arrayList.add(etPass.getText().toString());
        arrayList.add(etConfirm.getText().toString());
        arrayList.add(etCity.getText().toString());
        outState.putStringArrayList("user input", arrayList);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> arrayList = savedInstanceState.getStringArrayList("user input");

        if (arrayList != null) {
            etName.setText(arrayList.get(0));
            etEmail.setText(arrayList.get(1));
            etPass.setText(arrayList.get(2));
            etConfirm.setText(arrayList.get(3));
            etCity.setText(arrayList.get(4));
        }
    }


    private void registerUser(){
        final String name = etName.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPass.getText().toString();
        final String confirm = etConfirm.getText().toString();
        final String city = etCity.getText().toString();

        if (validateInputs(name, email, password, confirm, city)) {
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    etPass.setError(getString(R.string.error_invalid_password));
                                    etPass.requestFocus();
                                } catch (FirebaseAuthInvalidUserException e){
                                    etEmail.setError(getString(R.string.error_invalid_email));
                                    etEmail.requestFocus();;
                                } catch (FirebaseNetworkException e){
                                    Toast.makeText(Register.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                                } catch(Exception e) {
                                    Toast.makeText(Register.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            } else{
                                firebaseAuth.signInWithEmailAndPassword(email, password);
                                String uID = firebaseAuth.getUid();
                                User user = new User(uID, name, email, city, "");
                                String userID = email.replace(",",",,").replace(".", ",");
                                db.child("users").child(uID).setValue(user);

                                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(mainActivity);
                                finish();
                            }
                        }
                    });
        }

    }



    //check if all input values are valid
    private boolean validateInputs(String name, String email, String password, String passRepeat, String city){
        if (name.isEmpty()){
            etName.setError(getString(R.string.error_invalid_name));
            etName.requestFocus();
            return false;
        }
        if (email.isEmpty()){
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()){
            etPass.setError(getString(R.string.error_invalid_password));
            etPass.requestFocus();
            return false;
        }
        if (passRepeat.isEmpty()){
            etConfirm.setError(getString(R.string.error_invalid_repeat_password));
            etConfirm.requestFocus();
            return false;
        }
        if (city.isEmpty()){
            etCity.setError(getString(R.string.error_invalid_name));
            etCity.requestFocus();
            return false;
        }
        if (!password.equals(passRepeat)){
            etConfirm.setError(getString(R.string.error_incorrect_password));
            etConfirm.requestFocus();
            return false;
        }

        return true;
    }


    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSignup:
                registerUser();
                break;
        }
    }
}
