package com.example.weibo_tianfangming;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageViewerActivity extends AppCompatActivity {
    private static final String TAG = "ImageViewerActivity";
    private ViewPager viewPager;
    private ImagePagerAdapter adapter;
    private TextView pageNumber;
    private TextView downloadText;
    private String[] imageUrls;
    private int currentIndex;

    private String username;
    private String avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // 获取传递的数据
        imageUrls = getIntent().getStringArrayExtra("imageUrls");
        currentIndex = getIntent().getIntExtra("index", 0);

        username = getIntent().getStringExtra("username");
        avatar = getIntent().getStringExtra("avatar");
        Log.d(TAG, "userName: " + username);
        Log.d(TAG, "avatar: " + avatar);

        viewPager = findViewById(R.id.viewPager);
        pageNumber = findViewById(R.id.pageNumber);
        downloadText = findViewById(R.id.downloadText);

        // 示例：显示 username 和 avatar
        TextView usernameTextView = findViewById(R.id.usernameTextView);
        ImageView avatarImageView = findViewById(R.id.avatarImageView);

        usernameTextView.setText(username);
        Glide.with(this).load(avatar).into(avatarImageView);

        adapter = new ImagePagerAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex);

        pageNumber.setText((currentIndex + 1) + "/" + imageUrls.length);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                pageNumber.setText((position + 1) + "/" + imageUrls.length);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        downloadText.setOnClickListener(v -> {
            Log.d(TAG, "Download button clicked");
            downloadImage(imageUrls[viewPager.getCurrentItem()]);
        });
    }

    private void downloadImage(String url) {
        new Thread(() -> {
            try {
                // Download the image
                URL imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                    return;
                }

                InputStream inputStream = connection.getInputStream();

                // Save the image to MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Weibo");

                Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver resolver = getContentResolver();
                Uri insertUri = resolver.insert(externalUri, values);

                if (insertUri == null) {
                    Log.e(TAG, "Failed to create new MediaStore record.");
                    return;
                }

                try (OutputStream outputStream = resolver.openOutputStream(insertUri)) {
                    if (outputStream != null) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        runOnUiThread(() -> Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show());
                } finally {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "下载图片失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}