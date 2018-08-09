package com.ltd.admin.appchat.Message;

/**
 * Created by Admin on 4/21/2018.
 */

public class MessagesUser{

    private String messages, type;   // messages : text, link image
    private long time;
    private boolean seen;
    private String from;
    private String user_cipher_c, user_cipher_c_index, user_cipher_r, user_cipher_s, user_index_padding;
    private long public_key_receiver;
    private long public_key_sender;
    private String replainText;

    public long getPublic_key_receiver() {
        return public_key_receiver;
    }

    public void setPublic_key_receiver(long public_key_receiver) {
        this.public_key_receiver = public_key_receiver;
    }

    public long getPublic_key_sender() {
        return public_key_sender;
    }

    public void setPublic_key_sender(long public_key_sender) {
        this.public_key_sender = public_key_sender;
    }

    public MessagesUser() {

    }

    public MessagesUser(String messages, String type, long time, boolean seen, String from, String user_cipher_c, String user_cipher_c_index, String user_cipher_r, String user_cipher_s, String user_index_padding) {
        this.messages = messages;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.from = from;
        this.user_cipher_c = user_cipher_c;
        this.user_cipher_c_index = user_cipher_c_index;
        this.user_cipher_r = user_cipher_r;
        this.user_cipher_s = user_cipher_s;
        this.user_index_padding = user_index_padding;
    }

    public String getUser_cipher_c() {
        return user_cipher_c;
    }

    public void setUser_cipher_c(String user_cipher_c) {
        this.user_cipher_c = user_cipher_c;
    }

    public String getUser_cipher_c_index() {
        return user_cipher_c_index;
    }

    public void setUser_cipher_c_index(String user_cipher_c_index) {
        this.user_cipher_c_index = user_cipher_c_index;
    }

    public String getUser_cipher_r() {
        return user_cipher_r;
    }

    public void setUser_cipher_r(String user_cipher_r) {
        this.user_cipher_r = user_cipher_r;
    }

    public String getUser_cipher_s() {
        return user_cipher_s;
    }

    public void setUser_cipher_s(String user_cipher_s) {
        this.user_cipher_s = user_cipher_s;
    }

    public String getUser_index_padding() {
        return user_index_padding;
    }

    public void setUser_index_padding(String user_index_padding) {
        this.user_index_padding = user_index_padding;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getReplainText() {
        return replainText;
    }

    public void setReplainText(String replainText) {
        this.replainText = replainText;
    }
}
