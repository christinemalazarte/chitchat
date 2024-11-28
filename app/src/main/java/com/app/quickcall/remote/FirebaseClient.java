package com.app.quickcall.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.app.quickcall.utils.SuccessCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.Map;

public class FirebaseClient {

    private final Gson gson = new Gson();
    private FirebaseFirestore db;
    String TAG = "FirebaseClient";

    public FirebaseClient() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(Map<String, Object> user, SuccessCallback callback) {
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void login(String username, SuccessCallback isSuccess) {

    }

    public void sendMessage() {

    }

    public void observeIncomingCall() {

    }
}
