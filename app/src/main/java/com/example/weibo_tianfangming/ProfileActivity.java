package com.example.weibo_tianfangming;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide; // 需要添加 Glide 库来加载图片

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ImageView userAvatar;
    private TextView userName, userFansCount, loginHint, tip, logoutButton;
    private LinearLayout navHome, navProfile;

    private long id;
    private String username;
    private String phone;
    private String avatar;
    private boolean loginStatus;
    private int fansCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userFansCount = findViewById(R.id.userFansCount);
        logoutButton = findViewById(R.id.logoutButton);
        loginHint = findViewById(R.id.loginHint);
        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);
        tip = findViewById(R.id.tip);

//        id = getIntent().getLongExtra("id", 0);
//        username = getIntent().getStringExtra("username");
//        phone = getIntent().getStringExtra("phone");
//        avatar = getIntent().getStringExtra("avatar");
//        loginStatus = getIntent().getBooleanExtra("loginStatus", false);
//        fansCount = 1;

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        String username = preferences.getString("username", null);
        Log.d("ProfileActivity", "New!!! ProfileActivity username: " + username);
        String avatar = preferences.getString("avatar", null);
        int fansCount = preferences.getInt("fansCount", 0);
        fansCount = 1;
        Log.d("ProfileActivity", "NfansCount: " + fansCount);
        Log.d("ProfileActivity", "isLoggedIn: " + isLoggedIn);

        if (isLoggedIn) {
            userName.setText(username);
            loginHint.setText("你没有新的动态哦～");
            tip.setVisibility(View.GONE);


            if (!Objects.equals(avatar, "null")) {
                // 使用 Glide 加载头像图片
                Glide.with(this).load(avatar).into(userAvatar);
            }
            if (fansCount > 0) {
                userFansCount.setText("粉丝: " + fansCount);
                userFansCount.setVisibility(View.VISIBLE);
            }
            logoutButton.setVisibility(View.VISIBLE);

        }

        userAvatar.setOnClickListener(view -> {
            if (!isLoggedIn) {
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            }
        });

        userName.setOnClickListener(view -> {
            if (!isLoggedIn) {
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            }
        });

        logoutButton.setOnClickListener(view -> {
//            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.remove("token");
            editor.apply();
//            editor.clear();
//            editor.apply();
            userName.setText("请先登录");
            userFansCount.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
            loginHint.setText("登录后查看");
            tip.setVisibility(View.VISIBLE);
            userFansCount.setVisibility(View.GONE);
            userAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
            //isLoggedIn
            Toast.makeText(ProfileActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
        });

        navHome.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });

        navProfile.setOnClickListener(view -> {
            // 当前已经在个人中心，无需跳转
        });
    }
}