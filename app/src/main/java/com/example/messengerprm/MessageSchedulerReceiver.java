package com.example.messengerprm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class MessageSchedulerReceiver extends BroadcastReceiver {
    private static final String TAG = "MessageSchedulerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received broadcast: " + intent.getAction());
        
        if ("SEND_SCHEDULED_MESSAGE".equals(intent.getAction())) {
            String messageId = intent.getStringExtra("messageId");
            if (messageId != null) {
                Log.d(TAG, "Processing scheduled message: " + messageId);
                
                // Start the service to handle the message
                Intent serviceIntent = new Intent(context, MessageSchedulerService.class);
                serviceIntent.setAction("SEND_SCHEDULED_MESSAGE");
                serviceIntent.putExtra("messageId", messageId);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
} 