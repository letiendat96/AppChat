package com.ltd.admin.appchat.Hardware;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ltd.admin.appchat.R;

import io.feeeei.circleseekbar.CircleSeekBar;

public class HardwareActivity extends AppCompatActivity {

    CircleSeekBar circleSeekBarTemp, circleSeekBarHumid;
    SeekBar seekBarTemp, seekBarHumid;
    TextView txtNotice, txtTemp, txtHumid, txtTempSetting, txtHumidSetting;
    EditText edtMessage;
    ImageButton btnLamp, btnFan;
    ImageView imgSend;
    DatabaseReference mReference, mValue;

    public String mTemp, mHumid;
    public  boolean lamp = false, fan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware);
        Anhxa();
        GetTempHumid();

        seekBarTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    txtTempSetting.setText(String.valueOf(progress));

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mValue.child("VTemp").setValue(txtTempSetting.getText().toString(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){
                            txtNotice.setText("Đã cài đặt nhiệt độ.");
                        }else {
                            Toast.makeText(HardwareActivity.this, "Err", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        seekBarHumid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    txtHumidSetting.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mValue.child("VHumid").setValue(txtHumidSetting.getText().toString(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){
                            txtNotice.setText("Đã cài đặt độ ẩm.");
                        }else {
                            Toast.makeText(HardwareActivity.this, "Err", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        btnLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lamp){
                    btnLamp.setImageResource(R.drawable.lampoff);
                    mReference.child("Lamp").setValue("0", new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                txtNotice.setText("Đèn đã tắt");
                            }else {
                                Toast.makeText(HardwareActivity.this, "Err", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    lamp = false;
                }else {
                    btnLamp.setImageResource(R.drawable.lampon);
                    mReference.child("Lamp").setValue("1", new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                txtNotice.setText("Đèn đã bật");
                            }else {
                                Toast.makeText(HardwareActivity.this, "Err", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    lamp = true;
                }
            }
        });

        btnFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fan){
                    btnFan.setImageResource(R.drawable.fanoff);
                    mReference.child("Fan").setValue("0", new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                txtNotice.setText("Quạt đã tắt");
                            }else {
                                Toast.makeText(HardwareActivity.this, "Err", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    fan = false;
                }else {
                    btnFan.setImageResource(R.drawable.fanon);
                    mReference.child("Fan").setValue("1", new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                txtNotice.setText("Quạt đã bật");
                            }else {
                                Toast.makeText(HardwareActivity.this, "Err", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    fan = true;
                }
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtMessage.getText().length() == 0){
                    imgSend.setImageResource(R.drawable.ic_send_black_24dp);
                }else {
                    imgSend.setImageResource(R.drawable.ic_send_blue_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValue.child("Message").setValue(edtMessage.getText().toString(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){
                            Toast.makeText(HardwareActivity.this, "Đã gửi tin nhắn!", Toast.LENGTH_SHORT).show();
                            edtMessage.setText("");
                        }
                    }
                });

            }
        });
    }

    public void Anhxa(){
        circleSeekBarTemp = (CircleSeekBar) findViewById(R.id.seekbar_temp);
        circleSeekBarHumid = (CircleSeekBar) findViewById(R.id.seekbar_humid);
        txtTemp = (TextView) findViewById(R.id.txt_temp_value);
        txtHumid = (TextView) findViewById(R.id.txt_humid_value);
        txtNotice = (TextView) findViewById(R.id.txt_notice);
        seekBarTemp = (SeekBar) findViewById(R.id.seek_temp);
        seekBarHumid = (SeekBar) findViewById(R.id.seek_humid);
        btnLamp = (ImageButton) findViewById(R.id.btn_lamp);
        btnFan = (ImageButton) findViewById(R.id.btn_fan);
        edtMessage = (EditText) findViewById(R.id.edt_send_message);
        imgSend = (ImageView) findViewById(R.id.img_send);
        txtTempSetting = (TextView) findViewById(R.id.txt_temp_setting);
        txtHumidSetting = (TextView) findViewById(R.id.txt_humid_setting);

        mReference = FirebaseDatabase.getInstance().getReference().child("DHT11");
        mValue = FirebaseDatabase.getInstance().getReference().child("Value");

    }
    public void GetTempHumid(){
        mReference.child("Temp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mTemp = dataSnapshot.getValue().toString();
                    txtTemp.setText(mTemp + " °C");
                    circleSeekBarTemp.setCurProcess(Integer.parseInt(mTemp));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mReference.child("Humid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mHumid = dataSnapshot.getValue().toString();
                    txtHumid.setText(mHumid + " %");
                    circleSeekBarHumid.setCurProcess(Integer.parseInt(mHumid));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
