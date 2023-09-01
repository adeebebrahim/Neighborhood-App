package com.example.neighborhood.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Message;
import com.example.neighborhood.R;
import com.example.neighborhood.Report;
import com.example.neighborhood.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String loggedInUserId;
    private DatabaseReference messagesRef;
    private ChildEventListener childEventListener;

    public MessageAdapter(Context context, List<Message> messageList, String loggedInUserId) {
        this.context = context;
        this.messageList = messageList;
        this.loggedInUserId = getCurrentUserId();
        this.messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Message newMessage = dataSnapshot.getValue(Message.class);

                if (newMessage != null) {
                    messageList.add(newMessage);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        messagesRef.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.messageTextView.setText(message.getMessageText());

        String senderUserId = message.getSenderUserId();
        if (senderUserId != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(senderUserId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User senderUser = dataSnapshot.getValue(User.class);
                    if (senderUser != null && senderUser.getImage() != null && !senderUser.getImage().isEmpty()) {

                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(context)
                                .load(senderUser.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.profileImageView);
                    } else {

                        Glide.with(context).load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if (senderUserId.equals(getCurrentUserId())) {


            } else {


            }
        }


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (message.getSenderUserId().equals(getCurrentUserId())) {

                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
                    deleteBuilder.setMessage("Delete this message?");
                    deleteBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteMessage(message);
                        }
                    });
                    deleteBuilder.setNegativeButton("No", null);
                    deleteBuilder.show();
                } else {

                    AlertDialog.Builder reportBuilder = new AlertDialog.Builder(context);
                    reportBuilder.setMessage("Report this message?");
                    reportBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            showMessageReportReasonDialog(message);
                        }
                    });
                    reportBuilder.setNegativeButton("No", null);
                    reportBuilder.show();
                }

                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView profileImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
        }
    }

    private void deleteMessage(Message message) {
        String messageId = message.getMessageId();
        String senderUserId = message.getSenderUserId();
        String recipientUserId = message.getRecipientUserId();

        if (messageId != null && senderUserId != null && recipientUserId != null) {
            String chatId = generateChatId(senderUserId, recipientUserId);

            if (chatId != null) {
                DatabaseReference chatRef = messagesRef.child(chatId).child(messageId);
                chatRef.removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                messageList.remove(message);
                                notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        }
    }

    private String generateChatId(String senderUserId, String recipientUserId) {
        if (senderUserId != null && recipientUserId != null) {
            String currentUserUid = senderUserId;
            String otherUserUid = recipientUserId;



            if (currentUserUid.compareTo(otherUserUid) < 0) {
                return currentUserUid + "_" + otherUserUid;
            } else {
                return otherUserUid + "_" + currentUserUid;
            }
        }
        return null;
    }


    private void showMessageReportReasonDialog(Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Report Message");


        View reportReasonsView = LayoutInflater.from(context).inflate(R.layout.dialog_report_reasons, null);

        String[] reportReasons = {"Inappropriate content", "Spam", "Harassment", "Other"};

        ListView reasonsListView = reportReasonsView.findViewById(R.id.reasons_list_view);
        ArrayAdapter<String> reasonsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice, reportReasons);
        reasonsListView.setAdapter(reasonsAdapter);

        builder.setView(reportReasonsView);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = reasonsListView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    String selectedReason = reportReasons[selectedPosition];
                    showMessageReportConfirmationDialog(message, selectedReason);
                } else {


                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showMessageReportConfirmationDialog(Message message, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Report");
        builder.setMessage("Are you sure you want to report this message for the following reason?\n\n" + reason);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                saveMessageReportToFirebase(message, reason);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void saveMessageReportToFirebase(Message message, String reason) {
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("MessageReports");
        String reportId = reportsRef.push().getKey();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Report report = new Report(reportId, currentUser.getUid(), message.getMessageId(), null, reason, System.currentTimeMillis());
            reportsRef.child(reportId).setValue(report)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
}

