package com.ltd.admin.appchat;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;

public class VideoCallActivity extends AppCompatActivity implements Connector.IConnect{

    private Connector vc;
    private FrameLayout videoFrame;
    private ImageButton btnVideoCall, btnVideo;
    private boolean connect = true;
    private String userVideoCall;

//    MediaRecorder mediaRecorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        ConnectorPkg.setApplicationUIContext(this);
        ConnectorPkg.initialize();

        videoFrame = (FrameLayout)findViewById(R.id.videoFrame);
        btnVideoCall = (ImageButton) findViewById(R.id.btn_video_call);
        btnVideo = (ImageButton) findViewById(R.id.btn_video);
        btnVideoCall.setVisibility(View.INVISIBLE);

        userVideoCall = getIntent().getExtras().get("user_name_video_call").toString();
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Start();
                btnVideoCall.setVisibility(View.VISIBLE);
                btnVideo.setVisibility(View.INVISIBLE);
            }
        });

        btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connect){
                    btnVideoCall.setImageResource(R.drawable.ic_videocam_off_black_24dp);
                    Connect();
                    connect = false;
                }else{
                    btnVideoCall.setImageResource(R.drawable.ic_videocam_black_24dp);
                    Disconnect();
                    connect = true;
                }
            }
        });
    }

    public void Start() {
        try{
            vc = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 15, "warning info@VidyoClient info@VidyoConnector", "", 0);
            vc.showViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());
        }catch (Exception e){
            Log.d("Err", e.getMessage().toString());
        }
    }

    public void Connect() {
        try{
            String token ="cHJvdmlzaW9uAHVzZXIxQDc2MTRhOS52aWR5by5pbwA2MzY5NjQzOTU4NwAAMzJhMWMzODdlNDNkNTdkYTc5YzQxNTNiMjFmOTVhYWYyYWJmMjNjYzQ4MDdjYmZkODBhN2YyYzczOTA3ZWU1ZDEzYzI1NjdkNDMxYjE4MTAyYThjNDllZDc2MTc5Y2Ni";
            vc.connect("prod.vidyo.io", token, userVideoCall, "DemoRoom", this);
        }catch (Exception e){
            Log.d("Err", e.getMessage().toString());
        }
    }

    public void Disconnect() {
        try {
            vc.disconnect();
        }catch (Exception e){
            Log.d("Err", e.getMessage().toString());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Disconnect();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Disconnect();
        finish();
    }

    @Override
    public void onSuccess() {
    }

    @Override
    public void onFailure(Connector.ConnectorFailReason connectorFailReason) {
    }

    @Override
    public void onDisconnected(Connector.ConnectorDisconnectReason connectorDisconnectReason) {
    }

}
