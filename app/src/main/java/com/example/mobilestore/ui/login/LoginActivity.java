package com.example.mobilestore.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mobilestore.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.mobilestore.R;
import com.example.mobilestore.ui.admin_customer_bill.CustomerBillActivity;
import com.example.mobilestore.ui.admin_report.ReportActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private AppCompatButton btnLogin;
    private TextView tvSignUp;
    private ImageView ivTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Kiểm tra nếu đã login thì chuyển thẳng sang ReportActivity
        if (isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, ReportActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        // Handle "Sign Up" click
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, com.example.mobilestore.ui.register.RegisterActivity.class);
            startActivity(intent);
        });

        // Toggle password visibility
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                // Show password
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            // Move cursor to the end
            etPassword.setSelection(etPassword.length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Handle login button click
        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else if (email.equals("admin@gmail.com") && password.equals("123456")) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, CustomerBillActivity.class);
                startActivity(intent);
                finish(); // Close LoginActivity
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isLoggedIn() {
        // Kiểm tra trạng thái đăng nhập từ SharedPreferences/Session
        return false; // Implement logic kiểm tra login status
    }
}
