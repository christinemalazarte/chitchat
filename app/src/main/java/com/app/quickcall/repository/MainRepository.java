package com.app.quickcall.repository;

import android.app.Activity;

import com.app.quickcall.model.CallModel;
import com.app.quickcall.remote.FirebaseClient;
import com.app.quickcall.utils.SuccessCallback;
import com.app.quickcall.webrtc.PeerConnectionObserver;
import com.app.quickcall.webrtc.WebRtcClient;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;

import java.util.Map;

public class MainRepository implements WebRtcClient.Listener {

    private FirebaseClient firebaseClient;
    private WebRtcClient webRtcClient;
    private String username;

    private static MainRepository instance;


    private String currentUsername;

    private SurfaceViewRenderer remoteView;

    public MainRepository() {
        firebaseClient = new FirebaseClient();
    }

    public static MainRepository getInstance() {
        if(instance == null) {
            instance = new MainRepository();
        }

        return instance;
    }

    public void login(Activity activity, String username, String password, SuccessCallback callback) {
        firebaseClient.login(activity, username, password, ()-> {
            this.webRtcClient = new WebRtcClient(activity.getApplicationContext(), new PeerConnectionObserver() {
                @Override
                public void onAddStream(MediaStream mediaStream) {
                    super.onAddStream(mediaStream);
                    try {
                        mediaStream.videoTracks.get(0).addSink(remoteView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                    super.onConnectionChange(newState);
                }

                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    super.onIceCandidate(iceCandidate);
                }

            }, username);

            webRtcClient.listener = this;
            callback.onSuccess();
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addUsers(Map<String, Object> user, SuccessCallback callback) {
        firebaseClient.addUser(user, ()-> {
            callback.onSuccess();
        });
    }

    public void signUpUser(Activity activity, String email, String password, SuccessCallback callback) {
        firebaseClient.signUpUser(activity, email, password, () -> {
            callback.onSuccess();
        });
    }

    public void initLocalView(SurfaceViewRenderer view){
        webRtcClient.initLocalSurfaceView(view);
    }

    public void initRemoteView(SurfaceViewRenderer view){
        webRtcClient.initRemoteSurfaceView(view);
        this.remoteView = view;
    }

    @Override
    public void onTransferDataToOtherPeer(CallModel model) {
        firebaseClient.sendMessage(model, () -> {

        });
    }

    public void startCall() {
        webRtcClient.call();
    }

    public void switchCamera() {
        webRtcClient.switchCamera();
    }
}
