package com.ltd.admin.appchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.ltd.admin.appchat.AllUserChat.AllUsersActivity;
import com.ltd.admin.appchat.Hardware.HardwareActivity;
import com.ltd.admin.appchat.Profile.PersonalPageActivity;
import com.ltd.admin.appchat.WelcomePage.StartPageActivity;

import static com.ltd.admin.appchat.R.menu.main_menu;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Toolbar toolbar;

    ViewPager mViewPager;
    TabLayout mTabLayout;
    TabsPagerAdapter mTabsPagerAdapter;
    FirebaseUser currentUser;
    private DatabaseReference UserReference;
    private DatabaseReference SetIdLogin;
    private String defaultIdLogin = "9999";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Anhxa();
    }
    public void Anhxa(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            String online_user_id = mAuth.getCurrentUser().getUid();
            UserReference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(online_user_id);

        }
        toolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AppChat");

        //Tao Adapter, set fragment vao Tablayout
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mViewPager.setAdapter(mTabsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_home_black_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_notifications_black_24dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_message_black_24dp);
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_contacts_black_24dp);
        SetIdLogin = FirebaseDatabase.getInstance().getReference().child("IdUserLogin");
    }



    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            //SetIdLogin.setValue(defaultIdLogin);
            LogoutUser();
        }else if (currentUser != null){
            UserReference.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null){
            UserReference.child("online").setValue(ServerValue.TIMESTAMP);
        }else {
            //SetIdLogin.setValue(defaultIdLogin);
        }

    }

    private void LogoutUser() {
        Intent mIntent = new Intent(MainActivity.this, StartPageActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout){
            // Set time khi user logout he thong
            if (currentUser != null){

                UserReference.child("online").setValue(ServerValue.TIMESTAMP);
                UserReference.child("user_login_status").setValue("false")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    SetIdLogin.setValue(defaultIdLogin);
                                    mAuth.signOut();
                                    LogoutUser();
                                }
                            }
                        });

            }

        }else if (item.getItemId() == R.id.main_account_setting){
            Intent mIntent = new Intent(MainActivity.this, PersonalPageActivity.class);
            startActivity(mIntent);
        }else if(item.getItemId() == R.id.main_all_user_button){
            Intent mIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(mIntent);
        }else if (item.getItemId() == R.id.main_device_setting){
            Intent mIntent = new Intent(MainActivity.this, HardwareActivity.class);
            startActivity(mIntent);
        }

        return  true;
    }
}
