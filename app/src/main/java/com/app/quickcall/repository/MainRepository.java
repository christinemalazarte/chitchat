package com.app.quickcall.repository;

import android.app.Activity;

import com.app.quickcall.remote.FirebaseClient;
import com.app.quickcall.utils.SuccessCallback;

import java.util.Map;

public class MainRepository {

    private FirebaseClient firebaseClient;
    private String username;

    private static MainRepository instance;

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

}
