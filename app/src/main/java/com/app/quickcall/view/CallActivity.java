package com.app.quickcall.view;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import com.app.quickcall.R;
import com.app.quickcall.databinding.ActivityCallBinding;
import com.app.quickcall.repository.MainRepository;
import com.app.quickcall.utils.DataModelType;

public class CallActivity extends AppCompatActivity implements MainRepository.Listener {

    private ActivityCallBinding views;
    private MainRepository mainRepository;
    private Boolean isCameraMuted = false;
    private Boolean isMicrophoneMuted = false;
    private String contactName;
    private boolean isCaller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        contactName = getIntent().getStringExtra("contact_name");
        isCaller = getIntent().getBooleanExtra("is_caller", false);

        mainRepository = MainRepository.getInstance();

        init();

    }

    public void startCall() {
        mainRepository.sendCallRequest(contactName,()->{
                Toast.makeText(this, "couldnt find the target", Toast.LENGTH_SHORT).show();
            });
    }

    private void init(){

        if (isCaller) {
            mainRepository.sendCallRequest(contactName,()->{
                Toast.makeText(this, "couldnt find the target", Toast.LENGTH_SHORT).show();
            });
        }

        if(mainRepository.webRtcClient.peerConnection == null) {
            mainRepository.initWebRtc(this);
        }

        mainRepository.initLocalView(views.localView);
        mainRepository.initRemoteView(views.remoteView);
        mainRepository.listener = this;

        mainRepository.subscribeForLatestEvent(data->{
            if (data.getType() == DataModelType.StartCall){
                runOnUiThread(()->{
                    views.incomingNameTV.setText(data.getSender()+" is Calling you");
                    views.incomingCallLayout.setVisibility(View.VISIBLE);

                    if (!isCaller) {
                        mainRepository.startCall(data.getSender());
                        views.incomingCallLayout.setVisibility(View.GONE);
                    }
//                    views.acceptButton.setOnClickListener(v->{
//                        //star the call here
//                        mainRepository.startCall(data.getSender());
//                        views.incomingCallLayout.setVisibility(View.GONE);
//                    });
                    views.rejectButton.setOnClickListener(v->{
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                });
            }
        });

        views.switchCameraButton.setOnClickListener(v->{
            mainRepository.switchCamera();
        });

        views.micButton.setOnClickListener(v->{
            if (isMicrophoneMuted){
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            } else {
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            mainRepository.toggleAudio(isMicrophoneMuted);
            isMicrophoneMuted=!isMicrophoneMuted;
        });

        views.videoButton.setOnClickListener(v->{
            if (isCameraMuted){
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
            } else {
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
            }
            mainRepository.toggleVideo(isCameraMuted);
            isCameraMuted=!isCameraMuted;
        });

        views.endCallButton.setOnClickListener(v->{
            mainRepository.endCall();
            finish();
        });
    }

    @Override
    public void webrtcConnected() {
        runOnUiThread(()->{
            views.incomingCallLayout.setVisibility(View.GONE);
            views.whoToCallLayout.setVisibility(View.GONE);
            views.callLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void webrtcClosed() {
        mainRepository.webRtcClient.peerConnection = null;
        runOnUiThread(this::finish);
    }
}