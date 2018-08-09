package com.ltd.admin.appchat.RegisterAccount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.WelcomePage.StartPageActivity;
import com.ltd.admin.appchat.VerifyAccount.KeyOption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RegisterAuthActivity extends AppCompatActivity {

    Toolbar regToolbar;
    EditText  mCodeAuth;
    Button btnRegCode;
    FirebaseAuth mAuth;
    DatabaseReference mAuthCodeReference;
    ProgressDialog loading;

    private KeyOption mKeyOption;
    private static String mSalt = "123456789";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_auth);
        Anhxa();

        setSupportActionBar(regToolbar);
        getSupportActionBar().setTitle("REGISTER");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnRegCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterAuthCode(mCodeAuth.getText().toString());
            }
        });

    }
    public void Anhxa(){
        regToolbar = (Toolbar) findViewById(R.id.register_toolbar_auth_code);
        btnRegCode = (Button) findViewById(R.id.btn_register_auth_code);
        mCodeAuth = (EditText) findViewById(R.id.edit_text_auth);
        mAuth = FirebaseAuth.getInstance();
        loading = new ProgressDialog(this);
        mKeyOption = new KeyOption();
//        mSalt = Salt();
        //Toast.makeText(RegisterAuthActivity.this, mSalt , Toast.LENGTH_SHORT).show();
    }

    private void RegisterAuthCode( String authCode){
        if (TextUtils.isEmpty(authCode)){
            Toast.makeText(this, "Please enter a password!", Toast.LENGTH_SHORT).show();
        }else {

            loading.setTitle("Loading...");
            loading.setMessage("Wait!");
            loading.show();
            //Push authcode => firebase

            final String hashPass = mKeyOption.SHA256(authCode + mSalt);

            String current_user_id = mAuth.getCurrentUser().getUid();

            mAuthCodeReference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(current_user_id);
//          // Set salt
//            mAuthCodeReference.child("user_salt").setValue(mSalt);
            // Set pass
            mAuthCodeReference.child("user_verify_code").setValue(hashPass)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful() ){
                                Intent mIntent = new Intent(RegisterAuthActivity.this, StartPageActivity.class);
                                //mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mIntent);
                                finish();
                            }else {
                                Toast.makeText(RegisterAuthActivity.this, "Error! Check the connection.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


            loading.dismiss();
        }
    }

    // Write data on external
    // data - authcode encrypt present
    private boolean WriteEx(String data){
        boolean writedata = true;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File root = Environment.getExternalStorageDirectory();
            // Create if not exist folder
            File dir = new File(root.getAbsolutePath() + "/AppChattest");
            if (!dir.exists()){
                dir.mkdir();
            }
            File file  = new File(dir, "log_pass.txt");
            try{
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(data.getBytes());
                writedata = true;
            }catch (FileNotFoundException e){
                writedata = false;
                e.printStackTrace();
            }catch (IOException e){
                writedata = false;
                e.printStackTrace();
            }

        }else {
            Toast.makeText(getApplicationContext(), "Cannot log file", Toast.LENGTH_SHORT).show();
            writedata = false;
        }
       return writedata;
    }
//    private String Salt(){
//        Random mRandom = new Random();
//        int a = 1000 + mRandom.nextInt(8000);
//        return Integer.toString(a);
//    }

}
