package com.example.asus.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //private FirebaseUser currentUser;
    private Button logInButton, phoneLogInButton;
    private EditText userEmail, userPassword;
    private TextView needNewAccountLink, forgetPasswordLink;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Change text color of EditText
        //SpannableStringBuilder ssb = new SpannableStringBuilder(needNewAccountLink.getText().toString().trim());
        //ForegroundColorSpan fcsBlue= new ForegroundColorSpan(Color.BLUE);
        //ssb.setSpan(fcsBlue, 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //needNewAccountLink.setTextColor(ContextCompat.getColorStateList(this, R.color.colorBlue));
        //needNewAccountLink.setTextColor(getResources().getColor(R.color.colorBlue, getResources().newTheme()));




        //4.1
        InitializeFields();

        //4.2/click on the Need New Account Link
        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });


        //6.1/
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });


        phoneLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent=new Intent(LoginActivity.this,phoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });

        mAuth=FirebaseAuth.getInstance();


        //As this activity has a Registered User.So, There is a pretty much sure ,we have a Current User.
        //currentUser=mAuth.getCurrentUser();
    }

    private void AllowUserToLogin(){

        loadingBar.setTitle("Sign In");
        loadingBar.setMessage("Please wait, while we Loading for you!");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your Email.",Toast.LENGTH_LONG);
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your Password.",Toast.LENGTH_LONG);
        }
        else {
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Logged in Successfully!", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                            else {
                                String message=task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error : "+message,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();

                            }
                        }
                    });
        }
    }




    //3.3/if users have already login then send it to Main Activity
    // its not needed anymore. Because ##currentUser validation have already exist on the Main Activity.
    /* protected void onStart() {
        super.onStart();

        if(currentUser != null){
            SendUserToMainActivity();
        }
    }*/


    //3.4/
    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);

        //when user have reached at MainActivity , we don't want to back again from Main to
        //Register Activity.. So we have to stop it by restricting backward.
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void InitializeFields() {

        logInButton=findViewById(R.id.login_button);
        phoneLogInButton=findViewById(R.id.phone_login_button);
        userEmail=findViewById(R.id.login_email);
        userPassword=findViewById(R.id.login_password);
        needNewAccountLink=findViewById(R.id.need_new_account_link);
        forgetPasswordLink=findViewById(R.id.forget_password_link);

        loadingBar=new ProgressDialog(this);


    }


    private void SendUserToRegisterActivity() {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }



}
