package com.app.quickcall.remote;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.quickcall.model.CallModel;
import com.app.quickcall.utils.ErrorCallback;
import com.app.quickcall.utils.NewEventCallback;
import com.app.quickcall.utils.SuccessCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirebaseClient {

    private final Gson gson = new Gson();
    private FirebaseFirestore db;
    String TAG = "FirebaseClient";
    private FirebaseAuth mAuth;
    private String currentUsername;
    private String email;
    private static final String LATEST_EVENT_FIELD_NAME = "latest_event";
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    public FirebaseClient() {
        db = FirebaseFirestore.getInstance();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
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

    public void login(Activity activity, String password, String username, SuccessCallback callback) {
        DatabaseReference eggRef = dbRef.child(username).child("email");
        eggRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    String email = task.getResult().getValue(String.class);
                    Log.d("TAGonCompleteonComplete", email);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
            //                            dbRef.child(username).setValue("").addOnCompleteListener(tasks -> {
            //                                currentUsername = username;
            //                                callback.onSuccess();
            //                            });
                                        dbRef.child(username).child("email")
                                                .setValue(email).addOnCompleteListener(tasks -> {
                                                    currentUsername = username;
                                                    callback.onSuccess();
                                                });
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(activity, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Log.d("TAGonCompleteonCompleteTAGonCompleteonComplete", task.getException().getMessage()); //Don't ignore potential errors!
                }
            }
        });

    }

    public void sendMessage(CallModel callModel, ErrorCallback errorCallback) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(callModel.getTarget()).exists()){
                    //send the signal to other user
                    dbRef.child(callModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
                            .setValue(gson.toJson(callModel));
                    Log.d("CALL-FEATURE: exists", gson.toJson(callModel).toString());

                } else {
                    Log.d("CALL-FEATURE: doesnot exists", callModel.getTarget());
                    errorCallback.onError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorCallback.onError();
            }
        });
    }

    public void observeIncomingLatestEvent(NewEventCallback callback) {
        dbRef.child(currentUsername).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try{
                            String data= Objects.requireNonNull(snapshot.getValue()).toString();
                            CallModel dataModel = gson.fromJson(data,CallModel.class);
                            callback.onNewEventReceived(dataModel);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    public void signUpUser(Activity activity, String email, String password, String username, SuccessCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);
                            currentUsername = username;

                            dbRef.child(username).child("email")
                                    .setValue(email).addOnCompleteListener(tasks -> {
                                        currentUsername = username;
                                        callback.onSuccess();
                                    });

//                            addUser(user, ()-> {
//                                Log.d("FIREBASE", "User added successfully on DB");
//                                callback.onSuccess();
//
//                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
