package com.example.neighborhood.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.User;
import com.example.neighborhood.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnFollowButtonClickListener followButtonClickListener;

    public UserAdapter(List<User> userList, OnFollowButtonClickListener followButtonClickListener) {
        this.userList = userList;
        this.followButtonClickListener = followButtonClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.usernameTextView.setText(user.getUsername());

        // Set the follow button text and click listener
        if (user.isFollowStatus()) {
            holder.followButton.setText("Following");
        } else {
            holder.followButton.setText("Follow");
        }

        holder.followButton.setOnClickListener(v -> {
            // Call the click listener to handle the follow button click
            if (followButtonClickListener != null) {
                boolean newFollowStatus = !user.isFollowStatus();
                followButtonClickListener.onFollowButtonClick(position, newFollowStatus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView usernameTextView;
        Button followButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            usernameTextView = itemView.findViewById(R.id.user_username);
            followButton = itemView.findViewById(R.id.follow_button);
        }
    }

    public interface OnFollowButtonClickListener {
        void onFollowButtonClick(int position, boolean newFollowStatus);
    }
}
