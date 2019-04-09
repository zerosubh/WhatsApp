package com.example.asus.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;
    private ProgressDialog loadingBar;
    private static final int GalleryPick=1;

    private StorageReference userProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        rootref=FirebaseDatabase.getInstance().getReference();

        userProfileImagesRef=FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();
        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
        
        RetriveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //for Image select
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });

        
    }



    private void UpdateSettings() {

        String setUserName=userName.getText().toString();
        String setUserStatus=userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){

            Toast.makeText(this, "Please write your Username!", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){

            Toast.makeText(this, "Please write your Status!", Toast.LENGTH_SHORT).show();
        }
        else {

            //if user dont have username, then set the ##username
            HashMap<String,String> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setUserStatus);

            rootref.child("Users").child(currentUserId).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile updated Successfully!", Toast.LENGTH_SHORT).show();

                            }
                            else {
                                String message=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error :"+message, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }

    }


    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);

        //when user have reached at MainActivity , we don't want to back again from Main to
        //Register Activity.. So we have to stop it by restricting backwardly.
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void RetriveUserInfo() {

        rootref.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){

                            String retriveUsername=dataSnapshot.child("name").getValue().toString();
                            String retriveUserStatus=dataSnapshot.child("status").getValue().toString();
                            String retriveProfileImage=dataSnapshot.child("image").getValue().toString();

                            userName.setText(retriveUsername);
                            userStatus.setText(retriveUserStatus);

                            Picasso.get().load(retriveProfileImage).into(userProfileImage);

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) ){

                            String retriveUsername=dataSnapshot.child("name").getValue().toString();
                            String retriveUserStatus=dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUsername);
                            userStatus.setText(retriveUserStatus);
                        }
                        else{

                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please Set and Update your profile information!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void InitializeFields() {

        UpdateAccountSettings=findViewById(R.id.update_settings_button);
        userName=findViewById(R.id.set_user_name);
        userStatus=findViewById(R.id.set_profile_status);
        userProfileImage=findViewById(R.id.profile_image);
        loadingBar=new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode == RESULT_OK && data !=null){

            Uri ImageUri= data.getData();

            // Read the documentation ... https://github.com/ArthurHub/Android-Image-Cropper
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        //see documentation
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating..");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                //cropped image
                Uri resultUri = result.getUri();

                // you can store any type of file you want...
                final StorageReference filePath=userProfileImagesRef.child(currentUserId + ".jpg");
                

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                       filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {

                               final String downloadUri=uri.toString();

                               rootref.child("Users").child(currentUserId).child("image").setValue(downloadUri)
                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(Task<Void> task) {

                                               if(task.isSuccessful()){

                                                   Toast.makeText(SettingsActivity.this, "Image save in Database, Successfully!", Toast.LENGTH_SHORT).show();
                                                   loadingBar.dismiss();

                                               }
                                               else {

                                                   String message=task.getException().toString();
                                                   Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                                   loadingBar.dismiss();
                                               }
                                           }
                                       });

                           }
                       });

                    }
                });



               /* filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        
                        if(task.isSuccessful()){

                            Toast.makeText(SettingsActivity.this, "profile Image is successfully uploaded!", Toast.LENGTH_SHORT).show();

                            //final String downloadUri=task.getResult().getDown
                        }
                        else {

                            String message=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });*/

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        }


}
