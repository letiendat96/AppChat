package com.ltd.admin.appchat.WelcomePage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ltd.admin.appchat.LoginAccount.LoginActivity;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.RegisterAccount.RegisterActivity;

public class StartPageActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page_activity);
        Anhxa();

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(mIntent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(mIntent);
            }
        });

    }

    public void Anhxa(){
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnSign = (Button) findViewById(R.id.btn_sign);
    }
}
