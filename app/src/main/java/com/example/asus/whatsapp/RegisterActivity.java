package com.example.asus.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //4.3/
        InitializeFields();

        //5.2.******
        mAuth=FirebaseAuth.getInstance();
        // keep database references
        rootRef=FirebaseDatabase.getInstance().getReference();
        Log.d("rootRef : "," "+rootRef);

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        //5.1*******
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }

        });




    }


    private void CreateNewAccount() {

        final String email=userEmail.getText().toString();
        final String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your Email.",Toast.LENGTH_LONG);
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your Password.",Toast.LENGTH_LONG);
        }
        else {

            loadingBar.setTitle("Creating Mew Account");
            loadingBar.setMessage("Please wait, while we creating new Account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            // send mail/password to Firebase DB.
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){



                                String currentUserID=mAuth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currentUserID).setValue(email);
                                //rootRef.push().setValue(email);

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account Created Successfully!",Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }

                            else {
                                String message=task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error : "+message,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();

                            }

                        }
                    });
        }

    }


    private void InitializeFields() {

        createAccountButton=findViewById(R.id.register_button);
        userEmail=findViewById(R.id.register_email);
        userPassword=findViewById(R.id.register_password);
        AlreadyHaveAccountLink=findViewById(R.id.already_have_account_link);

        loadingBar=new ProgressDialog(this);

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);

        //when user have reached at MainActivity , we don't want to back again from Main to
        //Register Activity.. So we have to stop it by restricting backwardly.
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
