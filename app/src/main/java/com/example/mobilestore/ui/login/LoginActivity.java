package com.example.mobilestore.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.mobilestore.R;
import com.example.mobilestore.ui.admin_customer_bill.CustomerBillActivity;
import com.example.mobilestore.ui.admin_report.ReportActivity;
import com.example.mobilestore.ui.shopping.ShoppingActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private AppCompatButton btnLogin;
    private TextView tvSignUp;
    private ImageView ivTogglePassword;
    private boolean isPasswordVisible = false;

    // Shared Preferences để lưu thông tin đăng nhập
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Kiểm tra nếu đã login thì chuyển thẳng sang màn hình tương ứng
        if (isLoggedIn()) {
            redirectBasedOnUserType();
            return;
        }

        // Initialize views
        initializeViews();

        // Set up listeners
        setupListeners();
    }

    private void initializeViews() {
        try {
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            tvSignUp = findViewById(R.id.tvSignUp);
            ivTogglePassword = findViewById(R.id.ivTogglePassword);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        // Handle "Sign Up" click
        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, com.example.mobilestore.ui.register.RegisterActivity.class);
                startActivity(intent);
            });
        }

        // Toggle password visibility
        if (ivTogglePassword != null && etPassword != null) {
            ivTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    // Hide password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    try {
                        ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
                    } catch (Exception e) {
                        // Use a system resource if custom drawable is missing
                        ivTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
                    }
                } else {
                    // Show password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    try {
                        ivTogglePassword.setImageResource(R.drawable.ic_visibility);
                    } catch (Exception e) {
                        // Use a system resource if custom drawable is missing
                        ivTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
                    }
                }
                // Move cursor to the end
                etPassword.setSelection(etPassword.length());
                isPasswordVisible = !isPasswordVisible;
            });
        }

        // Handle login button click
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                if (etUsername != null && etPassword != null) {
                    String email = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                    } else if (email.equals("admin@gmail.com") && password.equals("123456")) {
                        // Admin login
//                        saveLoginStatus(true, "admin", email);
                        Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                        try {
                            Intent intent = new Intent(LoginActivity.this, CustomerBillActivity.class);
                            startActivity(intent);
                            finish(); // Close LoginActivity
                        } catch (Exception e) {
                            Toast.makeText(this, "Error navigating to admin screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else if (email.equals("customer@gmail.com") && password.equals("123456")) {
                        // Customer login
//                        saveLoginStatus(true, "customer", email);
                        Toast.makeText(this, "Customer login successful!", Toast.LENGTH_SHORT).show();
                        try {
                            Intent intent = new Intent(LoginActivity.this, ShoppingActivity.class);
                            startActivity(intent);
                            finish(); // Close LoginActivity
                        } catch (Exception e) {
                            Toast.makeText(this, "Error navigating to shopping screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean isLoggedIn() {
        // Kiểm tra trạng thái đăng nhập từ SharedPreferences
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void saveLoginStatus(boolean isLoggedIn, String userType, String username) {
        // Lưu trạng thái đăng nhập và loại người dùng vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    private void redirectBasedOnUserType() {
        // Chuyển hướng người dùng dựa trên loại tài khoản
        String userType = sharedPreferences.getString(KEY_USER_TYPE, "");
        Intent intent;

        try {
            if (userType != null && userType.equals("admin")) {
                intent = new Intent(LoginActivity.this, CustomerBillActivity.class);
            } else {
                // Mặc định là customer
                intent = new Intent(LoginActivity.this, ShoppingActivity.class);
            }

            startActivity(intent);
            finish();
        } catch (Exception e) {
            // Reset login state if there's a navigation problem
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Error redirecting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức này có thể được sử dụng để đăng xuất khi cần
    public static void logout(AppCompatActivity activity) {
        try {
            SharedPreferences preferences = activity.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        } catch (Exception e) {
            Toast.makeText(activity, "Error during logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}