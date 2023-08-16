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
    private OnUserItemClickListener itemClickListener;

    public interface OnUserItemClickListener {
        void onUserItemClick(User user);
    }

    public void setOnUserItemClickListener(OnUserItemClickListener listener) {
        this.itemClickListener = listener;
    }

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

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
            Glide.with(context)
                    .load(user.getImage())
                    .apply(requestOptions)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.profileImageView);
        } else {
            Glide.with(context).load(R.drawable.ic_profile).into(holder.profileImageView);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onUserItemClick(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView usernameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
        }
    }
}
