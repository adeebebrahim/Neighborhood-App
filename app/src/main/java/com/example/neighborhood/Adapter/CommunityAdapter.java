package com.example.neighborhood.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.R;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private List<CommunityPost> communityPosts;

    public CommunityAdapter(List<CommunityPost> communityPosts) {
        this.communityPosts = communityPosts;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        CommunityPost post = communityPosts.get(position);
        holder.titleTextView.setText(post.getTopic()); // Set the title from the CommunityPost
        holder.descriptionTextView.setText(post.getDescription()); // Set the description from the CommunityPost
    }

    @Override
    public int getItemCount() {
        return communityPosts.size();
    }

    public void setCommunityPosts(List<CommunityPost> posts) {
        communityPosts = posts;
        notifyDataSetChanged();
    }

    public void addPosts(List<CommunityPost> posts) {
        communityPosts.addAll(posts);
        notifyDataSetChanged();
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
        }
    }
}
