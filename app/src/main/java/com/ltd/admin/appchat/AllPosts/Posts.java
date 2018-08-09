package com.ltd.admin.appchat.AllPosts;

/**
 * Created by Admin on 5/19/2018.
 */

public class Posts {
    public String date, description, name, postImage, profileImage, time, uid;

    public Posts() {
    }

    public Posts(String date, String description, String name, String postImage, String profileImage, String time, String uid) {
        this.date = date;
        this.description = description;
        this.name = name;
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.time = time;
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
