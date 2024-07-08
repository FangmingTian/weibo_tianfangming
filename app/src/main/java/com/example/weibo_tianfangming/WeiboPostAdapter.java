package com.example.weibo_tianfangming;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WeiboPostAdapter extends RecyclerView.Adapter<WeiboPostAdapter.ViewHolder> {
    private List<WeiboPost> weiboPosts;
    private Context context;
    private RequestQueue requestQueue;


    public WeiboPostAdapter(List<WeiboPost> weiboPosts, Context context) {
        this.weiboPosts = weiboPosts;
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weibo_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeiboPost post = weiboPosts.get(position);

        holder.username.setText(post.getUsername());
        holder.title.setText(post.getTitle());
        Glide.with(context).load(post.getAvatar()).into(holder.avatar);

        if (!Objects.equals(post.getVideoUrl(), "null")) {
            // Video post
            holder.video.setVisibility(View.GONE);
            holder.image.setVisibility(View.GONE);
            holder.imageGridView.setVisibility(View.GONE);
            holder.poster.setVisibility(View.VISIBLE);
            holder.currentTime.setVisibility(View.VISIBLE);
            holder.totalTime.setVisibility(View.VISIBLE);

            // Set video poster
            Glide.with(context).load(post.getPoster()).into(holder.poster);

            // Set click listener to play/pause video
            holder.poster.setOnClickListener(v -> {
                holder.poster.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.VISIBLE);
                holder.video.setVisibility(View.VISIBLE);
                holder.video.setVideoURI(Uri.parse(post.getVideoUrl()));
                holder.video.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                    holder.totalTime.setText(formatTime(mp.getDuration()));
                    holder.seekBar.setMax(mp.getDuration());
                    mp.start();
                    holder.startUpdatingSeekBar();
                });
            });

            holder.video.setOnClickListener(v -> {
                if (holder.video.isPlaying()) {
                    holder.video.pause();
                    //holder.poster.setVisibility(View.VISIBLE);
                    holder.stopUpdatingSeekBar();
                } else {
                    holder.video.start();
                    //holder.poster.setVisibility(View.GONE);
                    holder.startUpdatingSeekBar();
                }
            });
            // Set video to pause when it is scrolled out of view
            holder.video.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (holder.video.isPlaying()) {
                    holder.video.pause();
                    holder.poster.setVisibility(View.VISIBLE);
                    holder.stopUpdatingSeekBar();
                }
            });

        } else {
            // Image post
            holder.video.setVisibility(View.GONE);
            holder.poster.setVisibility(View.GONE);
            holder.image.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.GONE);
            holder.currentTime.setVisibility(View.GONE);
            holder.totalTime.setVisibility(View.GONE);

            if (post.getImageCount() == 1) {
                // Display single image
                Glide.with(context).load(post.getImages().get(0)).into(holder.image);
                holder.image.setVisibility(View.VISIBLE);
                holder.imageGridView.setVisibility(View.GONE);
            } else {
                // Display multiple images using RecyclerView
                holder.image.setVisibility(View.GONE);
                holder.imageGridView.setVisibility(View.VISIBLE);
                ImageGridAdapter adapter = new ImageGridAdapter(post.getImages(), context, post.getUsername(), post.getAvatar());
                holder.imageGridView.setAdapter(adapter);

            }
        }

        // Implement click listeners for buttons (delete, comment, like)
        holder.deleteButton.setOnClickListener(v -> {
            // Handle delete post
            weiboPosts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, weiboPosts.size());
        });

        holder.commentButton.setOnClickListener(v -> {
            // Handle comment
            Toast.makeText(context, "点击第" + (position + 1) + "条数据评论按钮", Toast.LENGTH_SHORT).show();
        });


        holder.likeCount.setText(String.valueOf(post.getLikeCount()));
        holder.likeCount.setVisibility(post.isLikeFlag() ? View.VISIBLE : View.GONE);
        holder.likeButton.setImageResource(post.isLikeFlag() ? R.drawable.ic_like_red : R.drawable.ic_like);
        holder.likeButton.setOnClickListener(v -> {
            SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String token = preferences.getString("token", null);
            if (token == null) {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                return;
            }

            if (post.isLikeFlag()) {
                unlikePost(post, holder);
            } else {
                likePost(post, holder);
            }
        });


        holder.image.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageViewerActivity.class);
            intent.putExtra("imageUrls", post.getImages().toArray(new String[0]));
            intent.putExtra("index", 0);  // 单图默认第0张
            intent.putExtra("username", post.getUsername());
            intent.putExtra("avatar", post.getAvatar());
            context.startActivity(intent);
        });
    }

    private void handleLikeButton(ViewHolder holder, WeiboPost post, int position) {
        SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);
        if (token == null) {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            return;
        }

        // 点赞或取消点赞动画
        if (post.isLikeFlag()) {
            holder.likeButton.animate().scaleX(0.8f).scaleY(0.8f).setDuration(500)
                    .withEndAction(() -> holder.likeButton.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500).start()).start();
        } else {
            holder.likeButton.animate().scaleX(1.2f).scaleY(1.2f).rotationY(360).setDuration(500)
                    .withEndAction(() -> holder.likeButton.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500).start()).start();
        }

        // 模拟网络请求，成功后更新状态
        new Handler().postDelayed(() -> {
            post.setLikeFlag(!post.isLikeFlag());
            holder.likeButton.setSelected(post.isLikeFlag());
            notifyItemChanged(position);
        }, 1000);
    }

    private void likePost(WeiboPost post, ViewHolder holder) {

        SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);
        if (token == null) {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            return; // Stop further execution
        }
        String url = "https://hotfix-service-prod.g.mi.com/weibo/like/up";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", post.getId());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                try {
                    int code = response.getInt("code");
                    if (code == 200) {
                        post.setLikeFlag(true);
                        post.setLikeCount(post.getLikeCount() + 1);
                        holder.likeButton.setImageResource(R.drawable.ic_like_red);
                        holder.likeCount.setText(String.valueOf(post.getLikeCount()));
                        holder.likeCount.setVisibility(View.VISIBLE);
                        animateLike(holder.likeButton);
                    } else if (code == 403){
                        //Toast.makeText(context, response.getString("msg"), Toast.LENGTH_SHORT).show();
                        // Clear token
                        //SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove("token");
                        editor.putBoolean("isLoggedIn", false); // Update login status
                        editor.apply();

                        // Show toast or alert to inform user
                        Toast.makeText(context, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();

                        // Redirect to login page
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                        context.startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Response parsing error", Toast.LENGTH_SHORT).show();
                }
            },
                    error -> Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String token = preferences.getString("token", null);
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void unlikePost(WeiboPost post, ViewHolder holder) {

        SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);
        if (token == null) {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            return; // Stop further execution
        }

        String url = "https://hotfix-service-prod.g.mi.com/weibo/like/down";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", post.getId());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            int code = response.getInt("code");
                            if (code == 200) {
                                post.setLikeFlag(false);
                                post.setLikeCount(post.getLikeCount() - 1);
                                holder.likeButton.setImageResource(R.drawable.ic_like);
                                holder.likeCount.setVisibility(View.GONE);
//                                holder.likeCount.setText(String.valueOf(post.getLikeCount()));
//                                holder.likeCount.setVisibility(post.getLikeCount() > 0 ? View.VISIBLE : View.GONE);
                                animateLike(holder.likeButton);
                            } else if (code == 403){
                                // Clear token
                                //SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.remove("token");
                                editor.putBoolean("isLoggedIn", false); // Update login status
                                editor.apply();

                                // Show toast or alert to inform user
                                Toast.makeText(context, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();

                                // Redirect to login page
                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                                context.startActivity(intent);
                                //Toast.makeText(context, response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Response parsing error", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String token = preferences.getString("token", null);
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void animateLike(ImageView likeButton) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(likeButton, "scaleX", 1f, 1.2f, 1f);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();

        animator = ObjectAnimator.ofFloat(likeButton, "scaleY", 1f, 1.2f, 1f);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

            private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return weiboPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView username;
        public TextView title;
        public ImageView image;
        public RecyclerView imageGridView;
        public VideoView video;
        public ImageView poster;
        public SeekBar seekBar;
        public TextView currentTime;
        public TextView totalTime;
        public Button deleteButton;
        public Button commentButton;
        public ImageView likeButton;
        public TextView likeCount;
        public Handler handler = new Handler(Looper.getMainLooper());
        public Runnable updateSeekBarRunnable;

        public ViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
            imageGridView = itemView.findViewById(R.id.imageGridView);
            video = itemView.findViewById(R.id.video);
            poster = itemView.findViewById(R.id.poster);
            seekBar = itemView.findViewById(R.id.seekBar);
            currentTime = itemView.findViewById(R.id.currentTime);
            totalTime = itemView.findViewById(R.id.totalTime);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            likeButton = itemView.findViewById(R.id.likeButton);

            likeCount = itemView.findViewById(R.id.likeCount);

            imageGridView.setLayoutManager(new GridLayoutManager(context, 3)); // Adjust span count as needed
        }
        public void startUpdatingSeekBar() {
            updateSeekBarRunnable = new Runnable() {
                @Override
                public void run() {
                    if (video.isPlaying()) {
                        int currentPosition = video.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        currentTime.setText(formatTime(currentPosition));
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(updateSeekBarRunnable);
        }

        public void stopUpdatingSeekBar() {
            handler.removeCallbacks(updateSeekBarRunnable);
        }

        private String formatTime(int milliseconds) {
            int seconds = milliseconds / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}

