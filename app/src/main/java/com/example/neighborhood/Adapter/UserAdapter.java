package com.example.neighborhood.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.R;
import com.example.neighborhood.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private UserProfileClickListener userProfileClickListener;

    public UserAdapter(Context context, List<User> userList, UserProfileClickListener userProfileClickListener) {
        this.context = context;
        this.userList = userList;
        this.userProfileClickListener = userProfileClickListener;
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

        ImageView profileImageView = holder.itemView.findViewById(R.id.profile_picture);
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
            Glide.with(context)
                    .load(user.getImage())
                    .apply(requestOptions)
                    .error(R.drawable.ic_profile)
                    .into(profileImageView);
        } else {

            Glide.with(context).load(R.drawable.ic_profile).into(profileImageView);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userProfileClickListener != null) {
                    userProfileClickListener.onItemClick(user.getUserId());
                }
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

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            usernameTextView = itemView.findViewById(R.id.user_username);
        }
    }

    public interface UserProfileClickListener {
        void onItemClick(String userId);
    }
}
