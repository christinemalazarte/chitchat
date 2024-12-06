package com.app.quickcall.webrtc;

import android.content.Context;
import android.util.Log;

import com.app.quickcall.model.CallModel;
import com.app.quickcall.utils.DataModelType;
import com.google.gson.Gson;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRtcClient {

    private final Gson gson = new Gson();
    private Context context;
    private String username;
    private EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();
    private PeerConnectionFactory peerConnectionFactory;
    public PeerConnection peerConnection;
    private List<PeerConnection.IceServer> iceServerList = new ArrayList<>();
    private CameraVideoCapturer videoCapturer;
    private VideoSource localVideoSource;
    private AudioSource localAudioSource;
    private String localTrackId = "local_track";
    private String localStreamId = "local_stream";
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private MediaStream localStream;
    private MediaConstraints mediaConstraints = new MediaConstraints();

    public Listener listener;

    public WebRtcClient(Context context, PeerConnection.Observer observer, String username) {
        this.context = context;
        this.username = username;
        initPeerConnectionFactory();
        this.peerConnectionFactory = createPeerConnectionFactory();
        iceServerList.add(PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302")
           .createIceServer());
        peerConnection = createPeerConnection(observer);
        localVideoSource = peerConnectionFactory.createVideoSource(false);
        localAudioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo","true"));
    }

    // Initializing Peer Connection
    private void initPeerConnectionFactory() {
        // Initialize WebRTC
        PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory.InitializationOptions.builder(context).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions();

        // Create a PeerConnectionFactory
        PeerConnectionFactory.initialize(options);
    }

    private PeerConnectionFactory createPeerConnectionFactory() {
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = false;
        options.disableNetworkMonitor = false;

        return PeerConnectionFactory.builder().setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBaseContext, true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBaseContext))
                .setOptions(options).createPeerConnectionFactory();
    }

    private PeerConnection createPeerConnection(PeerConnection.Observer observer) {
        return peerConnectionFactory.createPeerConnection(iceServerList, observer);
    }

    public interface Listener {
        void onTransferDataToOtherPeer(CallModel model);
    }

    //Initializing UI like surface view renderers
    public void initSurfaceViewRender(SurfaceViewRenderer viewRenderer){
        viewRenderer.setEnableHardwareScaler(true);
        viewRenderer.setMirror(true);
        viewRenderer.init(eglBaseContext,null);
    }

    public void initLocalSurfaceView(SurfaceViewRenderer view){
        initSurfaceViewRender(view);
        startLocalVideoStreaming(view);
    }

    public void initRemoteSurfaceView(SurfaceViewRenderer view){
        initSurfaceViewRender(view);
    }

    private void startLocalVideoStreaming(SurfaceViewRenderer view) {
        SurfaceTextureHelper helper= SurfaceTextureHelper.create(
                Thread.currentThread().getName(), eglBaseContext
        );

        localTrackId = "local_track";
        localStreamId = "local_stream";

        videoCapturer = getVideoCapturer();
        videoCapturer.initialize(helper,context,localVideoSource.getCapturerObserver());
        videoCapturer.startCapture(480,360,15);
        localVideoTrack = peerConnectionFactory.createVideoTrack(
                localTrackId+"_video",localVideoSource
        );
        localVideoTrack.addSink(view);

        localAudioTrack = peerConnectionFactory.createAudioTrack(localTrackId+"_audio",localAudioSource);
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId);
        localStream.addTrack(localVideoTrack);
        localStream.addTrack(localAudioTrack);
        peerConnection.addStream(localStream);
    }

    private CameraVideoCapturer getVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);

        String[] deviceNames = enumerator.getDeviceNames();

        for (String device: deviceNames){
            if (enumerator.isFrontFacing(device)){
                return enumerator.createCapturer(device,null);
            }
        }
        throw new IllegalStateException("front facing camera not found");
    }

    //The one who accepts the call, starts the call flow and creates an OFFER and pass it to the caller (target)
    public void call(String target) {
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                peerConnection.setLocalDescription(new SdpObserver(){
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        //Its time to transfer this sdp to other peer (caller/target)
                        if (listener!=null){
                            Log.d("caller", target);
                            listener.onTransferDataToOtherPeer(new CallModel(
                                    target,username,sessionDescription.description, DataModelType.Offer
                            ));
                        }
                    }
                },sessionDescription);
            }
        }, mediaConstraints);

    }

    public void answer(String target) {
        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                peerConnection.setLocalDescription(new SdpObserver(){
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        //its time to transfer this sdp to other peer
                        if (listener!=null){
                            listener.onTransferDataToOtherPeer(new CallModel(
                                    target,username,sessionDescription.description, DataModelType.Answer
                            ));
                        }
                    }
                },sessionDescription);
            }

        }, mediaConstraints );
    }

    public void closeConnection(){
        try{
            localVideoTrack.dispose();
            videoCapturer.stopCapture();
            videoCapturer.dispose();
            peerConnection.close();

            closeMedia();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void closeMedia() {
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }

        if (localAudioTrack != null) {
            localAudioTrack.dispose();
            localAudioTrack = null;
        }

        if (localVideoTrack != null) {
            localVideoTrack.dispose();
            localVideoTrack = null;
        }

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (Exception e) {
                e.printStackTrace();
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }

        if (localStream != null) {
            localStream.dispose();
            localStream = null;
        }
    }

    public void onRemoteSessionReceived(SessionDescription desc) {
        peerConnection.setRemoteDescription(new SdpObserver(), desc);
    }

    public void addIceCandidate(IceCandidate iceCandidate){
        peerConnection.addIceCandidate(iceCandidate);
    }

    public void sendIceCandidate(IceCandidate iceCandidate, String target){
        addIceCandidate(iceCandidate);
        if (listener!=null){
            listener.onTransferDataToOtherPeer(new CallModel(
                    target,username,gson.toJson(iceCandidate),DataModelType.IceCandidate
            ));
        }
    }

    public void switchCamera() {
        videoCapturer.switchCamera(null);
    }

    public void toggleVideo(Boolean shouldBeMuted){
        localVideoTrack.setEnabled(shouldBeMuted);
    }

    public void toggleAudio(Boolean shouldBeMuted){
        localAudioTrack.setEnabled(shouldBeMuted);
    }

}
