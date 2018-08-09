package com.ltd.admin.appchat.Comment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class CommentActivity extends AppCompatActivity {

    TextView txtLikeComment;
    ImageButton imgBtnLikeComment;
    ImageView sendComment, emojiComment;
    EmojiconEditText emojiconEditComment;
    RecyclerView CommentList;
    private String Post_Key, current_id;
    private FirebaseAuth mAuth;

    private EmojIconActions emojIcon;
    private View rootViewComment;
    private DatabaseReference UserRef, PostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Anhxa();
        Post_Key = getIntent().getExtras().get("PostKey").toString();

        PostRef = FirebaseDatabase.getInstance().getReference().child("AllPost").child(Post_Key).child("Comments");
        PostRef.keepSynced(true);

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserRef.child(current_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("user_name").getValue().toString();
                            String user_profile_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                            SendComment(userName, user_profile_image);
                            emojiconEditComment.setText("");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    public void Anhxa(){
        rootViewComment = findViewById(R.id.root_view_comment);

        CommentList = (RecyclerView) findViewById(R.id.comment_list);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);

        txtLikeComment = (TextView) findViewById(R.id.txt_like_comment);
        imgBtnLikeComment = (ImageButton) findViewById(R.id.img_btn_like_comment);
        sendComment = (ImageView) findViewById(R.id.img_send_comment);
        emojiComment = (ImageView) findViewById(R.id.emoji_send_comment);

        emojiconEditComment = (EmojiconEditText) findViewById(R.id.edt_send_comment);

        emojIcon = new EmojIconActions(getApplicationContext(), rootViewComment, emojiconEditComment, emojiComment);
        emojIcon.ShowEmojIcon();

        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });

        emojiconEditComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });

        emojiconEditComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emojiconEditComment.getText().toString().length() == 0){
                    sendComment.setImageResource(R.drawable.ic_send_black_24dp);
                }else {
                    sendComment.setImageResource(R.drawable.ic_send_blue_24dp);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        current_id = mAuth.getCurrentUser().getUid();

    }

    private void SendComment(String username, String user_profile_image){

        String commentText = emojiconEditComment.getText().toString();
        if (! TextUtils.isEmpty(commentText)){

            DateFormat df = new SimpleDateFormat("d MMM yyyy");
            final String saveCurrentDate = df.format(Calendar.getInstance().getTime());

            DateFormat time = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = time.format(Calendar.getInstance().getTime());

            final  String RandomKey = current_id + saveCurrentDate + saveCurrentTime ;

            HashMap commentMap = new HashMap();
            commentMap.put("uid", current_id);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("username", username);
            commentMap.put("user_profile_image", user_profile_image);

            PostRef.child(RandomKey).updateChildren(commentMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(CommentActivity.this, "Commented", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(CommentActivity.this, "Err", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(
                Comments.class,
                R.layout.all_comment_layout,
                CommentsViewHolder.class,
                PostRef

        ) {
            @Override
            protected void populateViewHolder(final CommentsViewHolder viewHolder, Comments model, int position) {

                PostRef.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            viewHolder.setComment(dataSnapshot.child("username").getValue().toString(), dataSnapshot.child("comment").getValue().toString());
                            viewHolder.setTime(dataSnapshot.child("date").getValue().toString(), dataSnapshot.child("time").getValue().toString());
                            if (!dataSnapshot.child("user_profile_image").getValue().toString().equals("default_image")){
                                viewHolder.setProfileImage(getApplicationContext(), dataSnapshot.child("user_profile_image").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        CommentList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public CommentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTime(String time, String date){
            TextView timeComment = (TextView) mView.findViewById(R.id.txt_time_comment);
            timeComment.setText(time + " " + date);

        }

        public void setComment(String userName, String comment){
            TextView userComment = (TextView) mView.findViewById(R.id.txt_comment);
            userComment.setText("@"+userName + "\n" + comment);
        }

        public void setProfileImage(final Context mContext, final String user_thumb_image){

            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.comment_profile_image);

            Picasso.with(mContext).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }
                        @Override
                        public void onError() {
                            Picasso.with(mContext).load(user_thumb_image).placeholder(R.drawable.default_image).into(thumb_image);
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
