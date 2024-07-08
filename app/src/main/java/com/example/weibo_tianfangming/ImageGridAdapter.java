package com.example.weibo_tianfangming;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ImageViewHolder> {
    private List<String> imageUrls;
    private Context context;
    private String username;
    private String avatar;

    private AdapterView.OnItemClickListener onItemClickListener;

    public ImageGridAdapter(List<String> imageUrls, Context context, String username, String avatar) {
        this.imageUrls = imageUrls;
        this.context = context;
        this.username = username;
        this.avatar = avatar;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_grid, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context).load(imageUrl).into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageViewerActivity.class);
            intent.putExtra("imageUrls", imageUrls.toArray(new String[0]));
            intent.putExtra("index", position);
            intent.putExtra("username", username);
            intent.putExtra("avatar", avatar);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}