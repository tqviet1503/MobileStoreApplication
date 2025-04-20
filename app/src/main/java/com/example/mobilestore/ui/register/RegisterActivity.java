package com.example.mobilestore.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.mobilestore.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;
    private AppCompatButton btnRegister;
    private TextView tvLoginRedirect;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect);

        // Handle password visibility toggle
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Handle confirm password visibility toggle
        ivToggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
            }
            etConfirmPassword.setSelection(etConfirmPassword.length());
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        });

        // Handle registration logic
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Registration successful
                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                // You can redirect to login screen or save data to database here
                Intent intent = new Intent(RegisterActivity.this, com.example.mobilestore.ui.login.LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle login redirect
        tvLoginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, com.example.mobilestore.ui.login.LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
