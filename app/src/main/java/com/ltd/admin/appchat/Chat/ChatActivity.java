package com.ltd.admin.appchat.Chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ltd.admin.appchat.Message.LastSeenTime;
import com.ltd.admin.appchat.Message.MessageAdapter;
import com.ltd.admin.appchat.Message.MessagesUser;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.VerifyAccount.KeyOption;
import com.ltd.admin.appchat.VideoCallActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;
    private Toolbar mToolbar;
    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView mCircleImageView;

    private DatabaseReference rootRef, rootName;

    private FirebaseAuth mAuth;
    private String messageSenderId;

    private ImageView sendMessageView;
    private ImageView selectImageView;
    private EmojiconEditText inputText;
    private RecyclerView userMessage;

    private final List <MessagesUser> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager ;
    private MessageAdapter messageAdapter;
    private static int GALLERY_PICK = 1;
    private static int SEND_FILE = 2;

    private StorageReference MessageImageStorageRef, PostFileRef;
    private ProgressDialog mDialog ;
    private ProgressDialog sDialog;
    private ProgressDialog mReceiver;

    //===================================================================
    private KeyOption mKeyOption;
    private long [] k = new long[2];
    private String privateKeySend = null;
    private String publicKeyReceiver = null;

    private StringBuilder rEnBuffer = new StringBuilder();
    private StringBuilder sEnBuffer = new StringBuilder();
    private StringBuilder cEnBuffer = new StringBuilder();
    //===================================================================

    private DatabaseReference UserReference;
    private DatabaseReference KeyReference;
    private DatabaseReference CipherReference;
    private DatabaseReference SetIdLoginReference;

    private String messageEncrypt = null;
    private SignCryptMessages mSignCrypt;

    private SignCryptMessages mSignCryptFile;
    public MessagesUser pMessagesUser = new MessagesUser();
    long pkey0_key = 0;
    long pkey0_key_send = 0;

    private  ReceiverMessage receiverMessage, refreshMessage;
    private EmojIconActions emojIcon;
    private View rootView;
    private ImageView emojiButton;
    private ImageView imgSendFile;
    private ImageView imgVideoCall;
    //===================================================================
    private String fileSend = null;
    private static int sendType = 0;
    private ProgressDialog mViewMessage;
    // Storage Permissions

    private long indexAes = 2;
    private String userName = null;
    //===================================================================
    private String saveCurrentDate, saveCurrentTime, postRandomName, downLoadFile;
    private String fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Anhxa();

        userMessage = (RecyclerView) findViewById(R.id.message_list_of_user);
        messageAdapter = new MessageAdapter(messageList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessage.setHasFixedSize(true);
        userMessage.setLayoutManager(linearLayoutManager);
        userMessage.setAdapter(messageAdapter);

        //==================================================================================================================
        // id , name nguoi nhan
        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString(); // nguoi gui tin
        messageReceiverName = getIntent().getExtras().get("user_name").toString();

        //Toast.makeText(ChatActivity.this, messageReceiverId + " " + messageReceiverName  , Toast.LENGTH_SHORT).show();

        // Get Key
        // A-> B : A -> Encrypt Data -> Server ( B's use publicKey0 ), Server -> A (B's use publicKey0), Server -> B (B's use publicKey0).

        KeyReference.child(messageReceiverId).child("user_key").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String mKey = dataSnapshot.getValue().toString();
                Log.d("getKey", mKey);
                int indexKey = mKey.indexOf('_');
                String key0_Key = mKey.substring(0, indexKey);
                String key1_Key = mKey.substring(indexKey +1 , indexKey +5);
                String key2_Key = mKey.substring(indexKey +6, mKey.length());
                String mKey_Key = key0_Key + key1_Key + key2_Key;

                pkey0_key = Long.parseLong(key0_Key);
                long pkey1_key = Long.parseLong(key1_Key);
                long pkey2_key = Long.parseLong(key2_Key);
                Log.d("creteKeyReceiver", Long.toString(pkey0_key));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        KeyReference.child(messageSenderId).child("user_key").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String mKeySend = dataSnapshot.getValue().toString();
                Log.d("getKey", mKeySend);
                int indexKey = mKeySend.indexOf('_');
                String key0_Key = mKeySend.substring(0, indexKey);
                String key1_Key = mKeySend.substring(indexKey +1 , indexKey +5);
                String key2_Key = mKeySend.substring(indexKey +6, mKeySend.length());
                String mKey_Key = key0_Key + key1_Key + key2_Key;

                pkey0_key_send = Long.parseLong(key0_Key);
                long pkey1_key = Long.parseLong(key1_Key);
                long pkey2_key = Long.parseLong(key2_Key);
                Log.d("creteKeySend", Long.toString(pkey0_key_send));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //==================================================================================================================
        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages_Pictures");

        mDialog = new ProgressDialog(this);
        sDialog = new ProgressDialog(this);
        //==================================================================================================================
        // Nhan tin nhan
        try{
            receiverMessage = new ReceiverMessage();
            receiverMessage.execute();
        }catch (Exception e){
            Log.d("Errr", e.getMessage().toString());
        }

        //Doc du lieu person
        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String online = dataSnapshot.child("online").getValue().toString();
                final  String userThumb = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image)
                        .into(mCircleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onError() {
                                Picasso.with(ChatActivity.this).load(userThumb).placeholder(R.drawable.default_image).into(mCircleImageView);
                            }
                        });
                if (online.equals("true")){
                    userLastSeen.setText("đang hoạt động");
                }else {
                    LastSeenTime getTime = new LastSeenTime();
                    long last_seen = Long.parseLong(online);
                    String lastSeenDisplayTime = getTime.getTimeAgo(last_seen, getApplicationContext());
                    userLastSeen.setText(lastSeenDisplayTime);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Send message
        sendMessageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! TextUtils.isEmpty(inputText.getText().toString())){
                    try{
                        sendType = 1;
                        mSignCrypt = new SignCryptMessages();
                        mSignCrypt.execute();

                    }catch (Exception e){

                    }
                }
            }
        });

        selectImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_GET_CONTENT);
                mIntent.addCategory(Intent.CATEGORY_OPENABLE);
                mIntent.setType("image/*");
                startActivityForResult(mIntent,GALLERY_PICK);
            }
        });

        imgSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_GET_CONTENT);
                mIntent.setType("*/*");
                startActivityForResult(mIntent,SEND_FILE);
            }
        });
        imgVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(ChatActivity.this, VideoCallActivity.class);
                mIntent.putExtra("user_name_video_call", userName);
                startActivity(mIntent);
            }
        });


    }
    // Nhan tin nhan send -> recevei
    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded( DataSnapshot dataSnapshot, String s) {

                        pMessagesUser = dataSnapshot.getValue(MessagesUser.class);
                        // Add public key
                        pMessagesUser.setPublic_key_receiver(pkey0_key);
                        pMessagesUser.setPublic_key_sender(pkey0_key_send);
                        messageList.add(pMessagesUser);
                        messageAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public void Anhxa(){
        mToolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        //Connect ActionBar to Activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        userNameTitle = (TextView) findViewById(R.id.txt_name_user);
        userLastSeen = (TextView) findViewById(R.id.txt_last_seen);
        mCircleImageView = (CircleImageView) findViewById(R.id.custom_profile_image);
        rootView = findViewById(R.id.root_view_chat);
        emojiButton = (ImageView) findViewById(R.id.emoji_btn_chat);


        userNameTitle.setText(messageReceiverName);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);

        rootName = FirebaseDatabase.getInstance().getReference().child("Users");
        rootName.keepSynced(true);

        PostFileRef = FirebaseStorage.getInstance().getReference();

        sendMessageView = (ImageView) findViewById(R.id.img_send);
        selectImageView = (ImageView) findViewById(R.id.img_select);
        inputText = (EmojiconEditText) findViewById(R.id.edt_send_message);

        emojIcon = new EmojIconActions(getApplicationContext(), rootView, inputText,emojiButton );
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });

        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });

        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputText.getText().toString().length() == 0){
                    sendMessageView.setImageResource(R.drawable.ic_send_black_24dp);
                }else {
                    sendMessageView.setImageResource(R.drawable.ic_send_blue_24dp);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid(); // nguoi gui tin

        //===========================
        KeyReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mKeyOption = new KeyOption();

        imgSendFile = (ImageView) findViewById(R.id.img_attach_file);
        mViewMessage = new ProgressDialog(this);

        imgVideoCall = (ImageView) findViewById(R.id.img_camera);

        //============================
        rootName.child(messageSenderId).child("user_name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               userName = dataSnapshot.getValue().toString();
                //Toast.makeText(ChatActivity.this, userName , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage(){
        String messageText = inputText.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this, "message", Toast.LENGTH_SHORT).show();
        }else {
            //Toast.makeText(ChatActivity.this, messageSenderId, Toast.LENGTH_SHORT).show(); // nguoi gui tin nhan
            // Tao node gui tin

            String message_send_ref ="Messages/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref ="Messages/" + messageReceiverId + "/" + messageSenderId;

            //  node messages - tao hai HashMap luu tru  message
            DatabaseReference user_message_key = rootRef.child("Messages")
                    .child(messageSenderId).child(messageReceiverId).push();

            String message_push_id = user_message_key.getKey(); // Id noi dung ban tin

            Log.d("push_id", message_push_id);

            // Noi dung tin nhan
            Map messageTextBody = new HashMap();
            messageTextBody.put("messages", messageText);


            messageTextBody.put("seen", false);
            messageTextBody.put("type", "text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from", messageSenderId);

            // Noi dung ben gui - ben nhan
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_send_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Log.d("Chatlog" , databaseError.getMessage().toString());
                    }
                    inputText.setText("");
                }
            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Send images
        if (requestCode == GALLERY_PICK && resultCode ==RESULT_OK && data != null){

            mDialog.setTitle("Đang gửi ảnh!");
            mDialog.show();
            // Upload ca buc anh
            Uri ImageUri = data.getData();
            final String message_send_ref ="Messages/" + messageSenderId + "/" + messageReceiverId;
            final String message_receiver_ref ="Messages/" + messageReceiverId + "/" + messageSenderId;

            //  node messages - tao hai HashMap luu tru  message
            DatabaseReference user_message_key = rootRef.child("Messages")
                    .child(messageSenderId).child(messageReceiverId).push();
            final String message_push_id = user_message_key.getKey(); // Id noi dung ban tin
            Log.d("push_id", message_push_id);

            //Upload image full size len server
            StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg");

            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        // Noi dung tin nhan
                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        Map messageTextBody = new HashMap();
                        messageTextBody.put("messages", downloadUri);
                        messageTextBody.put("seen", false);
                        messageTextBody.put("type", "image");
                        messageTextBody.put("time", ServerValue.TIMESTAMP);
                        messageTextBody.put("from", messageSenderId);

                        // Noi dung ben gui - ben nhan
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(message_send_ref + "/" + message_push_id, messageTextBody);
                        messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);
                        rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null){
                                    Log.d("Chat_Log", databaseError.getMessage().toString());
                                }
                                inputText.setText("");
                                mDialog.dismiss();
                            }
                        });
                        mDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "Đã gửi ảnh!", Toast.LENGTH_SHORT).show();
                    }else {
                        mDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "Lỗi! Kiểm tra kết nối mạng! ", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        else if (requestCode == SEND_FILE && resultCode == RESULT_OK && data != null){
            // Text => String => iconText file.
            sendType = 2;
            String selectFile = data.getData().getPath();
            fileName = selectFile.substring(selectFile.lastIndexOf("/") + 1);
            String extendsion = selectFile.substring(selectFile.lastIndexOf("."));

            //Log.d("Name_file", extendsion);
            // Kiem tra tep tin .txt
            if (extendsion.equals(".txt")){
                selectFile = selectFile.substring(selectFile.lastIndexOf(":") +1);
                try{
                    File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+selectFile);
                    // Lay kich thuoc tep tin
                    int filesize = Integer.parseInt(String.valueOf(myFile.length()/1024));
                    //Log.d("Name_file", Integer.toString(filesize));
                    //
                    if (filesize <= 20){

                        mDialog.setTitle("Đang gửi!");
                        mDialog.show();

                        FileInputStream fileInputStream = new FileInputStream(myFile);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                        String aDataRow = "";
                        String aBuffer = "";
                        while( (aDataRow = bufferedReader.readLine()) != null){
                            aBuffer += aDataRow ;
                            //Log.d("txtlol", aBuffer);
                        }
                        fileSend = aBuffer;
                        bufferedReader.close();
                        //Log.d("txt_content", fileSend);
                        //Log.d("lenght_file_send", Integer.toString(fileSend.length()));

                        if (fileSend.length() != 0){
                            try{
                                mSignCryptFile = new SignCryptMessages();
                                mSignCryptFile.execute();
                            }catch (Exception e){
                                Log.d("Errr", e.getMessage().toString());
                            }
                            mDialog.dismiss();
                        }else{
                            Toast.makeText(ChatActivity.this, "Tệp rỗng! Xin tạo tệp!", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }else{
                        Toast.makeText(this, "Tệp kích thước lớn!", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d("Errr", e.getMessage().toString());
                }
            }else if ( extendsion.equals(".pdf") || extendsion.equals(".docx")
                    || extendsion.equals(".rar") || extendsion.equals(".xls")
                    || extendsion.equals(".doc") || extendsion.equals(".xlsx")
                    || extendsion.equals(".mp3") || extendsion.equals(".mp4")){
                sendType = 3;
                // Random key
                DateFormat df = new SimpleDateFormat("d MMM yyyy");
                saveCurrentDate = df.format(Calendar.getInstance().getTime());
                DateFormat time = new SimpleDateFormat("HH:mm:ss");
                saveCurrentTime = time.format(Calendar.getInstance().getTime());
                postRandomName = saveCurrentDate + saveCurrentTime;

                mDialog.setTitle("Đang gửi tệp!");
                mDialog.show();
                Uri fileUri = data.getData();
                StorageReference filePath = PostFileRef.child("Post_File").child(postRandomName + fileUri.getLastPathSegment());

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            downLoadFile = task.getResult().getDownloadUrl().toString();
                            try{
                                mSignCryptFile = new SignCryptMessages();
                                mSignCryptFile.execute();
                            }catch (Exception e){

                            }
                            mDialog.dismiss();
                        }else {
                            Toast.makeText(ChatActivity.this, "Err" + task.getException().toString() , Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }
                });

            }
        }
    }

    // Encrypt Present  char state[] - 4 hex ,  char [] key - 4 hex
    private void Present( char state[], char [] key ){
        // o vong nao tao khoa vong do

        final char  sBox4[] ={0xc,0x5,0x6,0xb,0x9,0x0,0xa,0xd,0x3,0xe,0xf,0x8,0x4,0x7,0x1,0x2};

//        final char  state[]={ 0xd27c, 0xd2c6, 0xf35e,0x0b90};
//        char key[]={0x0031, 0x0032, 0x0033, 0x0034, 0x0035};
        char i ;
        char position = 0;
        char element_source = 0;
        char bit_source = 0;
        char element_destination= 0;
        char bit_destination	= 0;
        char [] temp_pLayer = new char[4];
        char round;
        char save1;
        char save2;
        round = 0;
        do{
            i = 0;
            do{
                state[i] ^= key[i+1];
                i++;
            }while(i<4);

            for(i=0;i<4;i++){
                state[i]= (char) (sBox4[(state[i]&0xF000)>>12]<<12|sBox4[(state[i]&0xF00)>>8]<<8|sBox4[(state[i]&0xF0)>>4]<<4|sBox4[state[i]&0xF]);
                //printf("%04x",state[i]);
            }
            for(i=0;i<4;i++)
            {
                temp_pLayer[i] = 0;
            }
            //printf("\n");
    /**/
            for(i=0;i<64;i++)
            {
                position = (char) ((char)(16*i) % 63);						//Artithmetic calculation of the pLayer
                if(i == 63)									//exception for bit 63
                    position = 63;
                element_source		= (char) (i / 16);
                bit_source 			= (char) ( i % 16);
                element_destination	= (char) (position / 16);
                bit_destination 	= (char) (position % 16);
                temp_pLayer[element_destination] |= ((state[element_source]>>bit_source) & 0x1) << (bit_destination);
            }
            for(i=0;i<4;i++)
            {
                state[i] = temp_pLayer[i];
                //printf("%04x",state[i]);
            }
    /**/
            save1 = key[0];
            save2 = key[1];
            i = 0;
            do
            {
                key[i]= (char) (key[i+1]>>3|key[i+2]<<13);
                i++;
            }while(i<3);
            key[3]= (char)( key[4]>>3 |save1<<13);
            key[4]=  (char)(save1>>3|save2<<13);
            key[4] =  (char)(sBox4[key[4]>>12]<<12 | (key[4] & 0xFFF));

            if((round+1) % 2 == 1)							//round counter addition
                key[0] ^= 32768;
            key[1] = (char) ((((round+1)>>1) ^ (key[1] & 15)) | (key[1] & 65520));
            round++;
        }while(round<31);
        i = 0;
        do{
            state[i] ^= key[i+1];
            i++;
        }while(i<4);

    }

    // SignCrypt - long [] k : key [] k global, char [] mess: message 4 hex,
    // aa: pkey0_user, a: Random num , x: pkey1_user, p: 9241
    // return 4 element encrypt format int number A B C D .
    private String mSignCrypt( long [] k , char [] mess , long aa , long a , long x , long p){

        String msg = String.valueOf(mess);
        String mSHA256  = mKeyOption.SHA256(msg);
        char [] sSHA256 = mSHA256.toCharArray();
        // Tinh r day len firebase

        long [] ri = new long[64];
        long r = 0;
        for ( int i = 0 ; i < 64 ; i++ ){
            ri[i] = sSHA256 [i] ^ k[0];
            r = r ^ ri[i];
        }
        String rCipher = Long.toString(r);
        rEnBuffer.append(rCipher).append(' ');
        //CipherReference.child(online_user_id).child("user_cipher_r").setValue(rCipher);

        //Log.d("comR", Long.toString(r));

        // Tinh s day len fire base
        long s1 = aa - a - r;
        //BigInteger s = new BigInteger(Integer.toString(x));
        long s = x;
        for ( int i =1 ; i <= s1 ; i++){
            s = mKeyOption.chebyshev1(3, s, p);
        }

        String sCipher = Long.toString(s);
        sEnBuffer.append(sCipher).append(' ');
        //CipherReference.child(online_user_id).child("user_cipher_s").setValue(sCipher);
        //Log.d("comS", Long.toString(s));

        char [] key = new char[5] ;
        for ( int i =0 ; i < 5 ; i++){
            key[i] = 0x0000;
        }
//      int [] key = {0x00, 0x00, 0x00, 0x00, 0x00 , 0x00, 0x00, 0x00 , 0x00 , 0x00};
        // int -> char
        // Biginterger -> char
        // Khoa 80bit co dinh

        try{
            int j = 0;
            while (k[1] > 0){
                key[4-j] = (char) (k[1] % 100);
                k[1] = k[1]/100;
                j = j+1;
                if ( j == 5) break;
            }
        }catch (Exception e){
            Log.d("fuck", e.getMessage().toString());
        }

        for ( int i =0 ; i < 5 ; i++){
            String ss = String.valueOf(key[i]);
            //Log.d("mKey", Integer.toHexString(key[i]));
            //Log.d("mKey", ss);
        }

        float lenght = (float) mess.length;
        int b = (int) Math.ceil(lenght/4);
        //Log.d("bIndex", Integer.toString(b));
        char [][] submess = new char[8][4];
        for (int m = 0 ; m < 8; m++){
            for ( int n = 0; n < 4; n++  ){
                submess[m][n] = 0x0000;
            }
        }
        int z = 0, count = 0;
        StringBuilder bufferCipher = new StringBuilder();
        for ( int rol = 0 ; rol < b ; rol++){
            for( int col = 0; col < 4; col ++ , z++ ){
                submess[rol][col] = mess[z];
            }
//            AES-128
//            String sKey = String.valueOf(key);
//            String  s_ = String.valueOf( submess[rol]);
//              EncryptAES(s_, sKey);


            Present (submess[rol],  key );
            for ( int rc = 0 ; rc < 4 ; rc ++){
                // Luu noi dung submess len firebase
                count = count + 1;
                if ( count == mess.length){
                    break;
                }
            }
        }

        for ( int n = 0; n < 4; n++  ){
            //Log.d("Submess", Integer.toString(submess[0][n]));
            bufferCipher.append(Integer.toString(submess[0][n])).append(' ');
            //Log.d("Submess", Integer.toHexString(submess[0][n]));
        }
        //CipherReference.child(online_user_id).child("user_cipher_c").setValue(bufferCipher.toString());
        // 4 character encrypt
        return bufferCipher.toString();
    }

    // Show elements of array format character
    private void ShowElementArray( char [] Arr){
        for (int i = 0; i < Arr.length ; i++){
            Log.d("EleArr", Character.toString(Arr[i]));
        }
    }
    //==================================================================================================================
    // Ky ma tin nhan
    private class SignCryptMessages extends AsyncTask< Void, Void, Void >{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sDialog = ProgressDialog.show(ChatActivity.this, "Đang gửi...", "Xin chờ");
        }
        // Send id - > Receiver Id

        @Override
        protected Void doInBackground(Void... params) {
            KeyReference.child(messageReceiverId).child("user_key")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            publicKeyReceiver = dataSnapshot.getValue().toString();
                            //Log.d("getKey", publicKeyReceiver);
                            KeyReference.child(messageSenderId).child("user_key")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            privateKeySend = dataSnapshot.getValue().toString();
                                            //Log.d("getKey", privateKeySend);
                                            try{

                                                int indexPrivateKey = privateKeySend.indexOf('_');
                                                //Toast.makeText(LoginActivity.this, Integer.toString(indexPrivateKey), Toast.LENGTH_SHORT).show();

                                                String key0_User = privateKeySend.substring(0, indexPrivateKey);
                                                String key1_User = privateKeySend.substring(indexPrivateKey +1, indexPrivateKey +5);
                                                String key2_User = privateKeySend.substring(indexPrivateKey +6, privateKeySend.length());
                                                String mKeyUser = key0_User + key1_User + key2_User;

                                                int indexPublicKey = publicKeyReceiver.indexOf('_');
                                                String key0_Server = publicKeyReceiver.substring(0, indexPublicKey);
                                                String key1_Server = publicKeyReceiver.substring(indexPrivateKey +1 , indexPublicKey +5);
                                                String key2_Server = publicKeyReceiver.substring(indexPrivateKey +6, publicKeyReceiver.length());
                                                String mKey_Server = key0_Server + key1_Server + key2_Server;
                                                //Toast.makeText(LoginActivity.this, mKeyUser + " " + mKey_Server, Toast.LENGTH_SHORT).show();

                                                //long pKeyUser = Long.parseLong(mKeyUser);
                                                //long pKeyPublic = Long.parseLong(mKey_Server);

                                                long pkey0_user = Long.parseLong(key0_User);
                                                long pkey1_user = Long.parseLong(key1_User);
                                                //long pkey2_user = Long.parseLong(key2_User);

                                                long pkey0_server = Long.parseLong(key0_Server);
                                                //long pkey1_server = Long.parseLong(key1_Server);
                                                long pkey2_server = Long.parseLong(key2_Server);
                                                // Set key de giai ma
                                                pMessagesUser.setPublic_key_receiver(pkey0_server);

                                                try{
                                                    // Encrypt message
                                                    if (sendType == 1){
                                                        messageEncrypt = inputText.getText().toString();
                                                    } else if ( sendType == 2 ){
                                                        messageEncrypt = fileSend;
                                                    }else if (sendType == 3){
                                                        messageEncrypt = fileName;
                                                    }
                                                    //===================================================================
                                                    // Chuyen doi du lieu ve kieu mang
                                                    long start = System.nanoTime();

                                                    char [] encrypt = messageEncrypt.toCharArray();

                                                    long end1 = System.nanoTime();

                                                    Log.d("ectimed1", Long.toString(end1 - start));

                                                    int mlenght = encrypt.length;
                                                   // Log.d("lenght_array_crypt", Integer.toString(mlenght));

                                                    //====================================================================
                                                    //Them cac ky tu 0 vao mang can ma hoa

                                                    char [] encrypt1 = {'0'};
                                                    switch (mlenght%4){
                                                        case 0 :
                                                                encrypt1 = Arrays.copyOf(encrypt, mlenght);
                                                                //ShowElementArray(encrypt1);
                                                                break;
                                                        case 1:
                                                                encrypt1 = Arrays.copyOf(encrypt, mlenght +3);
                                                                encrypt1[mlenght] = encrypt1 [mlenght+1] = encrypt1[mlenght+2] = '0';
                                                                //ShowElementArray(encrypt1);
                                                                break;
                                                        case 2:
                                                                encrypt1 = Arrays.copyOf(encrypt, mlenght +2);
                                                                encrypt1 [mlenght] = encrypt1 [mlenght+1] = '0';
                                                                //ShowElementArray(encrypt1);
                                                                break;
                                                        case 3:
                                                                encrypt1 = Arrays.copyOf(encrypt, mlenght +1);
                                                                encrypt1[mlenght]  = '0';
                                                                //ShowElementArray(encrypt1);
                                                                break;
                                                    }
                                                    //=========================
                                                   // Log.d("enLenght", Integer.toString(encrypt1.length));
                                                    long end2 = System.nanoTime();
                                                    long start1 = System.currentTimeMillis();

                                                    Log.d("ectimed2", Long.toString( end2 - end1));

                                                    String user_index_padding = Integer.toString( encrypt1.length - mlenght);

                                                    String user_cipher_c_index = Integer.toString(encrypt1.length);

                                                    int a = 0;
                                                    //String cs = null;
                                                    // Tinh R, S quy ve 1

                                                    char [] mess = {0x0000, 0x0000, 0x0000, 0x0000};
                                                    for( int i = 1; i <= encrypt1.length; i++ ){
                                                        if( (i%4) == 0 ){

                                                            mess[0] = encrypt1[i -4];
                                                            mess[1] = encrypt1[i -3];
                                                            mess[2] = encrypt1[i -2];
                                                            mess[3] = encrypt1[i -1];

                                                            a = mKeyOption.Random(9241);
                                                            mKeyOption.ComputeK(pkey0_user,pkey2_server, a ,pkey1_user, 9241, k);
                                                            cEnBuffer.append(mSignCrypt(k, mess, pkey0_user, a, pkey1_user, 9241));
                                                        }
                                                    }
                                                    //==================
                                                    long end3 = System.nanoTime();

                                                    Log.d("ectimed3", Long.toString(end3 - end2));
                                                    Log.d("ectimed3C", Long.toString(System.currentTimeMillis() - start1));

                                                    long aesIndex = (System.currentTimeMillis() -start1) * indexAes;
                                                    Log.d("ectimed4", Long.toString(end3 - start));

                                                    //aes
                                                    //Log.d("ectime_aes", Long.toString(aesIndex));
                                                    //=============================================================================================
                                                    // Tao node gui tin
                                                    String message_send_ref ="Messages/" + messageSenderId + "/" + messageReceiverId;
                                                    String message_receiver_ref ="Messages/" + messageReceiverId + "/" + messageSenderId;

                                                    DatabaseReference user_message_key = rootRef.child("Messages")
                                                            .child(messageSenderId).child(messageReceiverId).push();

                                                    String message_push_id = user_message_key.getKey(); // Id noi dung ban tin
                                                    //Log.d("push_id", message_push_id);

                                                    // Noi dung tin nhan
                                                    Map messageTextBody = new HashMap();

                                                    messageTextBody.put("user_cipher_c", cEnBuffer.toString());
                                                    messageTextBody.put("user_cipher_c_index", user_cipher_c_index);
                                                    messageTextBody.put("user_index_padding", user_index_padding);
                                                    messageTextBody.put("user_cipher_r", rEnBuffer.toString());
                                                    messageTextBody.put("user_cipher_s", sEnBuffer.toString());

                                                    messageTextBody.put("seen", false);

                                                    // Send text or file encrypt
                                                    if (sendType == 1){
                                                        messageTextBody.put("type", "text");
                                                    }else if (sendType == 2){
                                                        messageTextBody.put("type", "txt");
                                                    }else if (sendType == 3){
                                                        messageTextBody.put("type", "other");
                                                        messageTextBody.put("messages", downLoadFile);
                                                    }

                                                    messageTextBody.put("time", ServerValue.TIMESTAMP);
                                                    messageTextBody.put("from", messageSenderId);

                                                    // Noi dung ben gui - ben nhan
                                                    Map messageBodyDetails = new HashMap();
                                                    messageBodyDetails.put(message_send_ref + "/" + message_push_id, messageTextBody);
                                                    messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                                                    rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError != null){
                                                                Log.d("Chatlog" , databaseError.getMessage().toString());
                                                            }
                                                            inputText.setText("");
                                                            cEnBuffer.setLength(0);
                                                            rEnBuffer.setLength(0);
                                                            sEnBuffer.setLength(0);
                                                        }
                                                    });

                                                }catch (Exception e){
                                                    Log.d("Errr", e.getMessage().toString());
                                                }

                                            }catch (Exception e){
                                                Log.d("Errr", e.getMessage().toString());
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            sDialog.dismiss();
        }
    }

    //==================================================================================================================
    // Nhan tin nhan
    private class ReceiverMessage extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mReceiver = ProgressDialog.show(ChatActivity.this, "Loading...", "Wait");
        }

        @Override
        protected Void doInBackground(Void... params) {
               try{
                   //Thread.sleep(3000);
                   FetchMessages();
               }catch (Exception e){
                   Log.d("Errrget",e.getMessage().toString() );
               }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mReceiver.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//========================================================================================
//AES - 128
//    private SecretKeySpec GenerateKey(String password){
//        SecretKeySpec secretKeySpec = null;
//        try{
//            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] bytes = password.getBytes("UTF-8");
//            digest.update(bytes, 0 , bytes.length);
//
//            byte [] key = digest.digest();
//
//            Log.d("keyLenght", Integer.toString(key.length));
//
//            secretKeySpec = new SecretKeySpec(key, "AES");
//
//        }catch (Exception e){
//            Log.d("Err", e.getMessage().toString());
//        }
//        return secretKeySpec;
//    }
//
//    private String EncryptAES( String Data, String password){
//        String encrypt = null;
//        try {
//            SecretKeySpec key = GenerateKey(password);
//            //Log.d("keyValue", key.toString());
//            Cipher c = Cipher.getInstance("AES");
//            c.init(Cipher.ENCRYPT_MODE, key);
//            byte[] val = c.doFinal(Data.getBytes());
//            encrypt = Base64.encodeToString(val, Base64.DEFAULT);
//        }catch (Exception e){
//            Log.d("Err", e.getMessage().toString());
//        }
//        return  encrypt;
//
//    }
//
//    private String DecryptAES(String output, String password){
//        String decrypted = null;
//        try{
//            SecretKeySpec key = GenerateKey(password);
//            Cipher c = Cipher.getInstance("AES");
//            c.init(Cipher.DECRYPT_MODE, key);
//            byte [] decodedValue = Base64.decode(output, Base64.DEFAULT);
//            byte [] decValue = c.doFinal(decodedValue);
//            decrypted = new String(decValue);
//        }catch (Exception e){
//            Log.d("Err", e.getMessage().toString());
//        }
//
//        return decrypted;
//    }

}
