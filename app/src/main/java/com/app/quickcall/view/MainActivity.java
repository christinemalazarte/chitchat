package com.app.quickcall.view;

import android.content.Intent;
import android.os.Bundle;

import com.app.quickcall.R;
import com.app.quickcall.remote.FirebaseClient;
import com.app.quickcall.repository.MainRepository;
import com.app.quickcall.utils.CallListener;
import com.app.quickcall.utils.DataModelType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.app.quickcall.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CallListener, MainRepository.Listener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseClient fbClient;

    String callerName;
    MainRepository mainRepository;

    // Access a Cloud Firestore instance from your Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainRepository = MainRepository.getInstance();
        mainRepository.listener = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//
//        setSupportActionBar(binding.toolbar);


//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                mainRepository.login("diane");
//
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAnchorView(R.id.fab)
//                        .setAction("Action", null).show();
//            }
//        });

        loadFragment(new FirstFragment(getApplicationContext(), this));

        // Handle tab selection
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new FirstFragment(getApplicationContext(), this);
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new SecondFragment();
            }
            return loadFragment(selectedFragment);
        });

        mainRepository.subscribeForLatestEvent(data->{
            if (data.getType() == DataModelType.StartCall){
                runOnUiThread(()->{
                    Log.d("MAINACTIVITY", "IS CALLING " + data.getSender());
                    callerName = data.getSender();
                    binding.incomingNameTV.setText(data.getSender()+" is Calling you");
                    binding.incomingCallLayout.setVisibility(View.VISIBLE);

                    Log.d("MAINACTIVITY", "IS CALLING " + binding.incomingCallLayout.getVisibility());

                    binding.acceptButton.setOnClickListener(v->{
                        //star the call here
//                        mainRepository.startCall(data.getSender());
                        binding.incomingCallLayout.setVisibility(View.GONE);

                        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                        intent.putExtra("contact_name", data.getSender());
                        intent.putExtra("is_caller", false); // Pass contact name to the new activity
                        startActivity(intent);
                    });
                    binding.rejectButton.setOnClickListener(v->{
                        binding.incomingCallLayout.setVisibility(View.GONE);
                    });
                });
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void startCall(String name) {
        Log.d("CALLLISTENER", "INTERFACE " + name);

//        mainRepository.sendCallRequest(name, ()->{
//            Toast.makeText(this, "couldnt find the target", Toast.LENGTH_SHORT).show();
//        });

//        binding.incomingCallLayout.setVisibility(View.GONE);

        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra("contact_name", name); // Pass contact name to the new activity
        intent.putExtra("is_caller", true);
        startActivity(intent);
    }

    @Override
    public void webrtcConnected() {
//        runOnUiThread(()->{
//
//            Log.d("webrtcConnected: MainActivity ", "TRUE");
//            binding.incomingCallLayout.setVisibility(View.GONE);
//            Intent intent = new Intent(getApplicationContext(), CallActivity.class);
//            intent.putExtra("contact_name", callerName); // Pass contact name to the new activity
//            startActivity(intent);
//        });
    }

    @Override
    public void webrtcClosed() {

    }
}