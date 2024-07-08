package com.example.weibo_tianfangming;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatar;
    public TextView username;
    public TextView title;
    public ImageView image;
    public RecyclerView imageGridView; // Declare RecyclerView for multiple images
    public VideoView video;
    public Button deleteButton;
    public Button commentButton;
    public ImageView likeButton;

    public ViewHolder(View itemView) {
        super(itemView);
        avatar = itemView.findViewById(R.id.avatar);
        username = itemView.findViewById(R.id.username);
        title = itemView.findViewById(R.id.title);
        image = itemView.findViewById(R.id.image);
        imageGridView = itemView.findViewById(R.id.imageGridView); // Initialize imageGridView
        video = itemView.findViewById(R.id.video);
        deleteButton = itemView.findViewById(R.id.deleteButton);
        commentButton = itemView.findViewById(R.id.commentButton);
        likeButton = itemView.findViewById(R.id.likeButton);
    }
}
