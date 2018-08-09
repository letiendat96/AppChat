package com.ltd.admin.appchat.Profile;



import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ltd.admin.appchat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class ProfileActivity extends AppCompatActivity {

    private Button btnRequest,declineRequest, btnSendMsg;
    private TextView txtName, txtStatus;
    private ImageView imgView, imgCoverPhoto;
    private DatabaseReference UserReference;
    private String receiver_user_id,  CURRENT_STATE, sender_user_id, imageThumb, coverPhoto;
    private DatabaseReference FriendRequestReference, FriendsReference, NotificationsReference;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Anhxa();

        UserReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                imageThumb = dataSnapshot.child("user_thumb_image").getValue().toString();
                coverPhoto = dataSnapshot.child("user_cover_photo").getValue().toString();

                txtName.setText(name);
                txtStatus.setText(status);

                if (!coverPhoto.equals("default_cover_photo" ) || !imageThumb.equals("default_profile")){
                    try {
                        mDialog.setMessage("Wait!");
                        mDialog.show();
                        LoadCoverPhoto();
                        functionCalledFromUIThread();
                        mDialog.dismiss();

                    }catch (Exception e){
                        Log.d("Errr", e.getMessage().toString());
                    }

                }


                FriendRequestReference.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(receiver_user_id)){
                                        String req_type = dataSnapshot.child(receiver_user_id).child("request_type")
                                                .getValue().toString();
                                        if(req_type.equals("sent")){
                                            CURRENT_STATE ="request_sent";
                                            btnRequest.setText("Hủy kết bạn");

                                            declineRequest.setVisibility(View.INVISIBLE);
                                            declineRequest.setEnabled(false);

                                        }else if (req_type.equals("receiver")){
                                            CURRENT_STATE = "request_received";
                                            btnRequest.setText("Chấp nhận kết bạn");

                                            declineRequest.setVisibility(View.VISIBLE);
                                            declineRequest.setEnabled(true);

                                            declineRequest.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    DeclineFriendRequest();
                                                }
                                            });
                                        }
                                    }

                                else {
                                    FriendsReference.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiver_user_id)){
                                                CURRENT_STATE = "friends";
                                                btnRequest.setText("Hủy kết bạn");

                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
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

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        declineRequest.setVisibility(View.INVISIBLE);
        declineRequest.setEnabled(false);


        // Khong gui yeu cau den me
        if (! sender_user_id.equals(receiver_user_id)){
            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnRequest.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestToPerson();
                    }else if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }else if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }else if(CURRENT_STATE.equals("friends")){
                        UnFriend();
                    }
                }
            });

        }else {
            btnRequest.setVisibility(View.INVISIBLE);
            declineRequest.setVisibility(View.INVISIBLE);
            btnSendMsg.setVisibility(View.INVISIBLE);
        }
        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void DeclineFriendRequest() {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                btnRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                btnRequest.setText("Kết bạn");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    //Huy ket ban sau khi da ket ban
    private void UnFriend() {
        FriendsReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendsReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                btnRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                btnRequest.setText("Kết bạn");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    public void Anhxa(){
        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();
        //Toast.makeText(this, receiver_user_id, Toast.LENGTH_SHORT).show();
        Log.d("userid", receiver_user_id);

        btnRequest = (Button) findViewById(R.id.btn_make_friend_request);
        declineRequest = (Button) findViewById(R.id.btn_cancel_friend_request);
        btnSendMsg = (Button) findViewById(R.id.btn_send_msg);

        txtName = (TextView) findViewById(R.id.txt_display_user_name);
        txtStatus = (TextView) findViewById(R.id.txt_display_user_status);

        imgView = (ImageView) findViewById(R.id.profile_visit_user_image);
        imgCoverPhoto = (ImageView) findViewById(R.id.img_cover_photo);

        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UserReference.keepSynced(true);
        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);

        CURRENT_STATE = "not_friends";
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        // Danh sach chap nhan ket ban
        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);

        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifictions");
        NotificationsReference.keepSynced(true);
        mDialog = new ProgressDialog(this);

    }
    //------------------------------------------------------------------------------------------
    // Optimized CURRENT_STATE
    private void SendFriendRequestToPerson(){
        FriendRequestReference.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("receiver")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                // Dung HashMap de them du lieu vao firebase
                                                // Random Key

                                                HashMap<String, String> notifications = new HashMap<String, String>();
                                                notifications.put("from", sender_user_id);
                                                notifications.put("type", "request");

                                                NotificationsReference.child(receiver_user_id).push().setValue(notifications)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()){
                                                                btnRequest.setEnabled(true);
                                                                CURRENT_STATE = "request_sent";
                                                                btnRequest.setText("Hủy kết bạn");

                                                                declineRequest.setVisibility(View.INVISIBLE);
                                                                declineRequest.setEnabled(false);
                                                            }
                                                        }
                                                    });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelFriendRequest(){
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                btnRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                btnRequest.setText("Kết bạn");
                                                declineRequest.setVisibility(View.INVISIBLE);
                                                declineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    public void AcceptFriendRequest() {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        final String date = df.format(Calendar.getInstance().getTime());
        Log.d("ketqua", date);

        FriendsReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(date)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            FriendsReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(date)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        btnRequest.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        btnRequest.setText("Hủy kết bạn");


                                                                                        declineRequest.setVisibility(View.INVISIBLE);
                                                                                        declineRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });

                        }
                    }
                });

    }

    private void functionCalledFromUIThread(){
        Picasso.with(ProfileActivity.this).load(imageThumb).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.default_image).into(imgView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(ProfileActivity.this).load(imageThumb).placeholder(R.drawable.default_image)
                        .into(imgView);
            }
        });

    }

    private void LoadCoverPhoto(){

        Picasso.with(ProfileActivity.this).load(coverPhoto).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.banner).into(imgCoverPhoto, new Callback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onError() {
                Picasso.with(ProfileActivity.this).load(coverPhoto).placeholder(R.drawable.banner)
                        .into(imgCoverPhoto);
            }
        });


    }
}
