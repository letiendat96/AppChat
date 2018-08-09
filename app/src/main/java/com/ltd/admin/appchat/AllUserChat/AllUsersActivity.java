package com.ltd.admin.appchat.AllUserChat;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ltd.admin.appchat.Profile.ProfileActivity;
import com.ltd.admin.appchat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private Toolbar mToolbar;
    private RecyclerView allUserList;
    private DatabaseReference allDatabaseUserReference;
    private ArrayList arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        Anhxa();

    }

    public void Anhxa(){
        mToolbar = (Toolbar) findViewById(R.id.all_user_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Tìm kiếm bạn bè");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allUserList = (RecyclerView) findViewById(R.id.all_user_list);
        allUserList.setHasFixedSize(true);
        allUserList.setLayoutManager( new LinearLayoutManager(this));
        allDatabaseUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        allDatabaseUserReference.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<AllUsers,AllUsersViewHolder > firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                (
                        AllUsers.class,
                        R.layout.all_users_display_layout,
                        AllUsersViewHolder.class,
                        allDatabaseUserReference
                )
        {

            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {
                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_images(getApplicationContext(), model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        //Toast.makeText(AllUsersActivity.this, visit_user_id, Toast.LENGTH_SHORT).show();
                        Intent mIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        mIntent.putExtra("visit_user_id", visit_user_id);

                        startActivity(mIntent);
                    }
                });

            }
        };
        allUserList.setAdapter(firebaseRecyclerAdapter);
    }

    //Menu tim kiem
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_search_friend, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        SetFilter(newText);
        return true;
    }
    //Set Filter
    private void SetFilter( String search){
        // Tim kiem theo ten user
        Query searchUser = allDatabaseUserReference.orderByChild("user_name")
                .startAt(search).endAt(search + "\uf8ff");

        FirebaseRecyclerAdapter<AllUsers,AllUsersViewHolder > firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                (
                        AllUsers.class,
                        R.layout.all_users_display_layout,
                        AllUsersViewHolder.class,
                        searchUser
                )
        {

            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {
                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_images(getApplicationContext(), model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        //Toast.makeText(AllUsersActivity.this, visit_user_id, Toast.LENGTH_SHORT).show();
                        Intent mIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        mIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(mIntent);
                    }
                });

            }
        };
        allUserList.setAdapter(firebaseRecyclerAdapter);
    }

    // Tao lop All view holder lay du lieu nguoi dung hien thi RecyclerView
    public static class AllUsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public AllUsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUser_name( String user_name){
            TextView name = (TextView) mView.findViewById(R.id.all_users_name);
            name.setText(user_name);
        }

        public void setUser_status(String user_status){
            TextView status = (TextView) mView.findViewById(R.id.all_users_status);
            status.setText(user_status);
        }
        public void setUser_thumb_images(final Context mContext, final String user_thumb_image){
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

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

    }

}
