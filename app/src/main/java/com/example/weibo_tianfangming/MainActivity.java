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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private ImageView  loadingTextView, errorImageView;
    private TextView emptyContentTextView;
    private Button errorTextButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WeiboPostAdapter adapter;
    private List<WeiboPost> weiboPosts = new ArrayList<>();
    private RequestQueue requestQueue;
    private boolean isLoading = false;
    private int currentPage = 1;
    private LinearLayout navHome, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(view -> {
            // 切换到首页内容，可以在这里添加对应的逻辑
        });

        navProfile.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        emptyContentTextView = findViewById(R.id.emptyContentText);
        loadingTextView = findViewById(R.id.loadingText);
        errorImageView = findViewById(R.id.errorImage);
        errorTextButton = findViewById(R.id.errorTextButton);
        //noNetworkTextView = findViewById(R.id.noNetworkText);

        errorTextButton.setOnClickListener(view -> {
            fetchPosts();
        });

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        adapter = new WeiboPostAdapter(weiboPosts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        swipeRefreshLayout.setOnRefreshListener(this::fetchPosts);

        // 显示加载中页面
        //showLoading();

        // 读取缓存数据
        loadCachedPosts();

        //fetchPosts();
        // 检查登录状态
        checkLoginStatus();

        // Infinite scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && dy > 0 && !isLoading) {
                    fetchPosts();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = preferences.getString("token", null);
        if (token != null) {
            fetchPosts();
        }
//        if (token == null) {
//            // 如果没有找到 token，跳转到登录页面
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        } else {
//            // 加载微博帖子
//            fetchPosts();
//        }
    }



    private void fetchPosts() {
        if (isLoading) return;
        isLoading = true;

        String url = "https://hotfix-service-prod.g.mi.com/weibo/homePage?current=" + currentPage + "&size=10";
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = preferences.getString("token", null);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        int code = response.getInt("code");
                        if (code == 200) {
                            emptyContentTextView.setVisibility(View.GONE);
                            JSONObject data = response.getJSONObject("data");
                            JSONArray records = data.getJSONArray("records");
                            if (swipeRefreshLayout.isRefreshing()) {
                                weiboPosts.clear();
                                currentPage = 1;
                            }
                            if (records.length() == 0) {
                                Toast.makeText(MainActivity.this, "无更多内容", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int i = 0; i < records.length(); i++) {
                                JSONObject record = records.getJSONObject(i);
                                WeiboPost post = new WeiboPost();
                                post.setId(record.getLong("id"));
                                post.setUserId(record.getLong("userId"));
                                post.setUsername(record.getString("username"));
                                post.setAvatar(record.getString("avatar"));
                                post.setTitle(record.getString("title"));
                                post.setVideoUrl(record.optString("videoUrl", null));
                                post.setPoster(record.getString("poster"));
                                JSONArray imagesArray = record.optJSONArray("images");
                                if (imagesArray != null) {
                                    List<String> images = new ArrayList<>();
                                    for (int j = 0; j < imagesArray.length(); j++) {
                                        images.add(imagesArray.getString(j));
                                    }
                                    post.setImages(images);
                                }
                                post.setLikeCount(record.getInt("likeCount"));
                                post.setLikeFlag(record.getBoolean("likeFlag"));
                                String createTimeStr = record.getString("createTime");
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime dateTime = LocalDateTime.parse(createTimeStr, formatter);
                                long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                post.setCreateTime(timestamp);

                                weiboPosts.add(post);
                            }
                            adapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            loadingTextView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            errorTextButton.setVisibility(View.GONE);
                            errorImageView.setVisibility(View.GONE);
                            currentPage++;

                            // 缓存数据
                            cachePosts(data.toString());
                            //hideNoNetwork();
                        } else if (code == 403) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove("token");
                            editor.apply();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            //emptyContentTextView.setVisibility(View.VISIBLE);
                            String msg = response.getString("msg");
                            //Toast.makeText(MainActivity.this, "Failed to fetch data: " + msg, Toast.LENGTH_SHORT).show();
                            loadingTextView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            errorTextButton.setVisibility(View.VISIBLE);
                            errorImageView.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        loadingTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        errorTextButton.setVisibility(View.VISIBLE);
                        errorImageView.setVisibility(View.VISIBLE);
                    }
                    isLoading = false;
                },
                error -> {
                    Log.e(TAG, "Volley error", error);
                    //Toast.makeText(MainActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    //showError();
                    isLoading = false;

                    // 使用缓存数据
                    //SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    String cachedData = preferences.getString("cached_posts", null);
                    if (cachedData != null) {
                        try {
                            JSONObject data = new JSONObject(cachedData);
                            JSONArray records = data.getJSONArray("records");
                            weiboPosts.clear();
                            for (int i = 0; i < records.length(); i++) {
                                JSONObject record = records.getJSONObject(i);
                                WeiboPost post = new WeiboPost();
                                post.setId(record.getLong("id"));
                                post.setUserId(record.getLong("userId"));
                                post.setUsername(record.getString("username"));
                                post.setAvatar(record.getString("avatar"));
                                post.setTitle(record.getString("title"));
                                post.setPoster(record.getString("poster"));
                                post.setVideoUrl(record.optString("videoUrl", null));
                                JSONArray imagesArray = record.optJSONArray("images");
                                if (imagesArray != null) {
                                    List<String> images = new ArrayList<>();
                                    for (int j = 0; j < imagesArray.length(); j++) {
                                        images.add(imagesArray.getString(j));
                                    }
                                    post.setImages(images);
                                }
                                post.setLikeCount(record.getInt("likeCount"));
                                post.setLikeFlag(record.getBoolean("likeFlag"));
                                String createTimeStr = record.getString("createTime");
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime dateTime = LocalDateTime.parse(createTimeStr, formatter);
                                long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                post.setCreateTime(timestamp);

                                weiboPosts.add(post);
                            }
                            adapter.notifyDataSetChanged();

                            loadingTextView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            errorTextButton.setVisibility(View.GONE);
                            errorImageView.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error", e);
                        }
                    } else {
                        errorImageView.setVisibility(View.VISIBLE);
                        loadingTextView.setVisibility(View.GONE);
                        errorTextButton.setVisibility(View.VISIBLE);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void cachePosts(String data) {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cached_posts", data);
        editor.apply();

        Log.d("!!!WeiboPost", "保存成功！！Cached posts: " + data);
    }

    private void loadCachedPosts() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String cachedData = preferences.getString("cached_posts", null);
        Log.d("!!!WeiboPost", "缓存为空判断 Cached data: " + cachedData);
        if (cachedData != null) {
            try {
                JSONObject data = new JSONObject(cachedData);
                JSONArray records = data.getJSONArray("records");
                Log.d("!!!WeiboPost", "Cached records: " + records.toString());
                weiboPosts.clear();
                for (int i = 0; i < records.length(); i++) {
                    JSONObject record = records.getJSONObject(i);
                    WeiboPost post = new WeiboPost();
                    post.setId(record.getLong("id"));
                    post.setUserId(record.getLong("userId"));
                    post.setUsername(record.getString("username"));
                    post.setAvatar(record.getString("avatar"));
                    post.setTitle(record.getString("title"));
                    post.setPoster(record.getString("poster"));
                    post.setVideoUrl(record.optString("videoUrl", null));
                    JSONArray imagesArray = record.optJSONArray("images");
                    if (imagesArray != null) {
                        List<String> images = new ArrayList<>();
                        for (int j = 0; j < imagesArray.length(); j++) {
                            images.add(imagesArray.getString(j));
                        }
                        post.setImages(images);
                    }
                    post.setLikeCount(record.getInt("likeCount"));
                    post.setLikeFlag(record.getBoolean("likeFlag"));
                    String createTimeStr = record.getString("createTime");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(createTimeStr, formatter);
                    long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    post.setCreateTime(timestamp);

                    weiboPosts.add(post);
                }
                adapter.notifyDataSetChanged();

                loadingTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                errorTextButton.setVisibility(View.GONE);
                errorImageView.setVisibility(View.GONE);
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error", e);
            }
        } else {
            loadingTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            errorTextButton.setVisibility(View.GONE);
            errorImageView.setVisibility(View.GONE);
        }
    }
}


