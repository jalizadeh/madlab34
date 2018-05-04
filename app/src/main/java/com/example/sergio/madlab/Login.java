package com.example.sergio.madlab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
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
import android.util.Log;
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

    private static final String TAG_CREDENTIAL = "Credentials";
    private static final String TAG_ERROR = "Error";


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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(txt_email.getText().toString());
        arrayList.add(txt_password.getText().toString());
        outState.putStringArrayList(TAG_CREDENTIAL, arrayList);
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> arrayList = savedInstanceState.getStringArrayList(TAG_CREDENTIAL);
        if (arrayList != null) {
            txt_email.setText(arrayList.get(0));
            txt_password.setText(arrayList.get(1));
        }
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
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                txt_password.setError(getString(R.string.error_invalid_password));
                                txt_password.requestFocus();
                            } catch (FirebaseAuthInvalidUserException e){
                                txt_email.setError(getString(R.string.error_invalid_email));
                                txt_email.requestFocus();;
                            } catch (FirebaseNetworkException e){
                                Toast.makeText(Login.this, "Network error", Toast.LENGTH_SHORT).show();
                            } catch(Exception e) {
                                Toast.makeText(Login.this, "Some problem", Toast.LENGTH_SHORT).show();
                                Log.e(TAG_ERROR, e.getMessage());
                            }

                    }
                });
    }



    //@Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Login error. Maybe there is no connection",Toast.LENGTH_SHORT).show();
    }
}