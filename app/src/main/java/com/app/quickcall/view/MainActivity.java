package com.app.quickcall.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import com.app.quickcall.R;
import com.app.quickcall.remote.FirebaseClient;
import com.app.quickcall.repository.MainRepository;
import com.app.quickcall.utils.CallListener;
import com.app.quickcall.utils.DataModelType;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.Manifest;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.app.quickcall.databinding.ActivityMainBinding;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements CallListener {

    private ActivityMainBinding binding;
    private FirebaseClient fbClient;
    private final int CAMERA_PERMISSION_CODE = 1;
    private final int CAMERA_REQUEST_CODE = 2;
    private final int AUDIO_PERMISSION_CODE = 3;

    String callerName;
    MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String currentUsername = getIntent().getStringExtra("current_username");
        mainRepository = MainRepository.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        requestPermission();

        loadFragment(new ContactListFragment(getApplicationContext(), this, currentUsername));

        // Handle tab selection
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new ContactListFragment(getApplicationContext(), this, currentUsername);
            } else if (item.getItemId() == R.id.nav_history) {
                selectedFragment = new HistoryListFragment();
            }  else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            return loadFragment(selectedFragment);
        });

        mainRepository.subscribeForLatestEvent(data->{
            if (data.getType() == DataModelType.StartCall){
                runOnUiThread(()->{
                    callerName = data.getSender();
                    binding.incomingNameTV.setText(data.getSender()+" is Calling you");
                    binding.incomingCallLayout.setVisibility(View.VISIBLE);
                    binding.incomingCallLayout.setBackgroundColor(Color.WHITE);

                    binding.acceptButton.setOnClickListener(v->{
                        //start the call here
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

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
        }
    }

    @Override
    public void startCall(String name) {
        callerName = name;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getApplicationContext(), CallActivity.class);
            intent.putExtra("contact_name", callerName); // Pass contact name to the new activity
            intent.putExtra("is_caller", true);
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
                }

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE);
                Toast.makeText(this, "You need to allow the permissions on your settings.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}