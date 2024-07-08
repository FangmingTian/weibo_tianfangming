// LoginActivity.java
package com.example.weibo_tianfangming;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneEditText, codeEditText;
    private Button getCodeButton, loginButton;
    private TextView backButton, title;
    private CountDownTimer countDownTimer;
    private RequestQueue requestQueue;
    private int requestCount = 0;
    private static final int MAX_REQUESTS = 20;

    long id;
    String username;
    String phone;
    String avatar;
    boolean loginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneEditText = findViewById(R.id.phoneNumberInput);
        codeEditText = findViewById(R.id.verificationCodeInput);
        getCodeButton = findViewById(R.id.getVerificationCodeButton);
        loginButton = findViewById(R.id.loginButton);
        backButton = findViewById(R.id.backButton);
        title = findViewById(R.id.title);

        title.setText("登录账号");
        backButton.setText("返回");
        //backButton.setOnClickListener(view -> finish());
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });


        requestQueue = Volley.newRequestQueue(this);

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 11) {
                    getCodeButton.setEnabled(true);
                } else {
                    getCodeButton.setEnabled(false);
                }
            }
        });

        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                loginButton.setEnabled(s.length() == 6 && phoneEditText.getText().length() == 11);
            }
        });

        getCodeButton.setOnClickListener(view -> {
            if (requestCount < MAX_REQUESTS) {
                sendCodeRequest();
                startCountDownTimer();
                requestCount++;
            } else {
                Toast.makeText(LoginActivity.this, "今日验证码请求已达上限", Toast.LENGTH_SHORT).show();
            }
        });

        loginButton.setOnClickListener(view -> loginRequest());
    }

    private void sendCodeRequest() {
        String phone = phoneEditText.getText().toString();
        String url = "https://hotfix-service-prod.g.mi.com/weibo/api/auth/sendCode";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("phone", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        int code = response.getInt("code");
                        String msg = response.getString("msg");
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(LoginActivity.this, "发送验证码失败", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    private void loginRequest() {
        String phone = phoneEditText.getText().toString();
        String code = codeEditText.getText().toString();
        String url = "https://hotfix-service-prod.g.mi.com/weibo/api/auth/login";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("phone", phone);
            requestBody.put("smsCode", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Request URL: " + url);
        Log.d(TAG, "Request Body: " + requestBody.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        int codeRes = response.getInt("code");
                        String msg = response.getString("msg");
                        Log.d(TAG, "Response Code: " + codeRes);
                        Log.d(TAG, "Response Message: " + msg);
                        if (codeRes == 200) {
                            // 检查 data 是否为字符串类型
                            Object data = response.get("data");
                            if (data instanceof String) {
                                String token = (String) data;
                                Log.d(TAG, "Token: " + token);
                                // 存储 token 到 SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("token", token);
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                                fetchUserInfo(token);
                                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                                finish();
                            } else if (data instanceof JSONObject) {
                                JSONObject jsonData = (JSONObject) data;
                                String token = jsonData.getString("token");
                                String username = jsonData.getString("username");
                                Log.d(TAG, "1. username: " + username);


                                String avatarUrl = jsonData.getString("avatarUrl");
                                int fansCount = jsonData.getInt("fansCount");
                                Log.d(TAG, "Token: " + token);
                                Log.d(TAG, "Username: " + username);
                                Log.d(TAG, "Avatar URL: " + avatarUrl);
                                Log.d(TAG, "Fans Count: " + fansCount);
                                // 存储数据到 SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("token", token);
                                editor.putString("username", username);
                                editor.putString("avatarUrl", avatarUrl);
                                editor.putInt("fansCount", fansCount);
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                                finish();
                            } else {
                                Log.e(TAG, "Unexpected data type: " + data.getClass().getName());
                                Toast.makeText(LoginActivity.this, "登录失败，返回数据格式错误", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "JSON Exception: " + e.getMessage());
                    }
                }, error -> {
            error.printStackTrace();
            Log.e(TAG, "Volley Error: " + error.getMessage());
            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void startCountDownTimer() {
        getCodeButton.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getCodeButton.setText(String.valueOf(millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                getCodeButton.setText("获取验证码");
                getCodeButton.setEnabled(true);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void fetchUserInfo(String token) {
        String url = "https://hotfix-service-prod.g.mi.com/weibo/api/user/info";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        int code = response.getInt("code");
                        if (code == 200) {
                            JSONObject data = response.getJSONObject("data");
                            id = data.getLong("id");
                            username = data.getString("username");
                            phone = data.getString("phone");
                            avatar = data.getString("avatar");
                            loginStatus = data.getBoolean("loginStatus");

                            SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putLong("id", id);
                            editor.putString("username", username);
                            editor.putString("phone", phone);
                            editor.putString("avatar", avatar);
                            editor.putBoolean("loginStatus", loginStatus);
                            editor.apply();


                            if (loginStatus) {
                                Intent intent;
                                intent = new Intent(this, ProfileActivity.class);
//                                intent.putExtra("id", id);
//                                intent.putExtra("username", username);
//                                intent.putExtra("phone", phone);
//                                intent.putExtra("avatar", avatar);
//                                intent.putExtra("loginStatus", loginStatus);
                                startActivity(intent);
                            }

                        } else {
                            String msg = response.getString("msg");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}