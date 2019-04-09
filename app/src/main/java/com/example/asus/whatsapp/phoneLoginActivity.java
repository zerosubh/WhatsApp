package com.example.asus.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class phoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton,verifyButton;
    private EditText inputPhoneNumber,inputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth=FirebaseAuth.getInstance();


        sendVerificationCodeButton=findViewById(R.id.send_ver_code_button);
        verifyButton=findViewById(R.id.verify_button);
        inputPhoneNumber=findViewById(R.id.phone_number_input);
        inputVerificationCode=findViewById(R.id.verification_code_input);
        loadingBar=new ProgressDialog(this);

        // ***********1
        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String phoneNumber=inputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber)){

                    Toast.makeText(phoneLoginActivity.this, "Please type your phone number first..", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait, while we are authenticating your phone..");

                    //if user touch on the screen then loading is not disappeared..
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();


                    // **********************1.1
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,                                  // Phone number to verify
                            60,                                        // Timeout duration
                            TimeUnit.SECONDS,                             // Unit of timeout
                            phoneLoginActivity.this,               // Activity (for callback binding)
                            callBacks);                                   // OnVerificationStateChangedCallbacks


                }
            }
        });


        // **********************2
        callBacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override

            //if verification is completed
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            //if something is wrong ..
            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(phoneLoginActivity.this, "Please enter the valid phone number with your country code..", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);

            }


            //**********************2.1
            //This method is called when code is being sent for user's phone.....
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                loadingBar.dismiss();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(phoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);

            }
        };


        //***************** 3
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode=inputVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){

                    Toast.makeText(phoneLoginActivity.this, "please write first..", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, while we are verifying Verification code..");

                    //if user touch on the screen then loading is not disappeared..
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    //*************** 3.1
                    //After entering verification code from his phone ....
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);

                    signInWithPhoneAuthCredential(credential);
                }
            }
        });



    }


    //************************ 4
    //All we have done by reading of the documentation
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            loadingBar.dismiss();
                            SendUSerToMainActivity();
                            Toast.makeText(phoneLoginActivity.this, "Congratulations, you're logged in Successfully!", Toast.LENGTH_SHORT).show();


                        }
                        else {
                            String message=task.getException().toString();
                            Toast.makeText(phoneLoginActivity.this, "Error : "+message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void SendUSerToMainActivity(){

        Intent loginIntent= new Intent(phoneLoginActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
