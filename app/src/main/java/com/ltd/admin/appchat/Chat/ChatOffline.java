package com.ltd.admin.appchat.Chat;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Admin on 4/15/2018.
 */

public class ChatOffline extends Application{

    private DatabaseReference UsersReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Load picture Picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        Anhxa();

        if (currentUser != null){
            String online_user_id = mAuth.getCurrentUser().getUid();
            UsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);

            UsersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UsersReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    //UsersReference.child("online").setValue(true);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void Anhxa(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }
}
