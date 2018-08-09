package com.ltd.admin.appchat.RegisterAccount;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.VerifyAccount.KeyOption;

public class RegisterActivity extends AppCompatActivity {

    Toolbar regToolbar;
    DatabaseReference storeDatabaseReference;
    DatabaseReference idUser;
    EditText mName, mEmail ;
    Button btnReg;
    FirebaseAuth mAuth;
    ProgressDialog loading;
    KeyOption mKeyOption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Anhxa();
        setSupportActionBar(regToolbar);
        getSupportActionBar().setTitle("REGISTER");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String pass = mEmail.getText().toString();
                try{
                    RegisterAccount(name, email, pass);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void Anhxa(){
        regToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        mName = (EditText) findViewById(R.id.edit_text_name_id);
        mEmail = (EditText) findViewById(R.id.edit_text_email_id);

        btnReg = (Button) findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        loading = new ProgressDialog(this);
        mKeyOption = new KeyOption();

    }

    private void RegisterAccount(final String name, String email, String pass){
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
        }
        else{
            loading.setTitle("Creating account");
            loading.setMessage("Wait! ");
            loading.show();

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                // Tao account => tao thong tin nguoi dung
                                String current_user_id = mAuth.getCurrentUser().getUid();
                                String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                                // Register id to receive key
                                idUser = FirebaseDatabase.getInstance().getReference().child("IdUser");
                                idUser.setValue(current_user_id);

                                storeDatabaseReference = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child(current_user_id);
                                storeDatabaseReference.child("user_name").setValue(name);
                                storeDatabaseReference.child("user_status").setValue("appChat");
                                storeDatabaseReference.child("user_image").setValue("default_profile");
                                storeDatabaseReference.child("user_key").setValue("default_key");
                                storeDatabaseReference.child("server_public_key").setValue("default_server_public_key");
                                storeDatabaseReference.child("device_token").setValue(DeviceToken);
                                storeDatabaseReference.child("user_login_status").setValue("true");
                                storeDatabaseReference.child("user_cover_photo").setValue("default_cover_photo");
                                storeDatabaseReference.child("user_thumb_image")
                                        .setValue("default_image").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent mIntent = new Intent(RegisterActivity.this, RegisterAuthActivity.class);
                                            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mIntent);
                                            finish();
                                        }
                                    }
                                });

                            }else {
                                Toast.makeText(RegisterActivity.this, "Unable to register account! Check the connection.", Toast.LENGTH_SHORT).show();
                            }
                            loading.dismiss();
                        }
                    });
        }
    }
}
