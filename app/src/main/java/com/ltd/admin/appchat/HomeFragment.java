package com.ltd.admin.appchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ltd.admin.appchat.AllPosts.Posts;
import com.ltd.admin.appchat.Comment.CommentActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Admin on 5/19/2018.
 */

public class HomeFragment extends Fragment{

    private View mView;
    private RecyclerView mHomeList;
    public DatabaseReference PostRef, LikeRef, getUserDataReference;
    private boolean likeCheck;
    private FirebaseAuth mAuth;
    public String mFirebaseUser, thumbProfile;
    SwipeRefreshLayout swipeRefreshLayoutHome;

    public HomeFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup con
                              tainer, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_home, container, false);
        mHomeList = (RecyclerView) mView.findViewById(R.id.home_list);
        mHomeList.setHasFixedSize(true);
        swipeRefreshLayoutHome = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_home);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mHomeList.setLayoutManager(linearLayoutManager);

        PostRef = FirebaseDatabase.getInstance().getReference().child("AllPost");
        PostRef.keepSynced(true);

        LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikeRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser().getUid();

        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mFirebaseUser);
        getUserDataReference.keepSynced(true);
        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                thumbProfile = dataSnapshot.child("user_thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //===============================================
        //Load all post
        try{
            Thread.sleep(1000);
            LoadAllPost();
        }catch (Exception e){
            Log.d("Errr", e.getMessage().toString());
        }
        //=================================================
        // Refreshlayout
        swipeRefreshLayoutHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RefreshLayout();
            }
        });
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void RefreshLayout(){
        try{
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoadAllPost();
                    swipeRefreshLayoutHome.setRefreshing(false);
                }
            }, 1000);
        }catch (Exception e){
            Log.d("Errr", e.getMessage().toString());
        }
    }

    public void LoadAllPost(){

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, HomeFragment.PostsViewHolder>(
                        Posts.class,
                        R.layout.all_posts_layout,
                        HomeFragment.PostsViewHolder.class,
                        PostRef
                ) {
                    @Override
                    protected void populateViewHolder(final HomeFragment.PostsViewHolder viewHolder, Posts model, int position) {

                        final String PostKey = getRef(position).getKey();
                        Log.d("postkey", PostKey);

                        PostRef.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                viewHolder.setName(dataSnapshot.child("name").getValue().toString());
                                viewHolder.setTime(dataSnapshot.child("time").getValue().toString());
                                viewHolder.setDate(dataSnapshot.child("date").getValue().toString());
                                viewHolder.setDesCription(dataSnapshot.child("description").getValue().toString());

                                viewHolder.setProfileImage(getContext(), dataSnapshot.child("profile_image").getValue().toString());

                                try{
                                    String imageLink = dataSnapshot.child("post_image").getValue().toString();
                                    //Log.d("err_link", imageLink);
                                    if (imageLink.equals("image")){
                                        viewHolder.setDefaultPostImage(getContext());

                                    }else {
                                        viewHolder.setPostImage(getContext(), imageLink);
                                    }
                                }catch (Exception e){

                                }

                                viewHolder.setLikeButtonStatus(PostKey);
                                viewHolder.setCommentButton(PostKey);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        viewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                likeCheck = true;
                                LikeRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (likeCheck == true){
                                            if (dataSnapshot.child(PostKey).hasChild(mFirebaseUser)){
                                                LikeRef.child(PostKey).child(mFirebaseUser).removeValue();
                                                likeCheck = false;
                                            }else{
                                                LikeRef.child(PostKey).child(mFirebaseUser).setValue(true);
                                                likeCheck = false;
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Chinh sua comment
                                Toast.makeText(getContext(), PostKey , Toast.LENGTH_SHORT).show();

                            }
                        });

                        viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent mIntent = new Intent(getContext(), CommentActivity.class);
                                mIntent.putExtra("PostKey", PostKey);
                                startActivity(mIntent);
                            }
                        });
                    }
                };

        mHomeList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ImageButton btnLike, btnComment;
        public TextView numLike, numComment;
        int countLike, countComment;
        String currentUserId;
        DatabaseReference LikeRef, CommentRef;
        ImageView postImage;


        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            btnLike = (ImageButton) mView.findViewById(R.id.btn_like);
            btnComment = (ImageButton) mView.findViewById(R.id.btn_comment);
            numLike = (TextView) mView.findViewById(R.id.txt_like_count);
            numComment = (TextView) mView.findViewById(R.id.txt_comment_count);
            postImage = (ImageView) mView.findViewById(R.id.post_image);

            LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            CommentRef = FirebaseDatabase.getInstance().getReference().child("AllPost");

        }
        // Count num Like
        public void setLikeButtonStatus( final  String postKey){
            LikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countLike = (int) dataSnapshot.child(postKey).getChildrenCount();
                        btnLike.setImageResource(R.drawable.like);
                        numLike.setText(Integer.toString(countLike) + " Likes");
                    }else{
                        countLike = (int) dataSnapshot.child(postKey).getChildrenCount();
                        btnLike.setImageResource(R.drawable.dislike);
                        if (countLike == 0){
                            numLike.setText("Likes");
                        }else {
                            numLike.setText(Integer.toString(countLike) + " Likes");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Count comment
        public void setCommentButton(final String postKey){
            CommentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild("Comments")){
                        countComment = (int)dataSnapshot.child(postKey).child("Comments").getChildrenCount();
                        numComment.setText(Integer.toString(countComment) + " Comments");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setName( String name){
            TextView userName = (TextView) mView.findViewById(R.id.post_user_name);
            userName.setText(name);
        }

        public void setTime( String time){
            TextView userTime = (TextView) mView.findViewById(R.id.post_time);
            userTime.setText(time);
        }

        public void setDate( String date){
            TextView userDate = (TextView) mView.findViewById(R.id.post_date);
            userDate.setText(date);
        }

        public void setDesCription( String description){
            TextView userDes = (TextView) mView.findViewById(R.id.post_description);
            userDes.setText(description);
        }

        public void setProfileImage( final Context mContext, final String user_thumb_image){

            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.post_profile_image);

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
        public void setPostImage(final Context mContext,  final String post_image){

            final ImageView user_post_image = (ImageView) mView.findViewById(R.id.post_image);

            Picasso.with(mContext).load(post_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                    .into(user_post_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }
                        @Override
                        public void onError() {
                            Picasso.with(mContext).load(post_image).placeholder(R.drawable.default_image).into(user_post_image);
                        }
                    });
        }

        public void setDefaultPostImage(final Context mContext){
            final ImageView user_post_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(mContext).load(R.drawable.post_default).into(user_post_image);
        }
    }

}
