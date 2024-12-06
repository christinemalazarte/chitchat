package com.app.quickcall.remote;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.quickcall.model.CallModel;
import com.app.quickcall.utils.DataModelType;
import com.app.quickcall.utils.ErrorCallback;
import com.app.quickcall.utils.NewEventCallback;
import com.app.quickcall.utils.SuccessCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

public class FirebaseClient {

    String TAG = "FirebaseClient";
    private final Gson gson = new Gson();
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private String currentUsername;
    private static final String LATEST_EVENT_FIELD_NAME = "latest_event";
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    public FirebaseClient() {
        db = FirebaseFirestore.getInstance();
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
        DatabaseReference loginRef = dbRef.child(username).child("email");
        loginRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    String email = task.getResult().getValue(String.class);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        dbRef.child(username).child("email")
                                                .setValue(email).addOnCompleteListener(tasks -> {
                                                    currentUsername = username;
                                                });

                                        dbRef.child(username).child(LATEST_EVENT_FIELD_NAME)
                                                .setValue("").addOnCompleteListener(tasks -> {
                                                    currentUsername = username;
                                                    callback.onSuccess();
                                                });
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(activity, "Please input the correct username and password.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Log.d("LOGIN", task.getException().getMessage()); //Don't ignore potential errors!
                }
            }
        });

    }

    public void logout() {
        mAuth.signOut();

    }

    public void sendMessage(CallModel callModel, ErrorCallback errorCallback) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(callModel.getTarget()).exists()){
                    //send the signal to other user
                    dbRef.child(callModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
                            .setValue(gson.toJson(callModel));

                    if (callModel.getType() == DataModelType.CallRejected) {
                        resetLatestEvents(callModel);
                    }


                } else {
                    errorCallback.onError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorCallback.onError();
            }
        });
    }

    public void resetLatestEvents(CallModel callModel) {
        dbRef.child(callModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
                .setValue("");

        dbRef.child(callModel.getSender()).child(LATEST_EVENT_FIELD_NAME)
                .setValue("");

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
        DatabaseReference signUpRef = dbRef.child(username);
        signUpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    // The child doesn't exist

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        currentUsername = username;
                                        dbRef.child(username).child("email")
                                                .setValue(email).addOnCompleteListener(tasks -> {
                                                    currentUsername = username;
                                                    callback.onSuccess();
                                                });

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(activity, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // return an error, username is already exists
                    Log.d("SIGNUP", "USERNAME ALREADY EXISTS");
                    Toast.makeText(activity, "Username has already been taken.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
