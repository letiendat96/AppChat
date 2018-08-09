package com.ltd.admin.appchat.LoginAccount;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.ltd.admin.appchat.R;
import com.ltd.admin.appchat.WelcomePage.StartPageActivity;
import com.ltd.admin.appchat.VerifyAccount.KeyOption;

public class LoginActivity extends AppCompatActivity {

    Toolbar loginToolbar;
    Button btnLogin, btnAuthenCation;
    EditText edtEmail;
//    ProgressDialog mDialog;
    FirebaseAuth mAuth;

//    private DatabaseReference UserReference;
//    private DatabaseReference KeyReference;
//    private DatabaseReference CipherReference;
      private DatabaseReference SetIdLoginReference;

    private KeyOption mKeyOption;
    private long [] k = new long[2];
//    private String privateKeyUser = null;
//    private String publicKeyServer = null;
    private String email = null;
    private String pass = null;
    private String authCode = null;

   // private SignCrypt mSignCrypt;

    private static String online_user_id = null;
//    private StringBuffer rEnBuffer = new StringBuffer();
//    private StringBuffer sEnBuffer = new StringBuffer();
//    private StringBuffer cEnBuffer = new StringBuffer();

    private static int GALLERY_PICK = 1;
    private String message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Anhxa();

        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle("LOGIN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 email = edtEmail.getText().toString();
                 pass = edtEmail.getText().toString(); // pass -> SHA256
                 if(TextUtils.isEmpty(email)){
                     Toast.makeText(LoginActivity.this, "You have not entered an email yet!", Toast.LENGTH_SHORT).show();
                 }else {
                     try{
                         mAuth.signInWithEmailAndPassword(email, pass)
                                 .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                     @Override
                                     public void onComplete(@NonNull Task<AuthResult> task) {
                                         if (task.isSuccessful()){
                                             //SetIdLoginReference.setValue(mAuth.getCurrentUser().getUid());
                                             Intent mIntent = new Intent(LoginActivity.this, VerifyPassActivity.class);
                                             mIntent.putExtra("email_login", email);
                                             mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                             startActivity(mIntent);

                                         }else {
                                             Toast.makeText(LoginActivity.this, "Unregistered account!", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 });
                     }catch (Exception e){
                         Log.d("Errr", e.getMessage().toString());
                     }

                 }

                 //authCode = edtAuthCode.getText().toString();
                 //LoginUser(email, authCode);
            }
        });
    }

    public void Anhxa(){
        loginToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        btnLogin = (Button) findViewById(R.id.btn_login_app);
        edtEmail = (EditText) findViewById(R.id.edit_text_email_id_login);
        //edtAuthCode = (EditText) findViewById(R.id.edit_text_password_login);
        mAuth = FirebaseAuth.getInstance();
        edtEmail.setText("letiendattob96@gmail.com");

        //edtAuthCode.setText("abc123");

//        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");
//        KeyReference = FirebaseDatabase.getInstance().getReference().child("Users");
//        CipherReference = FirebaseDatabase.getInstance().getReference().child("Users");
//        mKeyOption = new KeyOption();
//
         //SetIdLoginReference = FirebaseDatabase.getInstance().getReference().child("IdUserLogin");
//        mDialog = new ProgressDialog(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mIntent = new Intent(LoginActivity.this, StartPageActivity.class);
        startActivity(mIntent);

    }

    // Sign, Encrypt
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == GALLERY_PICK && resultCode ==RESULT_OK && data != null){
//            String selectFile = data.getData().getPath();
//            String fileName = selectFile.substring(selectFile.lastIndexOf("/") + 1);
//            selectFile = selectFile.substring(selectFile.lastIndexOf(":") +1);
//
//            try{
//                File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+selectFile);
//                FileInputStream fileInputStream = new FileInputStream(myFile);
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
//                String aDataRow = "";
//                String aBuffer = "";
//                while( (aDataRow = bufferedReader.readLine()) != null){
//                    aBuffer += aDataRow ;
//                    Log.d("txtlol", aBuffer);
//                }
//                //
//                authCode = aBuffer;
//                bufferedReader.close();
//
//            }catch (Exception e){
//                Log.d("txtlol", e.getMessage().toString());
//            }
//
//            email = edtEmail.getText().toString();
//            pass = edtEmail.getText().toString(); // pass -> SHA256
//            LoginUser(email, authCode);
//        }
//    }
//
//    // Login, signcrypt
//    private void LoginUser(String email, String authCode){
//        if(TextUtils.isEmpty(email)){
//            Toast.makeText(this, "Bạn chưa nhập email!", Toast.LENGTH_SHORT).show();
//        }else if(TextUtils.isEmpty(authCode)){
//            Toast.makeText(this, "Bạn chưa nhập mã xác nhận!", Toast.LENGTH_SHORT).show();
//        }else{
//            try{
//                mSignCrypt = new SignCrypt();
//                mSignCrypt.execute();
//            }catch (Exception e){
//
//            }
//            //Wait server respond status login
//        }
//    }
//
//    // Encrypt Present  char state[] - 4 hex ,  char [] key - 4 hex
//    private void Present( char state[], char [] key ){
//        final char  sBox4[] ={0xc,0x5,0x6,0xb,0x9,0x0,0xa,0xd,0x3,0xe,0xf,0x8,0x4,0x7,0x1,0x2};
//
////        final char  state[]={ 0xd27c, 0xd2c6, 0xf35e,0x0b90};
////        char key[]={0x0031, 0x0032, 0x0033, 0x0034, 0x0035};
//        char i;
//        char position = 0;
//        char element_source = 0;
//        char bit_source = 0;
//        char element_destination	= 0;
//        char bit_destination	= 0;
//        char [] temp_pLayer = new char[4];
//        char round;
//        char save1;
//        char save2;
//        round = 0;
//        do{
//            i = 0;
//            do{
//                state[i] ^= key[i+1];
//                i++;
//            }while(i<4);
//
//            for(i=0;i<4;i++){
//                state[i]= (char) (sBox4[(state[i]&0xF000)>>12]<<12|sBox4[(state[i]&0xF00)>>8]<<8|sBox4[(state[i]&0xF0)>>4]<<4|sBox4[state[i]&0xF]);
//                //printf("%04x",state[i]);
//            }
//            for(i=0;i<4;i++)
//            {
//                temp_pLayer[i] = 0;
//            }
//            //printf("\n");
//    /**/
//            for(i=0;i<64;i++)
//            {
//                position = (char) ((char)(16*i) % 63);						//Artithmetic calculation of the pLayer
//                if(i == 63)									//exception for bit 63
//                    position = 63;
//                element_source		= (char) (i / 16);
//                bit_source 			= (char) ( i % 16);
//                element_destination	= (char) (position / 16);
//                bit_destination 	= (char) (position % 16);
//                temp_pLayer[element_destination] |= ((state[element_source]>>bit_source) & 0x1) << (bit_destination);
//            }
//            for(i=0;i<4;i++)
//            {
//                state[i] = temp_pLayer[i];
//                //printf("%04x",state[i]);
//            }
//    /**/
//            save1 = key[0];
//            save2 = key[1];
//            i = 0;
//            do
//            {
//                key[i]= (char) (key[i+1]>>3|key[i+2]<<13);
//                i++;
//            }while(i<3);
//            key[3]= (char)( key[4]>>3 |save1<<13);
//            key[4]=  (char)(save1>>3|save2<<13);
//            key[4] =  (char)(sBox4[key[4]>>12]<<12 | (key[4] & 0xFFF));
//
//            if((round+1) % 2 == 1)							//round counter addition
//                key[0] ^= 32768;
//            key[1] = (char) ((((round+1)>>1) ^ (key[1] & 15)) | (key[1] & 65520));
//            round++;
//        }while(round<31);
//        i = 0;
//        do{
//            state[i] ^= key[i+1];
//            i++;
//        }while(i<4);
//
//    }
//
//    // SignCrypt - long [] k : key [] k global, char [] mess: message 4 hex,
//    // aa: pkey0_user, a: Random num , x: pkey1_user, p: 9241
//    // return 4 element encrypt format int number A B C D .
//    private String mSignCrypt( long [] k , char [] mess , long aa , long a , long x , long p){
//
//        String msg = String.valueOf(mess);
//        String mSHA256  = mKeyOption.SHA256(msg);
//        char [] sSHA256 = mSHA256.toCharArray();
//        // Tinh r day len firebase
//
//        long [] ri = new long[64];
//        long r = 0;
//        for ( int i = 0 ; i < 64 ; i++ ){
//            ri[i] = sSHA256 [i] ^ k[0];
//            r = r ^ ri[i];
//        }
//        String rCipher = Long.toString(r);
//        rEnBuffer.append(rCipher).append(' ');
//        //CipherReference.child(online_user_id).child("user_cipher_r").setValue(rCipher);
//
//        Log.d("comR", Long.toString(r));
//
//        // Tinh s day len fire base
//        long s1 = aa - a - r;
//        //BigInteger s = new BigInteger(Integer.toString(x));
//        long s = x;
//        for ( int i =1 ; i <= s1 ; i++){
//            s = mKeyOption.chebyshev1(3, s, p);
//        }
//
//        String sCipher = Long.toString(s);
//        sEnBuffer.append(sCipher).append(' ');
//        //CipherReference.child(online_user_id).child("user_cipher_s").setValue(sCipher);
//        Log.d("comS", Long.toString(s));
//
//        char [] key = new char[5] ;
//        for ( int i =0 ; i < 5 ; i++){
//            key[i] = 0x0000;
//        }
//
////      int [] key = {0x00, 0x00, 0x00, 0x00, 0x00 , 0x00, 0x00, 0x00 , 0x00 , 0x00};
//        try{
//            int j = 0;
//            while (k[1] > 0){
//                key[4-j] = (char) (k[1] % 100);
//                k[1] = k[1]/100;
//                j = j+1;
//                if ( j == 5) break;
//            }
//        }catch (Exception e){
//            Log.d("fuck", e.getMessage().toString());
//        }
//
//        for ( int i =0 ; i < 5 ; i++){
//            String ss = String.valueOf(key[i]);
//            Log.d("mKey", Integer.toHexString(key[i]));
//            Log.d("mKey", ss);
//        }
//
//        float lenght = (float) mess.length;
//        int b = (int) Math.ceil(lenght/4);
//        Log.d("bIndex", Integer.toString(b));
//        char [][] submess = new char[8][4];
//        for (int m = 0 ; m < 8; m++){
//            for ( int n = 0; n < 4; n++  ){
//                submess[m][n] = 0x0000;
//            }
//        }
//        int z = 0, count = 0;
//
//        for ( int rol = 0 ; rol < b ; rol++){
//            for( int col = 0; col < 4; col ++ , z++ ){
//                submess[rol][col] = (char) mess[z];
//            }
//            Present (submess[rol],  key );
//            for ( int rc = 0 ; rc < 4 ; rc ++){
//                // Luu noi dung submess len firebase
//                count = count + 1;
//                if ( count == mess.length){
//                    break;
//                }
//            }
//        }
//        StringBuffer bufferCipher = new StringBuffer();
//
//            for ( int n = 0; n < 4; n++  ){
//                Log.d("Submess", Integer.toString(submess[0][n]));
//                bufferCipher.append(Integer.toString(submess[0][n])).append(' ');
//                Log.d("Submess", Integer.toHexString(submess[0][n]));
//            }
//
//        //CipherReference.child(online_user_id).child("user_cipher_c").setValue(bufferCipher.toString());
//        // 4 character encrypt
//        return bufferCipher.toString();
//    }
//
//    // Class SignCrypt
//    private class SignCrypt extends AsyncTask<Void, Void, Void >{
//        @Override
//        protected void onPreExecute() {
//
//            mDialog = ProgressDialog.show(LoginActivity.this, "Đang đăng nhập!", "Xin chờ!", true);
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Void doInBackground(Void... aVoid) {
//
//            try{
//                mAuth.signInWithEmailAndPassword(email,pass)
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()){
//
//                                    online_user_id = mAuth.getCurrentUser().getUid();
//                                    SetIdLoginReference.setValue(online_user_id);
//
//                                    // Dung de canh bao user login tren nhieu thiet bi
//                                    String DeviceToken = FirebaseInstanceId.getInstance().getToken();
//
//                                    UserReference.child(online_user_id).child("device_token").setValue(DeviceToken)
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    if (task.isSuccessful()){
//                                                        // Lay K user, K server
//
//                                                        KeyReference.child(online_user_id).child("server_public_key")
//                                                                .addValueEventListener(new ValueEventListener() {
//                                                                    @Override
//                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                                                        // Kiem tra khoa K !=
//
//                                                                        publicKeyServer = dataSnapshot.getValue().toString();
//                                                                        //Toast.makeText(LoginActivity.this, publicKeyServer , Toast.LENGTH_SHORT).show();
//                                                                        Log.d("getKey", publicKeyServer);
//
//                                                                        KeyReference.child(online_user_id).child("user_key")
//                                                                                .addValueEventListener(new ValueEventListener() {
//                                                                                    @Override
//                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                                                                        privateKeyUser = dataSnapshot.getValue().toString();
//                                                                                        Log.d("getKey", privateKeyUser);
//                                                                                        try{
//
//                                                                                            int indexPrivateKey = privateKeyUser.indexOf('_');
//                                                                                            //Toast.makeText(LoginActivity.this, Integer.toString(indexPrivateKey), Toast.LENGTH_SHORT).show();
//
//                                                                                            String key0_User = privateKeyUser.substring(0, indexPrivateKey);
//                                                                                            String key1_User = privateKeyUser.substring(indexPrivateKey +1, indexPrivateKey +5);
//                                                                                            String key2_User = privateKeyUser.substring(indexPrivateKey +6, privateKeyUser.length());
//                                                                                            String mKeyUser = key0_User + key1_User + key2_User;
//
//                                                                                            int indexPublicKey = publicKeyServer.indexOf('_');
//                                                                                            String key0_Server = publicKeyServer.substring(0, indexPublicKey);
//                                                                                            String key1_Server = publicKeyServer.substring(indexPrivateKey +1 , indexPublicKey +5);
//                                                                                            String key2_Server = publicKeyServer.substring(indexPrivateKey +6, publicKeyServer.length());
//                                                                                            String mKey_Server = key0_Server + key1_Server + key2_Server;
//                                                                                            //Toast.makeText(LoginActivity.this, mKeyUser + " " + mKey_Server, Toast.LENGTH_SHORT).show();
//
//                                                                                            long pKeyUser = Long.parseLong(mKeyUser);
//                                                                                            long pKeyPublic = Long.parseLong(mKey_Server);
//
//                                                                                            long pkey0_user = Long.parseLong(key0_User);
//                                                                                            long pkey1_user = Long.parseLong(key1_User);
//                                                                                            long pkey2_user = Long.parseLong(key2_User);
//
//                                                                                            long pkey0_server = Long.parseLong(key0_Server);
//                                                                                            long pkey1_server = Long.parseLong(key1_Server);
//                                                                                            long pkey2_server = Long.parseLong(key2_Server);
//
//                                                                                            // Loi
//                                                                                            try{
//
//                                                                                                //String msg = "abc123456;f,g;ldf,g2sd2g]rơ]f[lgl1236784sa121jhj1gg2yg3y31e3wd";
//
////                                                                                                char [] encrypt = new char[100];
////                                                                                                for( int i =0 ; i < 100 ; i++ ){
////                                                                                                    Random mRandom = new Random();
////                                                                                                    encrypt[i] = (char) mRandom.nextInt(255);
//
//
//                                                                                                // Dau cach
//
//                                                                                                char [] encrypt = authCode.toCharArray();
//
//                                                                                                int mlenght = encrypt.length;
//                                                                                                Log.d("enLenght", Integer.toString(mlenght));
//
//                                                                                                char [] encrypt1 = {'0'};
//                                                                                                switch (mlenght%4){
//                                                                                                    case 0 :
//                                                                                                        try{
//                                                                                                            encrypt1 = Arrays.copyOf(encrypt, mlenght);
//                                                                                                            ShowElementArray(encrypt1);
//                                                                                                        }catch (Exception e){
//                                                                                                            Log.d("Errr", e.getMessage().toString());
//                                                                                                        } break;
//
//                                                                                                    case 1:
//                                                                                                        try{
//                                                                                                            encrypt1 = Arrays.copyOf(encrypt, mlenght +3);
//                                                                                                            encrypt1[mlenght] = encrypt1 [mlenght+1] = encrypt1[mlenght+2] = '0';
//                                                                                                            ShowElementArray(encrypt1);
//                                                                                                        }catch (Exception e){
//                                                                                                            Log.d("Errr", e.getMessage().toString());
//                                                                                                        } break;
//                                                                                                    case 2:
//                                                                                                        try{
//                                                                                                            encrypt1 = Arrays.copyOf(encrypt, mlenght +2);
//                                                                                                            encrypt1 [mlenght] = encrypt1 [mlenght+1] = '0';
//                                                                                                            ShowElementArray(encrypt1);
//                                                                                                        }catch (Exception e){
//                                                                                                            Log.d("Errr", e.getMessage().toString());
//                                                                                                        } break;
//                                                                                                    case 3:
//                                                                                                        try{
//                                                                                                            encrypt1 = Arrays.copyOf(encrypt, mlenght +1);
//                                                                                                            encrypt1[mlenght]  = '0';
//                                                                                                            ShowElementArray(encrypt1);
//                                                                                                        }catch (Exception e){
//                                                                                                            Log.d("Errr", e.getMessage().toString());
//                                                                                                        } break;
//                                                                                                }
//                                                                                                Log.d("enLenght", Integer.toString(encrypt1.length));
//
//                                                                                                String user_index_padding = Integer.toString( encrypt1.length - mlenght);
//
//                                                                                                String user_cipher_c_index = Integer.toString(encrypt1.length);
//                                                                                                int a = 0;
//                                                                                                String cs = null;
//
//                                                                                                char [] mess = {0x0000, 0x0000, 0x0000, 0x0000};
//                                                                                                for( int i = 1; i <= encrypt1.length; i++ ){
//                                                                                                    if( (i%4) == 0 ){
//
//                                                                                                        mess[0] = encrypt1[i -4];
//                                                                                                        mess[1] = encrypt1[i -3];
//                                                                                                        mess[2] = encrypt1[i -2];
//                                                                                                        mess[3] = encrypt1[i -1];
//
//                                                                                                        a = mKeyOption.Random(9241);
//                                                                                                        Log.d("Ran",Integer.toString(a));
//
//                                                                                                        mKeyOption.ComputeK(pkey0_user,pkey2_server, a ,pkey1_user, 9241, k);
//                                                                                                        cs = Long.toString(k[0]) + " " + Long.toString(k[1]);
//                                                                                                        // Toast.makeText(LoginActivity.this, cs, Toast.LENGTH_SHORT).show();
//                                                                                                        //Log.d("Keycs", cs);
//
//                                                                                                        String cipherText =  mSignCrypt(k, mess, pkey0_user, a, pkey1_user, 9241);
//                                                                                                        cEnBuffer.append(cipherText);
//                                                                                                        //Log.d("cipher", cipherText);
//                                                                                                    }
//                                                                                                }
//
////                                                                                                Log.d("aKey", Long.toString(pkey0_user));
////                                                                                                Log.d("bkey", Long.toString(pkey1_user));
////
////
////                                                                                                Log.d("rLenght", Integer.toString(rEnBuffer.length()));
////                                                                                                Log.d("sLenght", Integer.toString(sEnBuffer.length()));
////                                                                                                Log.d("cLenght", Integer.toString(cEnBuffer.length()));
//
//                                                                                                Map update_cipher_data = new HashMap();
//                                                                                                update_cipher_data.put("user_cipher_c", cEnBuffer.toString());
//                                                                                                update_cipher_data.put("user_cipher_c_index", user_cipher_c_index);
//                                                                                                update_cipher_data.put("user_index_padding", user_index_padding);
//                                                                                                update_cipher_data.put("user_cipher_r", rEnBuffer.toString());
//                                                                                                update_cipher_data.put("user_cipher_s", sEnBuffer.toString());
//
//                                                                                                CipherReference.child(online_user_id).updateChildren(update_cipher_data)
//                                                                                                        .addOnCompleteListener(new OnCompleteListener() {
//                                                                                                            @Override
//                                                                                                            public void onComplete(@NonNull Task task) {
//                                                                                                                if (task.isSuccessful()){
//                                                                                                                    //String s = "update";
//                                                                                                                    //Log.d("cipherUpdate",s );
//                                                                                                                    // Delete StringBuffer
//                                                                                                                    cEnBuffer.setLength(0);
//                                                                                                                    rEnBuffer.setLength(0);
//                                                                                                                    sEnBuffer.setLength(0);
//                                                                                                                }
//                                                                                                            }
//                                                                                                        });
//
////                                                                                                String cipherText =  mSignCrypt(k, mess, pkey0_user, a, pkey1_user, 9241);
////                                                                                                Log.d("cipher", cipherText);
//
//                                                                                            }catch (Exception e){
//                                                                                                Log.d("Errr", e.getMessage().toString());
//                                                                                            }
//                                                                                        }catch (Exception e){
//                                                                                            Log.d("Errr", e.getMessage().toString());
//                                                                                        }
//                                                                                    }
//                                                                                    @Override
//                                                                                    public void onCancelled(DatabaseError databaseError) {
//
//                                                                                    }
//                                                                                });
//                                                                    }
//
//                                                                    @Override
//                                                                    public void onCancelled(DatabaseError databaseError) {
//
//                                                                    }
//                                                                });
//                                                    }
//                                                }
//                                            });
//
//                                }else {
//
//                                }
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//
//                            }
//                });
//
//            }catch (Exception e ){
//                Log.d("Err", e.getMessage().toString());
//            }
//
//          return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//
//            // Check user_status_login => main activity
//            Handler mHandler = new Handler();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try{
//                        CipherReference.child(online_user_id).child("user_login_status").addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                //Toast.makeText(LoginActivity.this, dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
//                                if (dataSnapshot.getValue().equals("true")){
//
//                                    Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
//
//                                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(mIntent);
//                                    finish();
//
//                                }else {
//                                    //Toast.makeText(LoginActivity.this, "Kiểm tra lại email, mã xác nhận!", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }catch (Exception e){
//
//                    }
//                }
//            }, 10000);
//            mDialog.dismiss();
//            super.onPostExecute(aVoid);
//        }
//    }
//
//    // Show elements of array format character
//    private void ShowElementArray( char [] Arr){
//        for (int i = 0; i < Arr.length ; i++){
//            Log.d("EleArr", Character.toString(Arr[i]));
//        }
//    }

}
