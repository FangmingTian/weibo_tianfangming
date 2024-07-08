package com.example.weibo_tianfangming;

import static android.media.tv.TvTrackInfo.TYPE_VIDEO;

import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class WeiboPost {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private String title;
    private String videoUrl;
    private String poster;
    private List<String> images;
    private int likeCount;
    private boolean likeFlag;
    private long createTime;
    private int type; // 0 - Text, 1 - Image, 2 - Video

    // Getter and Setter methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLikeFlag() {
        return likeFlag;
    }

    public void setLikeFlag(boolean likeFlag) {
        this.likeFlag = likeFlag;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    // Helper method to determine the type of content
    // Method to get the number of images
    public int getImageCount() {
        if (images != null) {
            return images.size();
        } else {
            return 0;
        }
    }
}