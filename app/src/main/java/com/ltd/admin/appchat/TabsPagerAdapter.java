package com.ltd.admin.appchat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ltd.admin.appchat.Chat.ChatsFragment;
import com.ltd.admin.appchat.RequestFriend.RequestsFragment;

/**
 * Created by Admin on 4/12/2018.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter{

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                HomeFragment homeFragment = new HomeFragment();
                return  homeFragment;
            case 1:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 2:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 3:
                FriendsFragment friendsFragment = new FriendsFragment();
                return  friendsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }



//    public CharSequence getPageTitle(int position){
//        switch (position){
//            case 0:
//                return "Trang chủ";
//            case 1:
//                return "Thông báo";
//            case 2:
//                return "Tin nhắn";
//            case 3:
//                return "Bạn bè";
//            default:
//                return null;
//
//        }
//    }
}
