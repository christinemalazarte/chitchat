package com.app.quickcall.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.quickcall.R;
import com.app.quickcall.databinding.ActivitySignUpBinding;
import com.app.quickcall.repository.MainRepository;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    private MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mainRepository = MainRepository.getInstance();

        String username = "chen2";
        String password = "password123"; //password123

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainRepository.signUpUser(SignUpActivity.this, "fdc.christinediane2@gmail.com", password, username,() -> {
                    Intent intent = new Intent(SignUpActivity.this, CallActivity.class);
                    startActivity(intent);
                });
            }
        });
    }
}