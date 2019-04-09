package com.example.asus.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {


    private RecyclerView findFriendsRecyclerViewList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");


        //set the Recycler view on to the Find_friends_layout
        findFriendsRecyclerViewList=findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerViewList.setLayoutManager(new LinearLayoutManager(this));

        setTitle("Find Friends");



    }


    @Override
    protected void onStart() {
        super.onStart();

        //binding db reference with model class..
        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UsersRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {

                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts contacts) {

                        holder.userName.setText(contacts.getName());
                        holder.userStatus.setText(contacts.getStatus());
                        Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);



                        //itemView is a parameter of FindFriendViewHolder class
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //when click on a particular row then it will return a profile's view
                                //corresponding with userID on that click..
                                //storing the key (getKey()) into the visit_user_id
                                String visit_user_id=getRef(position).getKey();

                                Intent profileIntent=new Intent(FindFriendsActivity.this,ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);

                        return viewHolder;
                    }
                };

        //data are adding into the RecyclerView
        findFriendsRecyclerViewList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

        }
    }
}
