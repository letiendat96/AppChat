package com.ltd.admin.appchat.Message;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.VerifyAccount.KeyOption;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Admin on 4/21/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter <MessageAdapter.MessageViewHolder>{
    private KeyOption cKeyOption = new KeyOption();
    private StringBuilder deBuffer = new StringBuilder();
    public int size = 0;
    public DeCryptMessage deCryptMessage;
    ProgressDialog mProgressDialog;

    private List <MessagesUser> userMessagesList;
    //Get id nguoi gui tin
    private FirebaseAuth mAuth;
    private DatabaseReference UserDatabaseReference;

    private String rCipher = null, sCipher = null, cCipher = null, cCipherIndex = null, userIndexPadding = null;
    private long publicKeyDeCrypt = 0;

    private long [] k = new long[2];
    public String dePlainText = null;
    private static int StatusDeCryptFileText = 0;
    private int index = 2;

    private String key_ = "aes123";
    private String plain_ = "123aes";
    public  String userName, userNameOther ;


    public MessageAdapter(List<MessagesUser> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout_of_user, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return  new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        String message_sender_id = mAuth.getCurrentUser().getUid();

        final MessagesUser messagesUser = userMessagesList.get(position);

        String fromUserId = messagesUser.getFrom();
        String fromMessageType = messagesUser.getType();
        UserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        UserDatabaseReference.keepSynced(true);

        UserDatabaseReference.child(fromUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("user_name").getValue().toString();
                String userImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                //Load anh user
                Picasso.with(holder.userProfileImage.getContext()).load(userImage)
                        .placeholder(R.drawable.default_image).into(holder.userProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Tin nhan la text
        if (fromMessageType.equals("text")){
            holder.messagePicture.setVisibility(View.INVISIBLE);

            if (Objects.equals(fromUserId, message_sender_id)){

                holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);
                holder.messageText.setTextColor(Color.BLACK);
                holder.messageText.setGravity(Gravity.RIGHT);

                rCipher = messagesUser.getUser_cipher_r();
                sCipher = messagesUser.getUser_cipher_s();
                cCipher = messagesUser.getUser_cipher_c();
                cCipherIndex = messagesUser.getUser_cipher_c_index();

                userIndexPadding = messagesUser.getUser_index_padding();

                publicKeyDeCrypt = messagesUser.getPublic_key_receiver();

               // Log.d("keyadapter", Long.toString(publicKeyDeCrypt));

                // Time decrypt message
                //long start = System.nanoTime();

                try{

                    char [] CipherArray = new char[Integer.parseInt(cCipherIndex)];
                    char [] rCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];
                    char [] sCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];

                    try{
                        PreCipher(CipherArray, cCipher);
                        //ShowArray(CipherArray);

                        PreCipher(rCipherArray, rCipher);
                        //ShowArray(rCipherArray);

                        PreCipher(sCipherArray, sCipher);
                        //ShowArray(sCipherArray);

                    }catch (Exception e){
                        Log.d("Err", e.getMessage().toString());
                    }
                    final char [] decrypt = {0x0000, 0x0000, 0x0000, 0x0000};

                    for( int i = 1 ; i <= Integer.parseInt(cCipherIndex); i++){
                        if ( i%4 == 0){
                            //Log.d("sValue", Integer.toString(sCipherArray[i/4 -1]));
                            RecoverK(sCipherArray[i/4 - 1], k , 9241, publicKeyDeCrypt, rCipherArray, (i/4) -1);

                            decrypt[0] = CipherArray[i - 4];
                            decrypt[1] = CipherArray[i - 3];
                            decrypt[2] = CipherArray[i - 2];
                            decrypt[3] = CipherArray[i - 1];
                            try{
                                if(unsigncrypt(decrypt,k , rCipherArray, (i/4) -1)){
                                   // Log.d("cdeSign", Integer.toString(i));
                                    StatusDeCryptFileText = 0;
                                }else{
                                    // Tin nhan sua doi => thong bao
                                    StatusDeCryptFileText = 1;
                                    try{
                                        holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                                        UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                holder.messageText.setText("@" + dataSnapshot.child("user_name").getValue().toString() + "\n" + "Tin nhắn chưa được xác thực!\n" +  GetTimeMessage(messagesUser.getTime()));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }catch (Exception e){
                                        Log.d("Errr", e.getMessage().toString());
                                    }

                                    break;
                                }
                            }catch (Exception e){
                                Log.d("Err", e.getMessage().toString());
                            }
                        }
                    }

                    //Log.d("deCrypt", deBuffer.toString());
                    int a = deBuffer.toString().length();
                    a =  a - Integer.parseInt(userIndexPadding);
                    final String decode = deBuffer.substring(0, a);
                    //Log.d("ppp", decode);
                    deBuffer.setLength(0);

                    //long end = System.nanoTime();
                    //Log.d("Decrypt_msg_me", Long.toString( end - start));
                    // Lay thoi gian, ten nguoi gui
                    if (StatusDeCryptFileText == 0){
                        try{
                            holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                            UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    holder.messageText.setText("@" +  dataSnapshot.child("user_name").getValue().toString() + "\n" + decode+ "\n" + GetTimeMessage(messagesUser.getTime()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){
                            Log.d("Errr", e.getMessage().toString());
                        }
                    }
                }catch (Exception e){
                    Log.d("Errr", e.getMessage().toString());
                }
            }else {
                holder.messageText.setBackgroundResource(R.drawable.chat_rounded_rect_bg);
                holder.messageText.setTextColor(Color.GRAY);
                holder.messageText.setGravity(Gravity.LEFT);
                rCipher = messagesUser.getUser_cipher_r();
                sCipher = messagesUser.getUser_cipher_s();
                cCipher = messagesUser.getUser_cipher_c();
                cCipherIndex = messagesUser.getUser_cipher_c_index();

                userIndexPadding = messagesUser.getUser_index_padding();

                publicKeyDeCrypt = messagesUser.getPublic_key_sender();

               // Log.d("keyadapter", Long.toString(publicKeyDeCrypt));

                //long startRemsg = System.nanoTime();

                try{

                    char [] CipherArray = new char[Integer.parseInt(cCipherIndex)];
                    char [] rCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];
                    char [] sCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];

                    try{
                        PreCipher(CipherArray, cCipher);
                        //ShowArray(CipherArray);

                        PreCipher(rCipherArray, rCipher);
                        //ShowArray(rCipherArray);

                        PreCipher(sCipherArray, sCipher);
                        //ShowArray(sCipherArray);

                    }catch (Exception e){
                        Log.d("Err", e.getMessage().toString());
                    }
                    final char [] decrypt = {0x0000, 0x0000, 0x0000, 0x0000};

                    for( int i = 1 ; i <= Integer.parseInt(cCipherIndex); i++){
                        if ( i%4 == 0){
                            //Log.d("sValue", Integer.toString(sCipherArray[i/4 -1]));
                            RecoverK(sCipherArray[i/4 - 1], k , 9241, publicKeyDeCrypt, rCipherArray, (i/4) -1);

                            decrypt[0] = CipherArray[i - 4];
                            decrypt[1] = CipherArray[i - 3];
                            decrypt[2] = CipherArray[i - 2];
                            decrypt[3] = CipherArray[i - 1];
                            try{
                                if(unsigncrypt(decrypt,k , rCipherArray, (i/4) -1)){
                                    //Log.d("cdeSign", Integer.toString(i));
                                    StatusDeCryptFileText = 0;
                                }else{
                                    StatusDeCryptFileText = 1;
                                    try{
                                        holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                                        UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                holder.messageText.setText("@" + dataSnapshot.child("user_name").getValue().toString() + "\n" + "Tin nhắn chưa được xác thực!\n" +  GetTimeMessage(messagesUser.getTime()));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }catch (Exception e){
                                        Log.d("Errr", e.getMessage().toString());
                                    }

                                    break;
                                }
                            }catch (Exception e){
                                Log.d("Err", e.getMessage().toString());
                            }
                        }
                    }

                   // Log.d("deCrypt", deBuffer.toString());
                    int a = deBuffer.toString().length();
                    a =  a - Integer.parseInt(userIndexPadding);
                    final String decode = deBuffer.substring(0, a);
                   // Log.d("ppp", decode);
                    deBuffer.setLength(0);

                    //long endRemsg = System.nanoTime();
                    //Log.d("DeCrypt_msg_other", Long.toString(endRemsg -startRemsg));
                    if (StatusDeCryptFileText == 0){
                        try{
                            holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                            UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    holder.messageText.setText("@" +  dataSnapshot.child("user_name").getValue().toString() + "\n" + decode+ "\n" + GetTimeMessage(messagesUser.getTime()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){
                            Log.d("Errr", e.getMessage().toString());
                        }
                    }
                }catch (Exception e){
                    Log.d("Errr", e.getMessage().toString());
                }
            }
            // DeEnCrypt data => holder setText()
        }
        // Load anh duoc gui
        else if(fromMessageType.equals("image")) {
            holder.messageDay.setText(GetDayMessage(messagesUser.getTime()) + "\n" + GetTimeMessage(messagesUser.getTime()));

            holder.messageText.setVisibility(View.INVISIBLE);
            holder.messageText.setPadding(0,0,0,0);
            //Load anh khi offline/ online
            Picasso.with(holder.userProfileImage.getContext())
                    .load(messagesUser.getMessages())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_image)
                    .into(holder.messagePicture, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError() {
                            Picasso.with(holder.userProfileImage.getContext())
                                    .load(messagesUser.getMessages())
                                    .placeholder(R.drawable.default_image)
                                    .into(holder.messagePicture);
                        }
                    });

        }
        // Message is file
        else if (fromMessageType.equals("txt")){
            holder.messagePicture.setVisibility(View.INVISIBLE);
            // A => Cipher => Sever => Cipher = (Nhan du lieu ve do minh gui).
            if (Objects.equals(fromUserId, message_sender_id)){

                holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);
                holder.messageText.setTextColor(Color.BLACK);
                holder.messageText.setGravity(Gravity.RIGHT);

                rCipher = messagesUser.getUser_cipher_r();
                sCipher = messagesUser.getUser_cipher_s();
                cCipher = messagesUser.getUser_cipher_c();
                cCipherIndex = messagesUser.getUser_cipher_c_index();

                userIndexPadding = messagesUser.getUser_index_padding();

                publicKeyDeCrypt = messagesUser.getPublic_key_receiver();

                //Log.d("keyadapter", Long.toString(publicKeyDeCrypt));

                //long start = System.nanoTime();

                try{
                    //=====================================================================
                    // Tinh toan lai phan tu r , s, c
                    char [] CipherArray = new char[Integer.parseInt(cCipherIndex)];
                    char [] rCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];
                    char [] sCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];

                    try{
                        PreCipher(CipherArray, cCipher);
                        //ShowArray(CipherArray);

                        PreCipher(rCipherArray, rCipher);
                       // ShowArray(rCipherArray);

                        PreCipher(sCipherArray, sCipher);
                       // ShowArray(sCipherArray);

                    }catch (Exception e){
                        Log.d("Err", e.getMessage().toString());
                    }

                    //long end1 = System.nanoTime();
                    //======================================================================
                    // UnsignCrypt
                    //Log.d("ectimeenc1", Long.toString(end1 - start));
                    //long start2 = System.currentTimeMillis();

                    final char [] decrypt = {0x0000, 0x0000, 0x0000, 0x0000};

                    for( int i = 1 ; i <= Integer.parseInt(cCipherIndex); i++){
                        if ( i%4 == 0){
                           // Log.d("sValue", Integer.toString(sCipherArray[i/4 -1]));
                            //long startK = System.nanoTime();
                            RecoverK(sCipherArray[i/4 - 1], k , 9241, publicKeyDeCrypt, rCipherArray, (i/4) -1);
                            //long endK = System.nanoTime();
                            //Log.d("logK", Long.toString( endK - startK));

                            decrypt[0] = CipherArray[i - 4];
                            decrypt[1] = CipherArray[i - 3];
                            decrypt[2] = CipherArray[i - 2];
                            decrypt[3] = CipherArray[i - 1];

                            try{
                                if(unsigncrypt(decrypt,k , rCipherArray, (i/4) -1)){
                                   // Log.d("cdeSign", Integer.toString(i));
                                    StatusDeCryptFileText = 0;
                                }else {
                                    deBuffer.setLength(0);
                                    StatusDeCryptFileText = 1;
                                    try{
                                        holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                                        UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                holder.messageText.setText("@" + dataSnapshot.child("user_name").getValue().toString() + "\n" + "Tệp tin .txt chưa được xác thực!\n" +  GetTimeMessage(messagesUser.getTime()));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }catch (Exception e){
                                        Log.d("Errr", e.getMessage().toString());
                                    }
                                    //Log.d("Err",Integer.toString(i));
                                    break;
                                }
                            }catch (Exception e){
                                Log.d("Err", e.getMessage().toString());
                            }
                            //long endUS = System.nanoTime();
                            //Log.d("logUS", Long.toString( endUS - endK));
                        }
                    }

                    //long end2 = System.nanoTime();
                    //Log.d("ectimeenc2", Long.toString(end2 - end1));
                    //=================================================================
                    // aes
//                    long t = (end2 - end1) * index;
//                    Log.d("ectime_aes", Long.toString(t));

                   // Log.d("ectimeenc22", Long.toString(System.currentTimeMillis() - start2));
                    //=================================================================
                    // Tinh lai padding

                    //Log.d("deCrypt", deBuffer.toString());
                    int a = deBuffer.toString().length();
                    a =  a - Integer.parseInt(userIndexPadding);
                    final String decode = deBuffer.substring(0, a);

                    long end3 = System.nanoTime();
                    //Log.d("ectimeenc3", Long.toString( end3 - end2));

                    //Log.d("ppp", decode);
                    deBuffer.setLength(0);
                    //long end = System.nanoTime();
                    // Test time to decrypt file txt
                    // Log.d("ectimedecrypt_file_me", Long.toString(end -start));
                    if (StatusDeCryptFileText == 0){
                        try{
                            holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                            UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    holder.messageText.setText("@" +  dataSnapshot.child("user_name").getValue().toString() + "\n" + decode+ "\n" + GetTimeMessage(messagesUser.getTime()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){
                            Log.d("Errr", e.getMessage().toString());
                        }
                    }
                }catch (Exception e){
                    Log.d("Errr", e.getMessage().toString());
                }

            }
            // A => Cipher => Server => Cipher => B
            // Gui tin nhan di
            else{
                holder.messageText.setBackgroundResource(R.drawable.chat_rounded_rect_bg);
                holder.messageText.setTextColor(Color.GRAY);
                holder.messageText.setGravity(Gravity.LEFT);

                rCipher = messagesUser.getUser_cipher_r();
                sCipher = messagesUser.getUser_cipher_s();
                cCipher = messagesUser.getUser_cipher_c();
                cCipherIndex = messagesUser.getUser_cipher_c_index();

                userIndexPadding = messagesUser.getUser_index_padding();

                publicKeyDeCrypt = messagesUser.getPublic_key_sender();

                //Log.d("keyadapter", Long.toString(publicKeyDeCrypt));

                //long startRe = System.nanoTime();

                try{

                    char [] CipherArray = new char[Integer.parseInt(cCipherIndex)];
                    char [] rCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];
                    char [] sCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];

                    try{
                        PreCipher(CipherArray, cCipher);
                        //ShowArray(CipherArray);

                        PreCipher(rCipherArray, rCipher);
                        //ShowArray(rCipherArray);

                        PreCipher(sCipherArray, sCipher);
                        //ShowArray(sCipherArray);

                    }catch (Exception e){
                        Log.d("Err", e.getMessage().toString());
                    }


                    final char [] decrypt = {0x0000, 0x0000, 0x0000, 0x0000};

                    for( int i = 1 ; i <= Integer.parseInt(cCipherIndex); i++){
                        if ( i%4 == 0){
                           // Log.d("sValue", Integer.toString(sCipherArray[i/4 -1]));
                            RecoverK(sCipherArray[i/4 - 1], k , 9241, publicKeyDeCrypt, rCipherArray, (i/4) -1);

                            decrypt[0] = CipherArray[i - 4];
                            decrypt[1] = CipherArray[i - 3];
                            decrypt[2] = CipherArray[i - 2];
                            decrypt[3] = CipherArray[i - 1];

                            //String sPlain = String.valueOf(decrypt);
                            //String sPlainKey = String.valueOf(k);
                            //String s = DecryptAES(sPlain, sPlainKey);
                            //Log.d("decryptaes", s);

                            try{
                                if(unsigncrypt(decrypt,k , rCipherArray, (i/4) -1)){
                                   // Log.d("cdeSign", Integer.toString(i));
                                    StatusDeCryptFileText = 0;
                                }else {
                                    deBuffer.setLength(0);
                                    StatusDeCryptFileText = 1;
                                    //Log.d("Err",Integer.toString(i));
                                    try{
                                        holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                                        UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                holder.messageText.setText("@" + dataSnapshot.child("user_name").getValue().toString() + "\n" + "Tệp tin .txt chưa được xác thực!\n" +  GetTimeMessage(messagesUser.getTime()));
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }catch (Exception e){
                                        Log.d("Errr", e.getMessage().toString());
                                    }
                                    break;
                                }
                            }catch (Exception e){
                                Log.d("Err", e.getMessage().toString());
                            }
                        }
                    }
                    //Log.d("deCrypt", deBuffer.toString());
                    int a = deBuffer.toString().length();
                    a =  a - Integer.parseInt(userIndexPadding);
                    final String decode = deBuffer.substring(0, a);
                    //Log.d("ppp", decode);
                    deBuffer.setLength(0);
                    //long endRe = System.nanoTime();
                    //Log.d("decrypt_file_other", Long.toString(endRe -startRe));
                    if (StatusDeCryptFileText == 0){
                        try{
                            holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                            UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    holder.messageText.setText("@" +  dataSnapshot.child("user_name").getValue().toString() + "\n" + decode+ "\n" + GetTimeMessage(messagesUser.getTime()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){
                            Log.d("Errr", e.getMessage().toString());
                        }
                    }
                }catch (Exception e){
                    Log.d("Errr", e.getMessage().toString());
                }
            }
        }
        else if (fromMessageType.equals("other")){
            holder.messagePicture.setVisibility(View.INVISIBLE);
            // A => Cipher => Sever => Cipher = (Nhan du lieu ve do minh gui).
            if (Objects.equals(fromUserId, message_sender_id)){

                holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);
                holder.messageText.setTextColor(Color.BLACK);
                holder.messageText.setGravity(Gravity.RIGHT);

                rCipher = messagesUser.getUser_cipher_r();
                sCipher = messagesUser.getUser_cipher_s();
                cCipher = messagesUser.getUser_cipher_c();
                cCipherIndex = messagesUser.getUser_cipher_c_index();

                userIndexPadding = messagesUser.getUser_index_padding();

                publicKeyDeCrypt = messagesUser.getPublic_key_receiver();

                //Log.d("keyadapter", Long.toString(publicKeyDeCrypt));

                //long start = System.nanoTime();

                try{
                    //=====================================================================
                    // Tinh toan lai phan tu r , s, c
                    char [] CipherArray = new char[Integer.parseInt(cCipherIndex)];
                    char [] rCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];
                    char [] sCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];

                    try{
                        PreCipher(CipherArray, cCipher);
                        //ShowArray(CipherArray);

                        PreCipher(rCipherArray, rCipher);
                        // ShowArray(rCipherArray);

                        PreCipher(sCipherArray, sCipher);
                        // ShowArray(sCipherArray);

                    }catch (Exception e){
                        Log.d("Err", e.getMessage().toString());
                    }

                    //long end1 = System.nanoTime();
                    //======================================================================
                    // UnsignCrypt
                    //Log.d("ectimeenc1", Long.toString(end1 - start));
                    //long start2 = System.currentTimeMillis();

                    final char [] decrypt = {0x0000, 0x0000, 0x0000, 0x0000};

                    for( int i = 1 ; i <= Integer.parseInt(cCipherIndex); i++){
                        if ( i%4 == 0){
                            // Log.d("sValue", Integer.toString(sCipherArray[i/4 -1]));
                            //long startK = System.nanoTime();
                            RecoverK(sCipherArray[i/4 - 1], k , 9241, publicKeyDeCrypt, rCipherArray, (i/4) -1);
                            //long endK = System.nanoTime();
                            //Log.d("logK", Long.toString( endK - startK));

                            decrypt[0] = CipherArray[i - 4];
                            decrypt[1] = CipherArray[i - 3];
                            decrypt[2] = CipherArray[i - 2];
                            decrypt[3] = CipherArray[i - 1];

                            try{
                                if(unsigncrypt(decrypt,k , rCipherArray, (i/4) -1)){
                                    // Log.d("cdeSign", Integer.toString(i));
                                    StatusDeCryptFileText = 0;
                                }else {
                                    deBuffer.setLength(0);
                                    StatusDeCryptFileText = 1;
                                    // Tep tin chua duoc xac thuc
                                    try{
                                        holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                                        UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                holder.messageText.setText("@" + dataSnapshot.child("user_name").getValue().toString() + "\n" + "Tệp tin chưa được xác thực!\n" +  GetTimeMessage(messagesUser.getTime()));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }catch (Exception e){
                                        Log.d("Errr", e.getMessage().toString());
                                    }
                                    //Log.d("Err",Integer.toString(i));
                                    break;
                                }
                            }catch (Exception e){
                                Log.d("Err", e.getMessage().toString());
                            }
                            //long endUS = System.nanoTime();
                            //Log.d("logUS", Long.toString( endUS - endK));
                        }
                    }

                    //long end2 = System.nanoTime();
                    //Log.d("ectimeenc2", Long.toString(end2 - end1));
                    //=================================================================
                    // aes
//                    long t = (end2 - end1) * index;
//                    Log.d("ectime_aes", Long.toString(t));

                    //Log.d("ectimeenc22", Long.toString(System.currentTimeMillis() - start2));
                    //=================================================================
                    // Tinh lai padding

                    //Log.d("deCrypt", deBuffer.toString());
                    int a = deBuffer.toString().length();
                    a =  a - Integer.parseInt(userIndexPadding);
                    final String decode = deBuffer.substring(0, a);

                    //long end3 = System.nanoTime();
                    //Log.d("ectimeenc3", Long.toString( end3 - end2));

                    //Log.d("ppp", decode);
                    deBuffer.setLength(0);
                    //long end = System.nanoTime();
                    // Test time to decrypt file txt
                    // Log.d("ectimedecrypt_file_me", Long.toString(end -start));
                    if (StatusDeCryptFileText == 0){
                        try{
                            holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                            UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    holder.messageText.setText("@" +  dataSnapshot.child("user_name").getValue().toString() +
                                            "\n" + decode+ "\n" + messagesUser.getMessages() + "\n"  + GetTimeMessage(messagesUser.getTime()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){
                            Log.d("Errr", e.getMessage().toString());
                        }
                    }
                }catch (Exception e){
                    Log.d("Errr", e.getMessage().toString());
                }

            }
            // A => Cipher => Server => Cipher => B
            // Gui tin nhan di
            else{
                holder.messageText.setBackgroundResource(R.drawable.chat_rounded_rect_bg);
                holder.messageText.setTextColor(Color.GRAY);
                holder.messageText.setGravity(Gravity.LEFT);

                rCipher = messagesUser.getUser_cipher_r();
                sCipher = messagesUser.getUser_cipher_s();
                cCipher = messagesUser.getUser_cipher_c();
                cCipherIndex = messagesUser.getUser_cipher_c_index();

                userIndexPadding = messagesUser.getUser_index_padding();

                publicKeyDeCrypt = messagesUser.getPublic_key_sender();

                //Log.d("keyadapter", Long.toString(publicKeyDeCrypt));

                //long startRe = System.nanoTime();

                try{

                    char [] CipherArray = new char[Integer.parseInt(cCipherIndex)];
                    char [] rCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];
                    char [] sCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];

                    try{
                        PreCipher(CipherArray, cCipher);
                        //ShowArray(CipherArray);

                        PreCipher(rCipherArray, rCipher);
                        //ShowArray(rCipherArray);

                        PreCipher(sCipherArray, sCipher);
                        //ShowArray(sCipherArray);

                    }catch (Exception e){
                        Log.d("Err", e.getMessage().toString());
                    }


                    final char [] decrypt = {0x0000, 0x0000, 0x0000, 0x0000};

                    for( int i = 1 ; i <= Integer.parseInt(cCipherIndex); i++){
                        if ( i%4 == 0){
                            // Log.d("sValue", Integer.toString(sCipherArray[i/4 -1]));
                            RecoverK(sCipherArray[i/4 - 1], k , 9241, publicKeyDeCrypt, rCipherArray, (i/4) -1);

                            decrypt[0] = CipherArray[i - 4];
                            decrypt[1] = CipherArray[i - 3];
                            decrypt[2] = CipherArray[i - 2];
                            decrypt[3] = CipherArray[i - 1];

                            //String sPlain = String.valueOf(decrypt);
                            //String sPlainKey = String.valueOf(k);
                            //String s = DecryptAES(sPlain, sPlainKey);
                            //Log.d("decryptaes", s);

                            try{
                                if(unsigncrypt(decrypt,k , rCipherArray, (i/4) -1)){
                                    // Log.d("cdeSign", Integer.toString(i));
                                    StatusDeCryptFileText = 0;
                                }else {
                                    deBuffer.setLength(0);
                                    StatusDeCryptFileText = 1;
                                    try{
                                        holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                                        UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                holder.messageText.setText("@" + dataSnapshot.child("user_name").getValue().toString() + "\n" + "Tệp tin chưa được xác thực!\n" +  GetTimeMessage(messagesUser.getTime()));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }catch (Exception e){
                                        Log.d("Errr", e.getMessage().toString());
                                    }
                                    //Log.d("Err",Integer.toString(i));
                                    break;
                                }
                            }catch (Exception e){
                                Log.d("Err", e.getMessage().toString());
                            }
                        }
                    }

                    //Log.d("deCrypt", deBuffer.toString());
                    int a = deBuffer.toString().length();
                    a =  a - Integer.parseInt(userIndexPadding);
                    final String decode = deBuffer.substring(0, a);
                    //Log.d("ppp", decode);
                    deBuffer.setLength(0);

                    //long endRe = System.nanoTime();

                    //Log.d("decrypt_file_other", Long.toString(endRe -startRe));
                    if (StatusDeCryptFileText == 0){
                        try{
                            holder.messageDay.setText(GetDayMessage(messagesUser.getTime()));
                            UserDatabaseReference.child(messagesUser.getFrom()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    holder.messageText.setText("@" +  dataSnapshot.child("user_name").getValue().toString() +
                                            "\n" + decode+ "\n" + messagesUser.getMessages() + "\n" + GetTimeMessage(messagesUser.getTime()));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){
                            Log.d("Errr", e.getMessage().toString());
                        }
                    }

                }catch (Exception e){
                    Log.d("Errr", e.getMessage().toString());
                }
            }

        }
    }
    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView userProfileImage;
        public ImageView messagePicture;
        public TextView messageDay;
        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.txt_message);
            userProfileImage = (CircleImageView) view.findViewById(R.id.messages_profile_image);
            messagePicture = (ImageView) view.findViewById(R.id.message_image_view);
            messageDay = (TextView) view.findViewById(R.id.txt_time_send_message);
        }
    }
    //============================================================================================
    // Tra ve mang gia tri nguyen arr 4 character
    private void PreCipher(char [] arr, String s){
        String [] words = s.split("\\s");
        int index = 0;
        for(String w:words){
            if (!w.equals("0")){
                int num = Integer.parseInt(w);
                arr[index] = (char) num;
                index = index + 1;
            }
        }
    }
    // State 4 character
    private void DePresent( char [] state, char [] key){
        // Giai ma tuan tu, tinh K trc roi dua vao
        final char sBox4[] = {0xc,0x5,0x6,0xb,0x9,0x0,0xa,0xd,0x3,0xe,0xf,0x8,0x4,0x7,0x1,0x2};
        final char invsBox4[] = {0x5,0xe,0xf,0x8,0xc,0x1,0x2,0xd,0xb,0x4,0x6,0x3,0x0,0x7,0x9,0xa};
        // State 4 phan tu
//        char state[]={0xfe6b,0x244e,0x61d1,0x45fe};
//        //uint16_t state[4]={0x5579,0xc138,0x7b22,0x8445};
        // Key 5 phan tu
//        char key[]={0x0031, 0x0032, 0x0033, 0x0034, 0x0035};
        char i = 0;

        char position = 0;
        char element_source = 0;
        char bit_source = 0;
        char element_destination = 0;
        char bit_destination = 0;
        char temp_pLayer[] = new char[4];

        char round=0;
        char save1;
        char save2;
        char subkey[][]= new char[32][4];

        for(i=1;i<=4;i++)
        {
            subkey[0][i-1] = key[i];
        }
        do{
            save1 = key[0];
            save2 = key[1];
            i = 0;
            do
            {
                key[i]= (char) (key[i+1]>>3|key[i+2]<<13);
                i++;
            }while(i<3);
            key[3]=  (char) (key[4]>>3|save1<<13);
            key[4]=  (char) (save1>>3|save2<<13);
            key[4] = (char) (sBox4[key[4]>>12]<<12 | (key[4] & 0xFFF));

            if((round+1) % 2 == 1)							//round counter addition
                key[0] ^= 32768;
            key[1] = (char)((((round+1)>>1) ^ (key[1] & 15)) | (key[1] & 65520));
            for(i=1;i<=4;i++)
            {
                subkey[round+1][i-1] = key[i];
            }

            round++;
        }while(round<31);
        do{
            i=0;
            do
            {
                state[i] = (char)(state[i] ^ subkey[round][i]);
                temp_pLayer[i] = 0;
                i++;
            }while(i<=3);
            for(i=0;i<64;i++)
            {
                position = (char)((4*i) % 63);						//Artithmetic calculation of the pLayer
                if(i == 63)									//exception for bit 63
                    position = 63;
                element_source		= (char)(i / 16);
                bit_source 			= (char)(i % 16);
                element_destination	= (char) (position / 16);
                bit_destination 	= (char) (position % 16);
                temp_pLayer[element_destination] |= ((state[element_source]>>bit_source) & 0x1) << (bit_destination);
            }
            for(i=0;i<4;i++)
            {
                state[i] = temp_pLayer[i];
            }

            for(i=0;i<4;i++){
                state[i]= (char) (invsBox4[(state[i]&0xF000)>>12]<<12|invsBox4[(state[i]&0xF00)>>8]<<8|invsBox4[(state[i]&0xF0)>>4]<<4|invsBox4[state[i]&0xF]);
                //printf("%04x",state[i]);
            }
            round--;
        }while(round>0);
        i = 0;
        do												//final key XOR
        {
            state[i] = (char) (state[i] ^ subkey[0][i]);
            i++;
        }while(i<=3);

    }

    // Da test , s - r: 1 character
    // ab public Key Send
    private void RecoverK( long s, long k[],long p, long ab, char [] r, int rIndex){

        //Log.d("rValue", Integer.toString(r[rIndex]));
        long z = s;
        //Log.d("zValue", Long.toString(z));
        // r gia tri nguyen lay tu firebase
        for(int i = 1; i <= (ab + r[rIndex]); i++){
            z = cKeyOption.chebyshev1(3,z,p);
        }

        //Log.d("zRecover", Long.toString(z));
        k[0] = z%877;
        k[1] = z - k[0];
        //String ss = Long.toString(k[0]) + " " + Long.toString(k[1]);
        //Log.d("mKey", ss);
    }
    // Ham unsign, cipher[] 4 character
    private boolean unsigncrypt(char cipher[], long k[], char [] r, int rIndex ){
        char [] key = new char[5];
        for( int i = 0 ; i<5; i++){
            key[i] = 0x0000;
        }

        char [][] plain = new char[8][4];
        for (int m = 0 ; m < 8; m++){
            for ( int n = 0; n < 4; n++  ){
                plain [m][n] = 0x0000;
            }
        }

        int j = 0 ,t=0;
        try{
            while(k[1]>0){
                key[4-j]= (char) (k[1]%100);
                k[1] = k[1]/100;
                j = j+1;
                if (j == 5 ) break;
            }
        }catch (Exception e){
            Log.d("deCrypt", e.getMessage().toString());
        }

        size = 4;

        float length = (float) size;
        //Log.d("mLen", Float.toString(length));
        int b = (int) Math.ceil(length/4);

        for(int i = 0; i<b ;i++){
            for( j = 0 ; j<4; j++ , t++){
                plain[i][j]=  cipher[t];
            }
            DePresent(plain[i],key);

        }




//============================================================
//        for(int i=0; i<b; i++){
//            for(j =0;j<4;j++){
//                String sPlain = String.valueOf(plain_);
//                String sPlainKey = String.valueOf(key_);
//                DecryptAES(sPlain, sPlainKey);
//            }
//        }

//=============================================================


        t = 0;
        StringBuilder buffer = new StringBuilder();

        for(int i = 0; i<b ; i++){
            for( j = 0; j<4 ; j++, t++){
                buffer.append(Character.toString (plain[i][j]));
                if (t == size ) break;
            }
            if (t == size) break;
        }
        //Log.d("deCrypt", buffer.toString());

        String mSHA256  = cKeyOption.SHA256(buffer.toString());
        char [] sSHA256 = mSHA256.toCharArray();
        long [] ri = new long[64];
        long r1 = 0;
        for ( int i = 0 ; i < 64 ; i++ ){
            ri[i] = sSHA256 [i] ^ k[0];
            r1 = r1 ^ ri[i];
        }
        //Log.d("r1deCipher", Long.toString(r1));

        if(r1 == r[rIndex]) {
            size = 0;
            // msg sau khi giai ma
            deBuffer.append(buffer.toString());
            //Flag = 1;
            StatusDeCryptFileText = 0;
            return true;
        }else {
            StatusDeCryptFileText = 1;
            deBuffer.setLength(0);
            return false;
        }
    }

    private void ShowArray( char [] Arr){
        for (int i = 0; i < Arr.length ; i++){
            Log.d("EleArr", Integer.toString(Arr[i]));
        }
    }

    private class  DeCryptMessage extends  AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

                char [] CipherArray = new char[Integer.parseInt(cCipherIndex)];
                char [] rCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];
                char [] sCipherArray = new char[ (Integer.parseInt(cCipherIndex))/4 ];

                try{
                    PreCipher(CipherArray, cCipher);
                    //ShowArray(CipherArray);

                    PreCipher(rCipherArray, rCipher);
                    //ShowArray(rCipherArray);

                    PreCipher(sCipherArray, sCipher);
                    //ShowArray(sCipherArray);

                }catch (Exception e){
                    Log.d("Err", e.getMessage().toString());
                }

                final char [] decrypt = {0x0000, 0x0000, 0x0000, 0x0000};

                for( int i = 1 ; i <= Integer.parseInt(cCipherIndex); i++){
                    if ( i%4 == 0){
                        Log.d("sValue", Integer.toString(sCipherArray[i/4 -1]));
                        RecoverK(sCipherArray[i/4 - 1], k , 9241, publicKeyDeCrypt, rCipherArray, (i/4) -1);

                        decrypt[0] = CipherArray[i - 4];
                        decrypt[1] = CipherArray[i - 3];
                        decrypt[2] = CipherArray[i - 2];
                        decrypt[3] = CipherArray[i - 1];
                        try{
                            if(unsigncrypt(decrypt,k , rCipherArray, (i/4) -1)){
                                Log.d("cdeSign", Integer.toString(i));
                            }
                        }catch (Exception e){
                            Log.d("Err", e.getMessage().toString());
                        }
                    }
                }
                Log.d("deCrypt", deBuffer.toString());
                int a = deBuffer.toString().length();
                a =  a - Integer.parseInt(userIndexPadding);
                String decode = deBuffer.substring(0, a);
                Log.d("ppp", decode);
                deBuffer.setLength(0);
                return decode;

        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("pppa", s);
            dePlainText = s;
            Log.d("pText", dePlainText);
        }

    }

    public String GetTimeMessage(long time){
        Date date = new Date( time );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
        return simpleDateFormat.format(date);
    }

    public String GetDayMessage(long time){
        Date date = new Date( time );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd-MMM-yyyy");
        return simpleDateFormat.format(date);
    }

    private String DecryptAES(String output, String password){
        String decrypted = null;
        try{
            SecretKeySpec key = GenerateKey(password);
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, key);
            byte [] decodedValue = Base64.decode(output, Base64.DEFAULT);
            byte [] decValue = c.doFinal(decodedValue);
            decrypted = new String(decValue);
        }catch (Exception e){
            Log.d("Err", e.getMessage().toString());
        }

        return decrypted;
    }

    private SecretKeySpec GenerateKey(String password){
        SecretKeySpec secretKeySpec = null;
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = password.getBytes("UTF-8");
            digest.update(bytes, 0 , bytes.length);

            byte [] key = digest.digest();

            Log.d("keyLenght", Integer.toString(key.length));

            secretKeySpec = new SecretKeySpec(key, "AES");

        }catch (Exception e){
            Log.d("Err", e.getMessage().toString());
        }
        return secretKeySpec;
    }

}
