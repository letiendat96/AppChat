package com.ltd.admin.appchat.Profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ltd.admin.appchat.Comment.CommentActivity;
import com.ltd.admin.appchat.AllPosts.Posts;
import com.ltd.admin.appchat.MainActivity;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.AllPosts.StatusActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class PersonalPageActivity extends AppCompatActivity {

    TextView txtUserName, txtUserStatus;
    Button btnChangeUserImage, btnChangeUserStatus;
    CircleImageView circleUserImageView;

    DatabaseReference getUserDataReference;
    FirebaseAuth mAuth;
    final static int GALLERY_PICK = 1;
    final static int COVER_PHOTO_PICK = 2;
    StorageReference storeProfileImage, storeCoverPhoto;
    ProgressDialog mDialog, mProgressDialog, mCoverPhotoDialog, mLoadCoverPhoto;
    String image , thumb, coverPhoto;
    Bitmap thump_bitmap = null;
    private StorageReference thumbImageRef;
    //==========================================================
    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    ImageButton imgMore, imgPhoto, imgEdit, imgActivity;
    private RecyclerView settingList;
    public DatabaseReference PostRef, LikeRef;
    private boolean likeCheck;
    public String mFirebaseUser;
    private ImageView imgCoverPhoto;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downLoadImage;
    //private SwipeRefreshLayout SwipeLayout;


    public PersonalPageActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_page);
        Anhxa();

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                image = dataSnapshot.child("user_image").getValue().toString();
                thumb = dataSnapshot.child("user_thumb_image").getValue().toString();
                coverPhoto = dataSnapshot.child("user_cover_photo").getValue().toString();

                //Cap nhat
                txtUserName.setText(name);
                txtUserStatus.setText(status);
                if (!coverPhoto.equals("default_cover_photo") || !image.equals("default_profile")){

                    mProgressDialog.setMessage("Đang tải...");
                    mProgressDialog.setTitle("Xin vui lòng chờ!");
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.show();
                    LoadCoverPhoto();
                    functionCalledFromUIThread();
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PersonalPageActivity.this, "Lỗi! 101", Toast.LENGTH_SHORT).show();
            }
        });
        try{
            Thread.sleep(1000);
            ShowPost();
        }catch (Exception e){
            Log.d("Errr", e.getMessage().toString());
        }

        circleUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_GET_CONTENT);
                mIntent.setType("image/*");
                startActivityForResult(mIntent, GALLERY_PICK);
            }
        });
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPopupMore();
            }
        });

//        SwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                RefreshLayout();
//            }
//        });

    }

    public void Anhxa(){
        txtUserName = (TextView) findViewById(R.id.txt_user_name);
        txtUserStatus = (TextView) findViewById(R.id.txt_user_status);

        circleUserImageView = (CircleImageView) findViewById(R.id.img_default);
        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);


        storeProfileImage = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        storeCoverPhoto = FirebaseStorage.getInstance().getReference().child("Cover_Photo_Images");

        mDialog = new ProgressDialog(this);
        mCoverPhotoDialog = new ProgressDialog(this);
        mLoadCoverPhoto = new ProgressDialog(this);
        mProgressDialog = new ProgressDialog(this);

        thumbImageRef = FirebaseStorage.getInstance().getReference().child("Thumb_Images");

        settingList = (RecyclerView) findViewById(R.id.layout_post);
        settingList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        settingList.setLayoutManager(linearLayoutManager);


        PostRef = FirebaseDatabase.getInstance().getReference().child("AllPost");
        PostRef.keepSynced(true);
        LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikeRef.keepSynced(true);
        mFirebaseUser = mAuth.getCurrentUser().getUid();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        ToolbarTextAppernce();

        imgMore = (ImageButton) findViewById(R.id.img_more);
        imgPhoto = (ImageButton) findViewById(R.id.img_photo);
        imgEdit = (ImageButton) findViewById(R.id.img_edit);
        imgActivity = (ImageButton) findViewById(R.id.img_activity);
        imgCoverPhoto = (ImageView) findViewById(R.id.img_cover_photo);
        //SwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

    }

    private void ToolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
    }
    // Hien thi icon PopupMenu
    private void ShowPopupMore(){
        PopupMenu popupMenu = new PopupMenu(this, imgMore);
        try{
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field: fields){
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }

            }
        }catch (Exception e){
            Log.d("Err", e.getMessage().toString());
        }
        popupMenu.getMenuInflater().inflate(R.menu.menu_popup_more, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_add_post:
                        Intent mIntent = new Intent(PersonalPageActivity.this, StatusActivity.class);
                        startActivity(mIntent);
                        break;
                    case R.id.menu_change_profile_picture:
                        Intent mProfile = new Intent();
                        mProfile.setAction(Intent.ACTION_GET_CONTENT);
                        mProfile.setType("image/*");
                        startActivityForResult(mProfile, GALLERY_PICK);
                        break;
                    case R.id.menu_change_cover_photo:
                        Intent mCoverPhoto = new Intent();
                        mCoverPhoto.setAction(Intent.ACTION_GET_CONTENT);
                        mCoverPhoto.setType("image/*");
                        startActivityForResult(mCoverPhoto, COVER_PHOTO_PICK);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }else if (requestCode == COVER_PHOTO_PICK && resultCode == RESULT_OK && data != null){
            mCoverPhotoDialog.setTitle("Đang cập nhật!");
            mCoverPhotoDialog.setMessage("Xin đợi!");
            mCoverPhotoDialog.show();

            Uri CoverPhotoUri = data.getData();

            Picasso.with(PersonalPageActivity.this).load(CoverPhotoUri).placeholder(R.drawable.banner)
                    .into(imgCoverPhoto);

            DateFormat df = new SimpleDateFormat("d MMM yyyy");
            saveCurrentDate = df.format(Calendar.getInstance().getTime());

            DateFormat time = new SimpleDateFormat("HH:mm");
            saveCurrentTime = time.format(Calendar.getInstance().getTime());

            postRandomName = saveCurrentDate + saveCurrentTime ;
            StorageReference filePath = storeCoverPhoto.child(CoverPhotoUri.getLastPathSegment() + postRandomName + ".jpg");
            filePath.putFile(CoverPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        downLoadImage = task.getResult().getDownloadUrl().toString();
                        // Cap nhat link cover photo vao User
                        getUserDataReference.child("user_cover_photo").setValue(downLoadImage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError.getMessage().toString() == null){
                                    Toast.makeText(PersonalPageActivity.this, "Đã cập nhật ảnh bìa!", Toast.LENGTH_SHORT).show();
                                    mCoverPhotoDialog.dismiss();

                                }else {
                                    Toast.makeText(PersonalPageActivity.this, "Err", Toast.LENGTH_SHORT).show();
                                    mCoverPhotoDialog.dismiss();
                                }
                            }
                        });

                    }else {
                        Toast.makeText(PersonalPageActivity.this, "Err + " + task.getException().toString() , Toast.LENGTH_SHORT).show();
                        mCoverPhotoDialog.dismiss();
                    }
                }
            });
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //Lay du lieu anh tra ve qua uri
                mDialog.setTitle("Đang cập nhật!");
                mDialog.setMessage("Xin vui lòng chờ!");
                mDialog.show();

                Uri resultUri = result.getUri();

                //Vao ra file get image uri
                File thumb_filePathUri = new File(resultUri.getPath());
                // Id User
                String user_id = mAuth.getCurrentUser().getUid();

                // Giam size kich thuoc anh  luu duoi dang Bitmap
                try{
                    thump_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxHeight(200)
                            .setQuality(50).compressToBitmap(thumb_filePathUri);

                }catch (IOException e){
                    e.printStackTrace();
                }

                // Ma hoa dang JPEG
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thump_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

                // Image => Byte => Upload byte
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();
                //--------------------------------------------------------------


                StorageReference filePath = storeProfileImage.child(user_id + ".jpg");
                final StorageReference thumb_filePath = thumbImageRef.child(user_id + ".jpg");

                //Upload file
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(PersonalPageActivity.this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();

                            final String downloadUri = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    final String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()){

                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image", downloadUri);
                                        update_user_data.put("user_thumb_image", thumb_downloadUrl);

                                        // Cap nhat vao database
                                        getUserDataReference.updateChildren(update_user_data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        // Tim kiem cap nhat anh dai dien vao bai dang
                                                        Query searchPost = PostRef.orderByChild("uid")
                                                                .startAt( mAuth.getCurrentUser().getUid()).endAt( mAuth.getCurrentUser().getUid() + "\uf8ff");
                                                        searchPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for( DataSnapshot mSnapshot: dataSnapshot.getChildren()){
                                                                    mSnapshot.getRef().child("profile_image").setValue(thumb_downloadUrl);
                                                                }
                                                                Toast.makeText(PersonalPageActivity.this, "Đã cập nhật ảnh! ", Toast.LENGTH_SHORT).show();
                                                                mDialog.dismiss();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                Toast.makeText(PersonalPageActivity.this, "Err", Toast.LENGTH_SHORT).show();
                                                                mDialog.dismiss();
                                                            }
                                                        });

                                                    }else{
                                                        Toast.makeText(PersonalPageActivity.this, "Err", Toast.LENGTH_SHORT).show();
                                                        mDialog.dismiss();
                                                    }
                                                }
                                            });
                                    }
                                }
                            });

                        }else {
                            Toast.makeText(PersonalPageActivity.this, "Lỗi! 101", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }
    }

    //Load anh dai dien
    public void functionCalledFromUIThread(){
        Picasso.with(PersonalPageActivity.this).load(thumb).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.default_image).
                into(circleUserImageView,new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onError() {

                //Toast.makeText(PersonalPageActivity.this, "Kiểm tra kết nối!", Toast.LENGTH_SHORT).show();
                Picasso.with(PersonalPageActivity.this).load(thumb).placeholder(R.drawable.default_image)
                        .into(circleUserImageView);
            }
        });
    }

    //Load anh bia
    public void LoadCoverPhoto(){

        Picasso.with(PersonalPageActivity.this).load(coverPhoto).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.banner).
                into(imgCoverPhoto,new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError() {

                        //Toast.makeText(PersonalPageActivity.this, "Kiểm tra kết nối!", Toast.LENGTH_SHORT).show();
                        Picasso.with(PersonalPageActivity.this).load(coverPhoto).placeholder(R.drawable.banner)
                                .into(imgCoverPhoto);
                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();
//        try {
//            Thread.sleep(500);
//            ShowPost();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            Log.d("Errr", e.getMessage().toString());
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

//    private void RefreshLayout(){
//        try{
//            Handler mHandler = new Handler();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ShowPost();
//                    SwipeLayout.setRefreshing(false);
//                }
//            }, 1000);
//        }catch (Exception e){
//            Log.d("Errr", e.getMessage().toString());
//        }
//    }

    private void ShowPost(){
        Log.d("Err_post", mAuth.getCurrentUser().getUid());
        // Tim kiem bai dang
        Query searchPost = PostRef.orderByChild("uid")
                .startAt( mAuth.getCurrentUser().getUid()).endAt( mAuth.getCurrentUser().getUid() + "\uf8ff");

        FirebaseRecyclerAdapter<Posts, PersonalPageActivity.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PersonalPageActivity.PostsViewHolder>(
                        Posts.class,
                        R.layout.all_posts_layout,
                        PersonalPageActivity.PostsViewHolder.class,
                        searchPost
                ) {
                    @Override
                    protected void populateViewHolder(final PersonalPageActivity.PostsViewHolder viewHolder, Posts model, int position) {

                        final String PostKey = getRef(position).getKey();
                        PostRef.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                viewHolder.setName(dataSnapshot.child("name").getValue().toString());
                                viewHolder.setTime(dataSnapshot.child("time").getValue().toString());
                                viewHolder.setDate(dataSnapshot.child("date").getValue().toString());
                                viewHolder.setDesCription(dataSnapshot.child("description").getValue().toString());
                                viewHolder.setProfileImage(getApplicationContext(), thumb);

                                try{
                                    String imageLink = dataSnapshot.child("post_image").getValue().toString();
                                    //Log.d("err_link", imageLink);
                                    if (imageLink.equals("image")){
                                        viewHolder.setDefaultPostImage(getApplicationContext());

                                    }else {
                                        viewHolder.setPostImage(getApplicationContext(), imageLink);
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
                                Toast.makeText(PersonalPageActivity.this, PostKey , Toast.LENGTH_SHORT).show();

                            }
                        });

                        viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent mIntent = new Intent(PersonalPageActivity.this, CommentActivity.class);
                                mIntent.putExtra("PostKey", PostKey);
                                startActivity(mIntent);
                            }
                        });
                    }
                };

        settingList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ImageButton btnLike, btnComment;
        public TextView numLike, numComment;
        int countLike, countComment;
        String currentUserId;
        DatabaseReference LikeRef, CommentRef;


        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            btnLike = (ImageButton) mView.findViewById(R.id.btn_like);
            btnComment = (ImageButton) mView.findViewById(R.id.btn_comment);
            numLike = (TextView) mView.findViewById(R.id.txt_like_count);
            numComment = (TextView) mView.findViewById(R.id.txt_comment_count);

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

        public void setProfileImage(final Context mContext, final String user_thumb_image){

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mIntent = new Intent(PersonalPageActivity.this, MainActivity.class);
        startActivity(mIntent);
        finish();
    }
}
