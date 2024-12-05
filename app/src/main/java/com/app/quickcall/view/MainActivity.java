package com.app.quickcall.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.app.quickcall.R;
import com.app.quickcall.remote.FirebaseClient;
import com.app.quickcall.repository.MainRepository;
import com.app.quickcall.utils.CallListener;
import com.app.quickcall.utils.DataModelType;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.fragment.app.Fragment;
import com.app.quickcall.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements CallListener {

    private ActivityMainBinding binding;
    private FirebaseClient fbClient;

    String callerName;
    MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String currentUsername = getIntent().getStringExtra("current_username");
        mainRepository = MainRepository.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadFragment(new ContactListFragment(getApplicationContext(), this, currentUsername));

        // Handle tab selection
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new ContactListFragment(getApplicationContext(), this, currentUsername);
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new HistoryListFragment();
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
        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra("contact_name", name); // Pass contact name to the new activity
        intent.putExtra("is_caller", true);
        startActivity(intent);
    }
}