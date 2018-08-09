package com.ltd.admin.appchat.LoginAccount;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ltd.admin.appchat.MainActivity;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.VerifyAccount.KeyOption;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerifyPassActivity extends AppCompatActivity {

    TextView txtGetName, txtGetEmail, txtGetPass;
    CircleImageView mCircleImageView;
    DatabaseReference getUserDataReference, CipherReference;

    FirebaseAuth mAuth;
    Button btnLogin2;
    EditText edtPass;

    private String image, thumb, emailLogin;

    private static String userSalt = "123456789";
    String online_user_id = null;

    ProgressDialog mProgressDialog;
    KeyOption mKeyOption;

    private DatabaseReference SetIdLoginReferenceNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_pass);
        Anhxa();

        emailLogin = getIntent().getExtras().get("email_login").toString();
        txtGetEmail.setText(emailLogin);

        txtGetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               GetPassword();
            }
        });

        getUserDataReference.child(online_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                image = dataSnapshot.child("user_image").getValue().toString();
                thumb = dataSnapshot.child("user_thumb_image").getValue().toString();
//                userSalt = dataSnapshot.child("user_salt").getValue().toString();

                txtGetName.setText(name);
                if(!image.equals("default_profile")){
                    functionCalledFromUIThread();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnLogin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtPass.getText().toString())){
                    Toast.makeText(VerifyPassActivity.this, "You have not entered a password yet!", Toast.LENGTH_SHORT).show();

                }else{
                    String hashVerify = mKeyOption.SHA256(edtPass.getText().toString() + userSalt);
                    //Toast.makeText(VerifyPassActivity.this, hashVerify , Toast.LENGTH_SHORT).show();
                    getUserDataReference.child(online_user_id).child("user_hash_login").setValue(hashVerify);
                    SetIdLoginReferenceNext.setValue(online_user_id);
                    CipherReference.child(online_user_id).child("user_login_status")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue().toString().equals("true")){

                                        Intent mIntent = new Intent(VerifyPassActivity.this, MainActivity.class);
                                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mIntent);
                                        finish();

                                    }else {
                                       // Toast.makeText(VerifyPassActivity.this, "Err", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });
    }

    public void Anhxa(){

        mCircleImageView = (CircleImageView) findViewById(R.id.img_default_next);
        txtGetName = (TextView) findViewById(R.id.txt_input_name);
        txtGetEmail = (TextView) findViewById(R.id.txt_get_status);
        edtPass = (EditText) findViewById(R.id.edit_text_password_login_next);
        txtGetPass = (TextView) findViewById(R.id.txt_get_pass);
        btnLogin2 = (Button) findViewById(R.id.btn_login2_app);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users");
        getUserDataReference.keepSynced(true);

        mKeyOption = new KeyOption();

        //Toast.makeText(this, online_user_id , Toast.LENGTH_SHORT).show();
        SetIdLoginReferenceNext = FirebaseDatabase.getInstance().getReference().child("IdUserLogin");
        CipherReference = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    public void GetPassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(VerifyPassActivity.this);
        builder.setTitle("Send link to your email");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(VerifyPassActivity.this, "Sended", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void functionCalledFromUIThread(){

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setTitle("Wait!");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();

        Picasso.with(VerifyPassActivity.this).load(thumb).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.default_image).
                into(mCircleImageView,new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onError() {

                        //Toast.makeText(PersonalPageActivity.this, "Kiểm tra kết nối!", Toast.LENGTH_SHORT).show();
                        Picasso.with(VerifyPassActivity.this).load(thumb).placeholder(R.drawable.default_image)
                                .into(mCircleImageView);
                        mProgressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mIntent = new Intent(VerifyPassActivity.this, LoginActivity.class);
        startActivity(mIntent);

    }
}
