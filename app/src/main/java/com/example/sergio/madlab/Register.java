package com.example.sergio.madlab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText txt_email;
    private EditText txt_pass;
    private  Button reg_btn;
    //private  EditText txt_confirm_pass;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        //setSupportActionBar(toolbar);

        progressDialog=new ProgressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();
        reg_btn=(Button)findViewById(R.id.signup_activity);
        txt_email=(EditText)findViewById(R.id.email);
        txt_pass=(EditText)findViewById(R.id.password);
        //txt_confirm_pass=(EditText)findViewById(R.id.confirm_pass);
        reg_btn.setOnClickListener(this);

    }
    public void registerUser(){
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
    }

    @Override
    public void onClick(View v) {

        registerUser();

    }
}