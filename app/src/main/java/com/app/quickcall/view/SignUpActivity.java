package com.app.quickcall.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
                String username = binding.editTextUsername.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                String email = binding.editTextEmail.getText().toString();

                if (username.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please input username",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please input email",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please input password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mainRepository.signUpUser(SignUpActivity.this, email, password, username,() -> {
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.putExtra("current_username", username); // Pass contact name to the new activity
                    startActivity(intent);
                });
            }
        });
    }
}