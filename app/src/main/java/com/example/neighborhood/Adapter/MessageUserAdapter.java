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

public class MessageUserAdapter extends RecyclerView.Adapter<MessageUserAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;

    public MessageUserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.usernameTextView.setText(user.getUsername());
        // You can also set profile image here
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
            Glide.with(context)
                    .load(user.getImage()) // Replace with the actual field in User
                    .apply(requestOptions)
                    .placeholder(R.drawable.ic_profile) // Placeholder image while loading
                    .error(R.drawable.ic_profile) // Error image if loading fails
                    .into(holder.profileImageView);
        } else {
            // If there's no profile picture, you can set a default image
            holder.profileImageView.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView usernameTextView;
        TextView lastMessageTextView;
        ImageView profileImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
        }
    }
}
