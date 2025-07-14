package com.example.messengerprm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.content.pm.ServiceInfo;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MessageSchedulerService extends Service {
    private static final String TAG = "MessageSchedulerService";
    private static final String CHANNEL_ID = "message_scheduler_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference scheduledMessagesRef;
    
    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        scheduledMessagesRef = database.getReference("scheduled_messages");
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        // Start foreground service with notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires foreground service type
            // Use 0x40000000 for FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            startForeground(NOTIFICATION_ID, createNotification(), 0x40000000);
        } else {
            startForeground(NOTIFICATION_ID, createNotification());
        }
        
        if (intent != null && "SEND_SCHEDULED_MESSAGE".equals(intent.getAction())) {
            // Handle specific scheduled message
            String messageId = intent.getStringExtra("messageId");
            if (messageId != null) {
                Log.d(TAG, "Processing scheduled message: " + messageId);
                processScheduledMessage(messageId);
            }
        } else {
            // Check for scheduled messages
            checkScheduledMessages();
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Message Scheduler",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Service for sending scheduled messages");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Message Scheduler")
            .setContentText("Monitoring scheduled messages...")
            .setSmallIcon(R.drawable.ic_schedule)
            .setPriority(NotificationCompat.PRIORITY_LOW);
        
        return builder.build();
    }
    
    private void checkScheduledMessages() {
        long currentTime = System.currentTimeMillis();
        
        scheduledMessagesRef.orderByChild("scheduledTime")
            .startAt(currentTime)
            .endAt(currentTime + 60000) // Check messages scheduled in the next minute
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ScheduledMessage scheduledMessage = snapshot.getValue(ScheduledMessage.class);
                        if (scheduledMessage != null && !scheduledMessage.isSent()) {
                            sendScheduledMessage(scheduledMessage, snapshot.getKey());
                        }
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error checking scheduled messages: " + databaseError.getMessage());
                }
            });
    }
    
    private void processScheduledMessage(String messageId) {
        scheduledMessagesRef.child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ScheduledMessage scheduledMessage = dataSnapshot.getValue(ScheduledMessage.class);
                if (scheduledMessage != null && !scheduledMessage.isSent()) {
                    sendScheduledMessage(scheduledMessage, messageId);
                } else {
                    Log.d(TAG, "Scheduled message not found or already sent: " + messageId);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error processing scheduled message: " + databaseError.getMessage());
            }
        });
    }
    
    private void sendScheduledMessage(ScheduledMessage scheduledMessage, String messageId) {
        Log.d(TAG, "Sending scheduled message: " + messageId);
        
        // Create the actual message to send
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", scheduledMessage.getMessage());
        messageMap.put("senderId", scheduledMessage.getSenderId());
        messageMap.put("type", scheduledMessage.getType());
        messageMap.put("timestamp", System.currentTimeMillis());
        
        if (scheduledMessage.getImageUrl() != null) {
            messageMap.put("imageUrl", scheduledMessage.getImageUrl());
        }
        
        // Send to chat room
        DatabaseReference chatRef = database.getReference()
            .child("chats")
            .child(scheduledMessage.getRoomId())
            .child("messages")
            .push();
        
        chatRef.setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Scheduled message sent successfully");
                    
                    // Update scheduled message status
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("isSent", true);
                    updateMap.put("status", "sent");
                    updateMap.put("sentAt", System.currentTimeMillis());
                    
                    scheduledMessagesRef.child(messageId).updateChildren(updateMap);
                } else {
                    Log.e(TAG, "Failed to send scheduled message: " + task.getException());
                    
                    // Update status to failed
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("status", "failed");
                    updateMap.put("error", task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    
                    scheduledMessagesRef.child(messageId).updateChildren(updateMap);
                }
            }
        });
    }
    
    public static void scheduleMessage(Context context, ScheduledMessage scheduledMessage) {
        try {
            // Validate context
            if (context == null) {
                Log.e(TAG, "Context is null");
                return;
            }
            
            // Validate scheduledMessage
            if (scheduledMessage == null) {
                Log.e(TAG, "ScheduledMessage is null");
                return;
            }
            
            Log.d(TAG, "Starting to schedule message: " + scheduledMessage.getMessage());
            
            // Save to Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("scheduled_messages").push();
            scheduledMessage.setMessageId(ref.getKey());
            
            Log.d(TAG, "Message ID generated: " + scheduledMessage.getMessageId());
            
            ref.setValue(scheduledMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Scheduled message saved to Firebase");
                        
                        try {
                            // Start the service
                            Intent serviceIntent = new Intent(context, MessageSchedulerService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent);
                            } else {
                                context.startService(serviceIntent);
                            }
                            
                            Log.d(TAG, "Service started successfully");
                            
                            // Schedule alarm for Android
                            scheduleAlarm(context, scheduledMessage);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting service or scheduling alarm: " + e.getMessage(), e);
                        }
                    } else {
                        Log.e(TAG, "Failed to save scheduled message: " + task.getException());
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in scheduleMessage: " + e.getMessage(), e);
        }
    }
    
    private static void scheduleAlarm(Context context, ScheduledMessage scheduledMessage) {
        try {
            Log.d(TAG, "Scheduling alarm for message: " + scheduledMessage.getMessageId());
            
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }
            
            Intent intent = new Intent(context, MessageSchedulerReceiver.class);
            intent.setAction("SEND_SCHEDULED_MESSAGE");
            intent.putExtra("messageId", scheduledMessage.getMessageId());
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduledMessage.getMessageId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            try {
                // Check if we can schedule exact alarms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            scheduledMessage.getScheduledTime(),
                            pendingIntent
                        );
                        Log.d(TAG, "Exact alarm scheduled successfully");
                    } else {
                        // Fallback to inexact alarm
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            scheduledMessage.getScheduledTime(),
                            pendingIntent
                        );
                        Log.d(TAG, "Inexact alarm scheduled (exact alarm permission not granted)");
                    }
                } else {
                    // For older Android versions, use exact alarm
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledMessage.getScheduledTime(),
                        pendingIntent
                    );
                    Log.d(TAG, "Exact alarm scheduled for older Android version");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException when scheduling alarm: " + e.getMessage());
                // Fallback to inexact alarm
                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledMessage.getScheduledTime(),
                        pendingIntent
                    );
                    Log.d(TAG, "Fallback to inexact alarm due to security exception");
                } catch (Exception fallbackException) {
                    Log.e(TAG, "Failed to schedule even inexact alarm: " + fallbackException.getMessage());
                }
            }
            
            Log.d(TAG, "Alarm scheduling completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm: " + e.getMessage(), e);
        }
    }
    
    public static void cancelScheduledMessage(Context context, String messageId) {
        // Cancel alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MessageSchedulerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            messageId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        
        // Remove from Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("scheduled_messages").child(messageId).removeValue();
    }
} 