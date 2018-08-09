package com.ltd.admin.appchat.VerifyAccount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ltd.admin.appchat.MainActivity;
import com.ltd.admin.appchat.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImagePassActivity extends AppCompatActivity {

    private Button btnLog, btnVerify;
    private CircleImageView imgDatabase, imgUser;

    final static int GALLERY_PICK = 1;
    Bitmap bitmapUser, bitmapDatabase;
    DatabaseReference getUserDataReference;
    FirebaseAuth mAuth;

    String image;
    String thumb;
    Bitmap thump_bitmap = null;
    ProgressDialog mDialogVerify, mProgressDialog;

    ByteArrayOutputStream stream;
    ImageAsynTask mImageAsynTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pass);
        Anhxa();

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                image = dataSnapshot.child("user_image").getValue().toString();
                thumb = dataSnapshot.child("user_thumb_image").getValue().toString();
                if(!image.equals("default_profile")){
                    functionCalledFromUIThread();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_GET_CONTENT);
                mIntent.setType("image/*");
                startActivityForResult(mIntent, GALLERY_PICK);
            }
        });
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageAsynTask = new ImageAsynTask();
                mImageAsynTask.execute();
            }
        });

    }
    public void Anhxa(){
        btnLog = (Button) findViewById(R.id.btn_image_pass);
        btnVerify = (Button) findViewById(R.id.btn_verify_img);

        imgDatabase = (CircleImageView) findViewById(R.id.img_default);
        imgUser = (CircleImageView) findViewById(R.id.img_default_user);
        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        btnVerify.setVisibility(View.INVISIBLE);
        imgDatabase.setVisibility(View.INVISIBLE);
        btnVerify.setEnabled(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode ==RESULT_OK && data != null){
            Uri ImageUri = data.getData();
            Picasso.with(ImagePassActivity.this).load(ImageUri).placeholder(R.drawable.default_image)
                    .into(imgUser);
            btnVerify.setVisibility(View.VISIBLE);
            btnVerify.setEnabled(true);
        }
    }

    //Load anh dai dien
    public void functionCalledFromUIThread(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Đang tải...");
        mProgressDialog.setTitle("Xin vui lòng chờ!");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
        Picasso.with(ImagePassActivity.this).load(thumb).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.default_image).
                into(imgDatabase ,new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onError() {

                        //Toast.makeText(PersonalPageActivity.this, "Kiểm tra kết nối!", Toast.LENGTH_SHORT).show();
                        Picasso.with(ImagePassActivity.this).load(thumb).placeholder(R.drawable.default_image)
                                .into(imgDatabase);
                        mProgressDialog.dismiss();
                    }
                });
    }

    public Bitmap convertImageViewToBitmap(ImageView v){
        Bitmap bm=((BitmapDrawable)v.getDrawable()).getBitmap();
        return bm;
    }
    // Ham so sanh hai anh bitmap
    public boolean CompareImages(Bitmap bitmap1, Bitmap bitmap2){
        if (bitmap1.getWidth() != bitmap2.getWidth() || bitmap2.getHeight() != bitmap2.getHeight()){
            return  false;
        }else{
            for( int y = 0 ; y < bitmap1.getHeight() ; y++){
                for( int x = 0 ; x < bitmap1.getWidth() ; x++){
                    if(bitmap1.getPixel(x,y) != bitmap2.getPixel(x,y)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public byte[] GetBitmapArray(Bitmap bitmap , int quality){
        stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    // Ham la gia tri RGB anh kich thuoc 256 x 256
    public void GetRGBValue256Bitmap(Bitmap bitmap, int [][] R, int [][] G, int [][] B){
        for( int y = 0 ; y < bitmap.getHeight() ; y++){
            for( int x = 0 ; x < bitmap.getWidth() ; x++){
                int pixel = bitmap.getPixel(x, y);
                R[y][x] = Color.red(pixel);
                G[y][x] = Color.green(pixel);
                B[y][x] = Color.blue(pixel);
                Log.d("ketqua", String.format("%d%d%d", R[y][x], G[y][x], B[y][x]));
            }
        }
    }

    public class ImageAsynTask extends AsyncTask<Void, Void, Void>{
        private boolean VerifyOk = true;
        @Override
        protected void onPreExecute() {
            mDialogVerify = ProgressDialog.show(ImagePassActivity.this, "Đang xác thực người dùng!", "Xin đợi!");
        }
        @Override
        protected Void doInBackground(Void... params) {
            try{
                bitmapUser = convertImageViewToBitmap(imgUser);
                bitmapDatabase = convertImageViewToBitmap(imgDatabase);

    //                byte [] ArrayImage = GetBitmapArray(bitmapDatabase,100);
    //                for ( byte b:ArrayImage){
    //                    Log.d("ketqua", String.format("0x%20x", b));
    //                }
                String h = Integer.toString(bitmapDatabase.getHeight());
                Log.d("mHeight", h);

                String w = Integer.toString(bitmapDatabase.getWidth());
                Log.d("mWidth", w);

                int [][] Red = new int [bitmapDatabase.getHeight()][bitmapDatabase.getWidth()];
                int [][] Green = new int [bitmapDatabase.getHeight()][bitmapDatabase.getWidth()];
                int [][] Blue = new int [bitmapDatabase.getHeight()][bitmapDatabase.getWidth()];

                //GetRGBValue256Bitmap(bitmapDatabase, Red, Green, Blue);
                //writeEx(Red, bitmapDatabase.getHeight(), bitmapDatabase.getWidth());

                if (CompareImages(bitmapUser, bitmapDatabase)){
                    VerifyOk = true;
                }else {
                    VerifyOk = false;
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!VerifyOk){
                Toast.makeText(ImagePassActivity.this, "Bạn không phải chủ tài khoản này!", Toast.LENGTH_SHORT).show();
                //Toast.makeText(ImagePassActivity.this, Integer.toString(bitmapDatabase.getPixel(10,10)), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(ImagePassActivity.this, "OK", Toast.LENGTH_SHORT).show();

                //Toast.makeText(ImagePassActivity.this, Integer.toString(bitmapDatabase.getPixel(10,10)), Toast.LENGTH_SHORT).show();
                Intent mIntent = new Intent(ImagePassActivity.this, MainActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mIntent);
                finish();
            }
            mDialogVerify.dismiss();
        }
    }

    public void writeEx( int [][] A, int rol, int col){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/AppChattest");
            if (!dir.exists()){
                dir.mkdir();
            }

            File file = new File(dir, "test.txt");
            String test = "abc";
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                for (int i = 0; i < rol; i ++){
                    for (int j =0 ; j < col; j ++){
                        String s = Integer.toString(A[i][j]) + "||";
                        fileOutputStream.write(s.getBytes());
                    }
                }
                fileOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            Toast.makeText(getApplicationContext(), "Err", Toast.LENGTH_SHORT).show();
        }

    }

    public void readEx(){
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/AppChattest");
        File file = new File(dir, "test.txt");
        String message;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((message =bufferedReader.readLine() )!= null){
                stringBuffer.append(message +"\n");
            }

            Log.d("txt", stringBuffer.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
