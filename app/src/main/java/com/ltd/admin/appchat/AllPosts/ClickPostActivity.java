package com.ltd.admin.appchat.AllPosts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.ltd.admin.appchat.R;

public class ClickPostActivity extends AppCompatActivity {

    Button btnEditPost;
    ImageView imgEditPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
    }
}
