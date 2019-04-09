package com.example.asus.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,senderUserId,Current_State;

    private CircleImageView userProfileImage;
    private TextView userProfileName,userPofileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");

        mAuth=FirebaseAuth.getInstance();



        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        //Toast.makeText(this, "User Id :"+ receiverUserId, Toast.LENGTH_SHORT).show();
        senderUserId=mAuth.getCurrentUser().getUid().toString();


        userProfileImage=findViewById( R.id.visit_profile_image);
        userProfileName=findViewById( R.id.visit_user_name);
        userPofileStatus=findViewById( R.id.visit_profile_status);
        SendMessageRequestButton=findViewById( R.id.send_message_request_button);
        DeclineMessageRequestButton=findViewById( R.id.decline_message_request_button);



        Current_State="new";

        RetriveUserInfo();
    }

    private void RetriveUserInfo() {

        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if( (dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){

                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                    userProfileName.setText(userName);
                    userPofileStatus.setText(userStatus);

                    ManageChatRequests();

                }
                else {

                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userPofileStatus.setText(userStatus);

                   ManageChatRequests();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //just after clicking someone's profile it would call ...
    private void ManageChatRequests() {


        //1.2
        ChatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(receiverUserId)){

                            String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){

                                Current_State="request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");

                            }

                            else if(request_type.equals("received")){

                                Current_State="request_received";
                                SendMessageRequestButton.setText("Accept Chat Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        CancelChatRequest();
                                    }
                                });
                            }

                        }

                        else {

                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild(receiverUserId)){

                                                Current_State="friends";
                                                SendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




        // 1.1
        //We get senderUserId from this Activity and
        //get receiverUserId from Intent.getExtra()....
        //(That means previous Intent)
        if(!senderUserId.equals(receiverUserId)){

            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //after clicking SendMessageRequestButton is set False...
                    SendMessageRequestButton.setEnabled(false);

                    if(Current_State.equals("new")){

                        SendChatRequest();
                    }

                    if(Current_State.equals("request_sent")){

                        CancelChatRequest();
                    }
                    if(Current_State.equals("request_received")){

                        AcceptChatRequest();

                    }
                    if(Current_State.equals("friends")){

                        RemoveSpecificContact();
                    }
                }



            });
        }

        else {

            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }



    public void AcceptChatRequest(){

        ContactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {

                        ContactsRef.child(receiverUserId).child(senderUserId)
                                .child("Contacts").setValue("Saved")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(Task<Void> task) {

                                        //As we saved Contact in our Contacts DB (node), so now we can remove
                                        //data from Chat request
                                        ChatRequestRef.child(senderUserId).child(receiverUserId)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            ChatRequestRef.child(receiverUserId).child(senderUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(Task<Void> task) {

                                                                            if(task.isSuccessful()) {

                                                                                SendMessageRequestButton.setEnabled(true);
                                                                                Current_State = "friends";
                                                                                SendMessageRequestButton.setText("Remove this Contact");

                                                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                DeclineMessageRequestButton.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    }
                                                });

                                    }
                                });

                    }
                });
    }


    private void CancelChatRequest(){

        // Both of the person (sender and receiver, we will make sure change the status of DB )
        ChatRequestRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {


                        if(task.isSuccessful()){

                            // Both of the person (sender and receiver, we will make sure change the status of DB )
                            ChatRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {

                                            if(task.isSuccessful()){

                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });


    }


    private void SendChatRequest() {


        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {

                        if(task.isSuccessful()){

                            ChatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {

                                            if(task.isSuccessful()){


                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="request_sent";
                                                SendMessageRequestButton.setText("Cancel Chat Request");


                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void RemoveSpecificContact() {

        //This function is same as CancelChatRequest()
        //just remove ContactsRef variable instead of ChatRequestRef variable.

        // Both of the person (sender and receiver, we will make sure change the status of DB )
        ContactsRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {


                        if(task.isSuccessful()){

                            // Both of the person (sender and receiver, we will make sure change the status of DB )
                            ContactsRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {

                                            if(task.isSuccessful()){

                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });


    }
}
