package com.example.neighborhood.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.R;
import com.example.neighborhood.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
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
        // Set the user data in the item_user.xml layout
//        holder.profilePictureImageView.setImageResource(R.drawable.default_profile_picture); // Set the profile picture from user.getProfilePictureUrl() if available
        holder.nameTextView.setText(user.getName());
        holder.usernameTextView.setText(user.getUsername());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
//        ImageView profilePictureImageView;
        TextView nameTextView;
        TextView usernameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
//            profilePictureImageView = itemView.findViewById(R.id.profile_picture);
            nameTextView = itemView.findViewById(R.id.user_name);
            usernameTextView = itemView.findViewById(R.id.user_username);
        }
    }
}
