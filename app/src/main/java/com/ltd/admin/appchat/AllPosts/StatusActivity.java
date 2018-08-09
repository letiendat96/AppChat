package com.ltd.admin.appchat.AllPosts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.ltd.admin.appchat.Profile.PersonalPageActivity;
import com.ltd.admin.appchat.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    Button btnUpdateStatus;
    EmojiconEditText emojiconEditText;
    ImageView emojiButton, filePost;
    ImageView submitImg, imgPost;
    View rootView;
    EmojIconActions emojIcon;
    DatabaseReference changeStatus, AllPostReference, UserRef;


    FirebaseAuth mAuth;
    ProgressDialog mDialog;

    private static final int GALLERY_PICK = 1;
    private static final int FILE_PICK = 1;
    private Uri ImageUri, FileUri;

    private StorageReference PostReference;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downLoadImage = "image";
    private String user_id;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Anhxa();

        emojiconEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });

        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newStatus = emojiconEditText.getText().toString();
                // Change status
                ChangeProfileStatus(newStatus);
                // Save post to database
                StoreImageFirebase();

            }
        });

        submitImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_GET_CONTENT);
                mIntent.addCategory(Intent.CATEGORY_OPENABLE);
                mIntent.setType("image/*");
                startActivityForResult(mIntent,GALLERY_PICK);
            }
        });

        filePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_GET_CONTENT);
                mIntent.addCategory(Intent.CATEGORY_OPENABLE);
                mIntent.setType("*/*");
                startActivityForResult(mIntent,GALLERY_PICK);
            }
        });

    }

    public void Anhxa(){
        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        btnUpdateStatus = (Button) findViewById(R.id.btn_update_status);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Your Page");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rootView = findViewById(R.id.root_view);
        emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        emojIcon = new EmojIconActions(StatusActivity.this, rootView, emojiconEditText, emojiButton);
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

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        changeStatus = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mDialog = new ProgressDialog(this);
        imgPost = (ImageView) findViewById(R.id.img_post);
        submitImg = (ImageView) findViewById(R.id.img_image);
        filePost = (ImageView) findViewById(R.id.img_attach_file);

        PostReference = FirebaseStorage.getInstance().getReference();
        AllPostReference = FirebaseDatabase.getInstance().getReference().child("AllPost");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        loadingBar = new ProgressDialog(this);

    }

    public void ChangeProfileStatus(String s){
        if (TextUtils.isEmpty(s)){
            Toast.makeText(this, " ...?", Toast.LENGTH_SHORT).show();
        }else {
            mDialog.setTitle("Đang cập nhật!");
            mDialog.setMessage("Xin vui lòng chờ!");
            mDialog.show();
            changeStatus.child("user_status").setValue(s)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(StatusActivity.this, "Đã cập nhật! ", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                        Intent mIntent = new Intent(StatusActivity.this, PersonalPageActivity.class);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mIntent);
                        finish();

                    }else {
                        Toast.makeText(StatusActivity.this, "Err", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Errr", e.getMessage().toString());
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            imgPost.setImageURI(ImageUri);

        }else if (requestCode == FILE_PICK && resultCode == RESULT_OK && data != null){

        }

    }

    // Save image to firebase
    private void StoreImageFirebase(){

        DateFormat df = new SimpleDateFormat("d MMM yyyy");
        saveCurrentDate = df.format(Calendar.getInstance().getTime());

        DateFormat time = new SimpleDateFormat("HH:mm");
        saveCurrentTime = time.format(Calendar.getInstance().getTime());

        postRandomName = saveCurrentDate + saveCurrentTime ;

        if (ImageUri != null){
            StorageReference filePath = PostReference.child("PostImages").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        downLoadImage = task.getResult().getDownloadUrl().toString();
                        SaveAllPost();
                        Toast.makeText(StatusActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();

                    }else {
                        Toast.makeText(StatusActivity.this, "Err" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            SaveAllPost();
        }
    }

    private void SaveAllPost(){
        UserRef.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String user_name = dataSnapshot.child("user_name").getValue().toString();
                    String user_profile_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);

                    postMap.put("description", emojiconEditText.getText().toString());
                    if (!downLoadImage.equals("image")){
                        postMap.put("post_image",downLoadImage);
                        downLoadImage = "image";
                    }else {
                        postMap.put("post_image","image");
                    }

                    postMap.put("uid", user_id);
                    postMap.put("profile_image", user_profile_image);
                    postMap.put("name", user_name);

                    AllPostReference.child( user_id + postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(StatusActivity.this, "Post updated", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(StatusActivity.this, task.getException().toString() , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
