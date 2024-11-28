package com.app.quickcall.repository;

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

    public void login(String username, SuccessCallback callback) {
        firebaseClient.login(username, ()-> {
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

}
