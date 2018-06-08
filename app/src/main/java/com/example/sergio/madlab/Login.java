package com.example.sergio.madlab;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Login extends AppCompatActivity implements View.OnClickListener {

    public Button btnLogin, btnRegister;
    private EditText etEmail, etPassword;
    private String email, password;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        //if user already logged in, jump to MainActivity
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            Intent mainActivity = new Intent(Login.this, MainActivity.class);
            startActivity(mainActivity);
        }

        etEmail = (EditText) findViewById(R.id.email);
        etPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnSignin);
        btnRegister = (Button) findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener((View.OnClickListener) this);
        btnRegister.setOnClickListener((View.OnClickListener) this);


    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(etEmail.getText().toString());
        arrayList.add(etPassword.getText().toString());
        outState.putStringArrayList("email+pass", arrayList);
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        //if user already logged in, jump to MainActivity
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            Intent mainActivity = new Intent(Login.this, MainActivity.class);
            startActivity(mainActivity);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> arrayList = savedInstanceState.getStringArrayList("email+pass");
        if (arrayList != null) {
            etEmail.setText(arrayList.get(0));
            etPassword.setText(arrayList.get(1));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnLogin) {
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(Login.this, "Please wait...\nLogging in progress", Toast.LENGTH_SHORT).show();
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(Login.this, "Log in Successful... ", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                finish();
                                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(mainActivity);
                            } else
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e){
                                    etEmail.setError(getString(R.string.error_invalid_email));
                                    etEmail.requestFocus();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    etPassword.setError(getString(R.string.error_invalid_password));
                                    etPassword.requestFocus();
                                } catch (FirebaseNetworkException e){
                                    Toast.makeText(Login.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                                } catch(Exception e) {
                                    Toast.makeText(Login.this,"Please try again", Toast.LENGTH_SHORT).show();
                                }

                        }
                    });
        }
        if (v == btnRegister) {
            Intent register = new Intent(Login.this, Register.class);
            startActivity(register);
        }
    }



    //@Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,R.string.error_network,Toast.LENGTH_SHORT).show();
    }
}