package com.app.quickcall;

import android.os.Bundle;

import com.app.quickcall.remote.FirebaseClient;
import com.app.quickcall.repository.MainRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.app.quickcall.ui.main.SectionsPagerAdapter;
import com.app.quickcall.databinding.ActivityMain2Binding;


public class MainActivity extends AppCompatActivity {

    private ActivityMain2Binding binding;
    private FirebaseClient fbClient;
    String TAG = "MainActivity";

    MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainRepository = MainRepository.getInstance();

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
                //addUser();

//                mainRepository.signUpUser(MainActivity.this, "fdc.chen@gmail.com", "password123", () -> {
//
//                });

                mainRepository.login(MainActivity.this, "fdc.christinediane@gmail.com", "password123", "christine", () -> {

                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}