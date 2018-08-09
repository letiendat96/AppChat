package com.ltd.admin.appchat.Comment;

/**
 * Created by Admin on 5/22/2018.
 */

public class Comments {
    public String comment, date, time, username , profileImage;

    public Comments() {
    }



    public Comments(String comment, String date, String time, String username, String profileImage) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.username = username;
        this.profileImage = profileImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
