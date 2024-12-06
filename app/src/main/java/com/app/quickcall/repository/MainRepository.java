package com.app.quickcall.repository;

import static com.app.quickcall.utils.DataModelType.CallRejected;
import static com.app.quickcall.utils.DataModelType.StartCall;

import android.app.Activity;
import android.util.Log;

import com.app.quickcall.model.CallModel;
import com.app.quickcall.remote.FirebaseClient;
import com.app.quickcall.utils.ErrorCallback;
import com.app.quickcall.utils.NewEventCallback;
import com.app.quickcall.utils.SuccessCallback;
import com.app.quickcall.webrtc.PeerConnectionObserver;
import com.app.quickcall.webrtc.WebRtcClient;
import com.google.gson.Gson;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

public class MainRepository implements WebRtcClient.Listener {

    private final Gson gson = new Gson();
    private static MainRepository instance;
    public Listener listener;
    public WebRtcClient webRtcClient;
    private FirebaseClient firebaseClient;
    private String username;
    private String currentUsername;
    private String target;
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

    public void login(Activity activity, String password, String username, SuccessCallback callback) {
        firebaseClient.login(activity, password, username, ()-> {
            currentUsername = username;
            initWebRtc(activity);
            callback.onSuccess();
        });
    }

    public void logout() {
        firebaseClient.logout();
    }

    public void initWebRtc(Activity activity) {

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

                Log.d("CALL-FEATURE: onConnectionChange: " , " " + newState);
                if (newState == PeerConnection.PeerConnectionState.CONNECTED && listener != null) {
                    listener.webrtcConnected();
                }

                if (newState == PeerConnection.PeerConnectionState.CLOSED || newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                    if (listener != null) {
                        listener.webrtcClosed();
                    }
                }
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d("CALL-FEATURE: onIceCandidate: " , target + " " + iceCandidate );
                webRtcClient.sendIceCandidate(iceCandidate,target);
            }

        }, currentUsername);

        webRtcClient.listener = this;
    }


    public void signUpUser(Activity activity, String email, String password, String username, SuccessCallback callback) {
        firebaseClient.signUpUser(activity, email, password, username, () -> {
            currentUsername = username;
            initWebRtc(activity);
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

    public void startCall(String target) {
        Log.d("startCall: caller: ", target);
        webRtcClient.call(target);
    }

    public void switchCamera() {
        webRtcClient.switchCamera();
    }

    public void toggleAudio(Boolean shouldBeMuted){
        webRtcClient.toggleAudio(shouldBeMuted);
    }

    public void toggleVideo(Boolean shouldBeMuted){
        webRtcClient.toggleVideo(shouldBeMuted);
    }

    public void sendCallRequest(String target, ErrorCallback
            errorCallBack){
        firebaseClient.sendMessage(
                new CallModel(target, currentUsername,null, StartCall),errorCallBack
        );
    }

    public void endCall(){
        webRtcClient.closeConnection();
    }

    public void subscribeForLatestEvent(NewEventCallback callBack){
        firebaseClient.observeIncomingLatestEvent(model -> {
            switch (model.getType()){

                case Offer:
                    this.target = model.getSender();
                    webRtcClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.OFFER,model.getData()
                    ));
                    webRtcClient.answer(model.getSender());
                    break;
                case Answer:
                    this.target = model.getSender();
                    webRtcClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.ANSWER,model.getData()
                    ));
                    break;
                case IceCandidate:
                    try{
                        IceCandidate candidate = gson.fromJson(model.getData(),IceCandidate.class);
                        webRtcClient.addIceCandidate(candidate);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case StartCall:
                    this.target = model.getSender();
                    callBack.onNewEventReceived(model);
                    break;
                case CallRejected:
                    callBack.onNewEventReceived(model);
                    break;
            }
        });
    }

    public void rejectCall(String target) {
        if (target != null && !target.isEmpty()) {
            Log.d("CALL-FEATURE", "Rejecting call from: " + target);

            // Notify the caller that the call is rejected
            firebaseClient.sendMessage(
                    new CallModel(target, currentUsername, null, CallRejected),
                    () -> {
                        Log.d("CALL-FEATURE", "Call rejection sent successfully to: " + target);
                    }
            );
        } else {
            Log.e("CALL-FEATURE", "Cannot reject call: Target is null or empty.");
        }
    }

    public interface Listener {
        void webrtcConnected();
        void webrtcClosed();
    }
}
