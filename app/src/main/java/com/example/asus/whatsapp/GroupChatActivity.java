package com.example.asus.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    //private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,GroupNameRef,GroupMessageKeyRef;

    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        //it stores/get the ###group name from the GroupsFragment.java class
        currentGroupName=getIntent().getExtras().get("groupName").toString();


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);





        //set Group name on the title bar....
        setTitle(currentGroupName);
        Toast.makeText(this, currentGroupName, Toast.LENGTH_LONG).show();
        
        InitializeFields();
        
        
        GetUserInfo();


        //save user message on to the DataBase
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

    }




    private void InitializeFields() {

        sendMessageButton=findViewById(R.id.send_message_button);
        userMessageInput=findViewById(R.id.input_group_message);
        displayTextMessages=findViewById(R.id.group_chat_text_display);
        mScrollView =findViewById(R.id.myScrollView);
    }


    private void GetUserInfo() {

        //we are getting currentUserId from this activity and currentUserInfo
        //from this method... if this user exists then we can go
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    currentUserName=dataSnapshot.child("name").getValue().toString();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        //Displayed all messages of a particular group

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){

                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){

                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    private void SaveMessageInfoToDatabase(){

        String message=userMessageInput.getText().toString();

        //For every message, it generates a ####unique key under the
        // Particular Group Name....
        String messageKEY=GroupNameRef.push().getKey();


        if(TextUtils.isEmpty(message)){

            Toast.makeText(this, "Please write the message!", Toast.LENGTH_SHORT).show();
        }
        else {

            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentDate=currentDateFormat.format(calForDate.getTime());

            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(calForTime.getTime());


            //No need that line... I don't know why he put ????
            //HashMap<String,Object> groupMessageKey=new HashMap<>();
            //GroupNameRef.updateChildren(groupMessageKey);


            GroupMessageKeyRef=GroupNameRef.child(messageKEY);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name",currentUserName);
                messageInfoMap.put("message",message);
                messageInfoMap.put("date",currentDate);
                messageInfoMap.put("time",currentTime);

            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }



    public void DisplayMessages(DataSnapshot dataSnapshot){

        Iterator iterator= (Iterator) dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){

            String chatDate= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime= (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" +chatTime + "    " + chatDate +"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }




}
