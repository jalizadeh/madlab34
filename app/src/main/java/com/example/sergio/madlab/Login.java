package com.example.sergio.madlab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "content_login";
    public Button mBtLogin;
    public Button mBtSignup;
    private EditText txt_email;
    private EditText txt_password;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        //setSupportActionBar(toolbar);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            Intent mainActivity = new Intent(Login.this, MainActivity.class);
            startActivity(mainActivity);
        }

        progressDialog = new ProgressDialog(this);
        txt_email = (EditText) findViewById(R.id.email);
        txt_password = (EditText) findViewById(R.id.password);
        mBtLogin = (Button) findViewById(R.id.email_sign_in_button);
        mBtSignup = (Button) findViewById(R.id.email_sign_up_button);
        mBtLogin.setOnClickListener((View.OnClickListener) this);
        mBtSignup.setOnClickListener((View.OnClickListener) this);


    }

    @Override
    public void onClick(View v) {
        if (v == mBtLogin) {
            authenticateUser();
        }
        if (v == mBtSignup) {
            Intent signUp = new Intent(Login.this, Register.class);
            startActivity(signUp);
        }
    }

    private void authenticateUser() {
        String email = txt_email.getText().toString().trim();
        String password = txt_password.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Logging On");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(Login.this, "Log in Successful... ", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            finish();
                            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(mainActivity);
                        } else
                            Toast.makeText(getApplicationContext(), "Log in failed... please try again", Toast.LENGTH_SHORT).show();

                    }
                });
    }

}