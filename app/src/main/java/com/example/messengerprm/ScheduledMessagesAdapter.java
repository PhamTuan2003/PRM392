package com.example.messengerprm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduledMessagesAdapter extends RecyclerView.Adapter<ScheduledMessagesAdapter.ViewHolder> {
    
    private Context context;
    private List<ScheduledMessage> scheduledMessages;
    
    public ScheduledMessagesAdapter(Context context, List<ScheduledMessage> scheduledMessages) {
        this.context = context;
        this.scheduledMessages = scheduledMessages;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_scheduled_message, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduledMessage message = scheduledMessages.get(position);
        
        holder.tvMessage.setText(message.getMessage());
        holder.tvScheduledTime.setText("Lên lịch: " + ScheduledMessagesActivity.formatScheduledTime(message.getScheduledTime()));
        holder.tvStatus.setText("Trạng thái: " + getStatusText(message.getStatus()));
        
        // Set status color based on status
        setStatusColor(holder.tvStatus, message.getStatus());
        
        // Handle button visibility based on status
        if ("sent".equals(message.getStatus()) || "failed".equals(message.getStatus())) {
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            
            // Show delete button for sent/failed messages
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof ScheduledMessagesActivity) {
                        ((ScheduledMessagesActivity) context).deleteScheduledMessage(message.getMessageId());
                    }
                }
            });
        } else {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);
            
            // Show cancel button for pending messages
            holder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof ScheduledMessagesActivity) {
                        ((ScheduledMessagesActivity) context).cancelScheduledMessage(message.getMessageId());
                    }
                }
            });
        }
        
        // Add click listener to view message in chat
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof ScheduledMessagesActivity) {
                    ((ScheduledMessagesActivity) context).viewMessageInChat(message);
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return scheduledMessages.size();
    }
    
    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "Chờ gửi";
            case "sent":
                return "Đã gửi";
            case "failed":
                return "Lỗi";
            default:
                return "Không xác định";
        }
    }
    
    private void setStatusColor(TextView textView, String status) {
        switch (status) {
            case "pending":
                textView.setTextColor(0xFF4CAF50); // Green
                break;
            case "sent":
                textView.setTextColor(0xFF2196F3); // Blue
                break;
            case "failed":
                textView.setTextColor(0xFFF44336); // Red
                break;
            default:
                textView.setTextColor(0xFFFFFFFF); // White
                break;
        }
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvScheduledTime, tvStatus;
        Button btnCancel, btnDelete;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvScheduledTime = itemView.findViewById(R.id.tv_scheduled_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
} 