package com.example.neighborhood.Adapter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.Fragment.CommunityCommentsFragment;
import com.example.neighborhood.Fragment.PostCommentsFragment;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private List<CommunityPost> communityPosts;
    private DatabaseReference usersRef;
    private FragmentActivity context;

    public CommunityAdapter(List<CommunityPost> communityPosts, FragmentActivity context) {
        this.communityPosts = communityPosts;
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        this.context = context;
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

        String userId = post.getUserId();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getImage() != null && !user.getImage().isEmpty()) {
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(holder.itemView.getContext())
                                .load(user.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.profileImageView);
                    } else {
                        Glide.with(holder.itemView.getContext()).load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                    holder.nameTextView.setText(user.getName());
                    holder.usernameTextView.setText("@" + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.titleTextView.setText(post.getTopic());
        holder.descriptionTextView.setText(post.getDescription());
        CharSequence timestampFormatted = DateUtils.getRelativeTimeSpanString(post.getTimestamp(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestampTextView.setText(timestampFormatted);

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("post", post);
                CommunityCommentsFragment commentsFragment = new CommunityCommentsFragment();
                commentsFragment.setArguments(bundle);
                context.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, commentsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments").child(post.getTopicId());
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numComments = (int) dataSnapshot.getChildrenCount();
                holder.commentTextView.setText("Comments " + numComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (post.getUserId().equals(getCurrentUserId())) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setMessage("Delete this post?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCommunityPostAndComments(post);
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();
                    return true;
                } else {
                    return true;
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return communityPosts.size();
    }

    public void setCommunityPosts(List<CommunityPost> posts) {
        communityPosts = posts;
        notifyDataSetChanged();
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView usernameTextView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView timestampTextView;
        ImageView commentButton;
        TextView commentTextView;
        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            commentButton = itemView.findViewById(R.id.comment_button);
            commentTextView = itemView.findViewById(R.id.comment_text_view);
        }
    }

    private void navigateToCommunityCommentsFragment(CommunityPost communityPost) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("communitypost", communityPost);

        PostCommentsFragment commentsFragment = new PostCommentsFragment();
        commentsFragment.setArguments(bundle);

        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, commentsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deleteCommunityPostAndComments(CommunityPost post) {
        communityPosts.remove(post);
        notifyDataSetChanged();

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("CommunityPosts").child(post.getTopicId());
        postsRef.removeValue();

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments").child(post.getTopicId());
        commentsRef.removeValue();
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
}
