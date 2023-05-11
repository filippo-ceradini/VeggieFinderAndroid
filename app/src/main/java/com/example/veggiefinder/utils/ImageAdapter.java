package com.example.veggiefinder.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.veggiefinder.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<String> imageUrls;
    private final OnImageSelectedListener onImageSelectedListener;
    private int currentlyCheckedPosition = -1; // -1 indicates that no item is checked

    public ImageAdapter(List<String> imageUrls, OnImageSelectedListener onImageSelectedListener) {
        this.imageUrls = imageUrls;
        this.onImageSelectedListener = onImageSelectedListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(url)
                .thumbnail(0.5f)
                .override(200, 200)
                .into(holder.imageView);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(position == currentlyCheckedPosition);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (currentlyCheckedPosition != -1) {
                    // Notify the previously checked item to update its checked state
                    notifyItemChanged(currentlyCheckedPosition);
                }
                currentlyCheckedPosition = holder.getAdapterPosition();
                onImageSelectedListener.onImageSelected(url);
            } else if (position == currentlyCheckedPosition) {
                // This item was just unchecked
                currentlyCheckedPosition = -1;
                onImageSelectedListener.onImageDeselected(url);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public CheckBox checkBox;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    public interface OnImageSelectedListener {
        void onImageSelected(String imageUrl);
        void onImageDeselected(String imageUrl);
    }
}
