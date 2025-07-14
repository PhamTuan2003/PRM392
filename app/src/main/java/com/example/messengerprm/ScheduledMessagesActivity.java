package com.example.messengerprm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduledMessagesActivity extends AppCompatActivity {
    private static final String TAG = "ScheduledMessagesActivity";
    
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private List<ScheduledMessage> scheduledMessages;
    private ScheduledMessagesAdapter adapter;
    
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_messages);
        
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        
        recyclerView = findViewById(R.id.recycler_scheduled_messages);
        tvEmpty = findViewById(R.id.tv_empty);
        
        scheduledMessages = new ArrayList<>();
        adapter = new ScheduledMessagesAdapter(this, scheduledMessages);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        loadScheduledMessages();
    }
    
    private void loadScheduledMessages() {
        DatabaseReference scheduledRef = database.getReference("scheduled_messages");
        scheduledRef.orderByChild("senderId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                scheduledMessages.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ScheduledMessage message = snapshot.getValue(ScheduledMessage.class);
                    if (message != null) {
                        message.setMessageId(snapshot.getKey());
                        scheduledMessages.add(message);
                    }
                }
                
                adapter.notifyDataSetChanged();
                
                if (scheduledMessages.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading scheduled messages: " + databaseError.getMessage());
            }
        });
    }
    
    public void cancelScheduledMessage(String messageId) {
        new AlertDialog.Builder(this)
            .setTitle("Hủy lịch tin nhắn")
            .setMessage("Bạn có muốn hủy lịch tin nhắn này không?")
            .setPositiveButton("Hủy lịch", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MessageSchedulerService.cancelScheduledMessage(ScheduledMessagesActivity.this, messageId);
                }
            })
            .setNegativeButton("Không", null)
            .show();
    }
    
    public void deleteScheduledMessage(String messageId) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa tin nhắn đã gửi")
            .setMessage("Bạn có muốn xóa tin nhắn này khỏi danh sách không?")
            .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatabaseReference scheduledRef = database.getReference("scheduled_messages");
                    scheduledRef.child(messageId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Scheduled message deleted successfully");
                            } else {
                                Log.e(TAG, "Failed to delete scheduled message: " + task.getException());
                            }
                        }
                    });
                }
            })
            .setNegativeButton("Không", null)
            .show();
    }
    
    public void viewMessageInChat(ScheduledMessage message) {
        // Navigate to chat with the receiver
        Intent intent = new Intent(this, chatWin.class);
        intent.putExtra("nameeee", getReceiverName(message.getReceiverId()));
        intent.putExtra("reciverImg", getReceiverImage(message.getReceiverId()));
        intent.putExtra("uid", message.getReceiverId());
        startActivity(intent);
    }
    
    private String getReceiverName(String receiverId) {
        // Try to get receiver name from friends list
        DatabaseReference friendsRef = database.getReference("friends").child(currentUserId);
        friendsRef.orderByChild("uid").equalTo(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) {
                        // Store the name for later use
                        // For now, we'll use a simple approach
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting receiver name: " + databaseError.getMessage());
            }
        });
        
        // For now, return a default name
        return "Người nhận";
    }
    
    private String getReceiverImage(String receiverId) {
        // Try to get receiver image from friends list
        DatabaseReference friendsRef = database.getReference("friends").child(currentUserId);
        friendsRef.orderByChild("uid").equalTo(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String image = snapshot.child("profilepic").getValue(String.class);
                    if (image != null) {
                        // Store the image for later use
                        // For now, we'll use a simple approach
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting receiver image: " + databaseError.getMessage());
            }
        });
        
        // For now, return empty string
        return "";
    }
    
    public static String formatScheduledTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
} 