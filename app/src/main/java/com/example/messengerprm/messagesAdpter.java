

package com.example.messengerprm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class messagesAdpter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<msgModelclass> messagesArrayList;
    private String senderImg, reciverIImg;
    int ITEM_SEND=1;
    int ITEM_RECIVE=2;

    public messagesAdpter(Context context, ArrayList<msgModelclass> messagesArrayList, String senderImg, String reciverIImg) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
        this.senderImg = senderImg;
        this.reciverIImg = reciverIImg;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new senderVierwHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false);
            return new reciverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        msgModelclass messages = messagesArrayList.get(position);
        if (holder instanceof senderVierwHolder) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("Xóa tin nhắn")
                            .setMessage("Bạn có chắc muốn xóa tin nhắn này?")
                            .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteMessage(position, messages);
                                }
                            })
                            .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();

                    return true;
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
        if (holder instanceof senderVierwHolder){
            senderVierwHolder viewHolder = (senderVierwHolder) holder;
            
            // Kiểm tra loại tin nhắn
            if ("image".equals(messages.getType()) && messages.getImageUrl() != null) {
                // Hiển thị hình ảnh
                viewHolder.msgtxt.setVisibility(View.GONE);
                viewHolder.imageView.setVisibility(View.VISIBLE);
                
                // Kiểm tra xem có phải Base64 không
                if (ImageUtils.isBase64Image(messages.getImageUrl())) {
                    // Load từ Base64
                    Bitmap bitmap = ImageUtils.convertBase64ToBitmap(messages.getImageUrl());
                    if (bitmap != null) {
                        viewHolder.imageView.setImageBitmap(bitmap);
                    } else {
                        viewHolder.imageView.setImageResource(R.drawable.photocamera);
                    }
                } else {
                    // Load từ URL
                    Picasso.get().load(messages.getImageUrl())
                            .placeholder(R.drawable.photocamera)
                            .error(R.drawable.photocamera)
                            .into(viewHolder.imageView);
                }
                
                // Thêm click listener cho ảnh
                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ImageViewerActivity.class);
                        intent.putExtra("image_data", messages.getImageUrl());
                        intent.putExtra("message_type", messages.getType());
                        context.startActivity(intent);
                    }
                });
            } else {
                // Hiển thị tin nhắn text
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.imageView.setVisibility(View.GONE);
                viewHolder.msgtxt.setText(messages.getMessage());
            }
            
            // Hiển thị thời gian
            String timeText = formatTime(messages.getTimestamp());
            viewHolder.timeText.setText(timeText);
            
            // Kiểm tra senderImg trước khi load
            if (this.senderImg != null && !this.senderImg.isEmpty()) {
                if (ImageUtils.isBase64Image(this.senderImg)) {
                    Bitmap bitmap = ImageUtils.convertBase64ToBitmap(this.senderImg);
                    if (bitmap != null) {
                        viewHolder.circleImageView.setImageBitmap(bitmap);
                    } else {
                        viewHolder.circleImageView.setImageResource(R.drawable.photocamera);
                    }
                } else {
                    Picasso.get().load(this.senderImg)
                            .placeholder(R.drawable.photocamera)
                            .error(R.drawable.photocamera)
                            .into(viewHolder.circleImageView);
                }
            } else {
                viewHolder.circleImageView.setImageResource(R.drawable.photocamera);
            }

            // Hiển thị trạng thái đã xem cho tin nhắn cuối cùng do mình gửi
            if (position == messagesArrayList.size() - 1) {
                // Lấy key của receiver
                final String receiverId;
                if (context instanceof chatWin) {
                    receiverId = ((chatWin) context).reciverUid;
                } else {
                    receiverId = null;
                }
                if (receiverId != null) {
                    String senderRoom = FirebaseAuth.getInstance().getCurrentUser().getUid() + receiverId;
                    DatabaseReference lastReadRef = FirebaseDatabase.getInstance().getReference()
                        .child("chats").child(senderRoom)
                        .child("lastReadTimestamp").child(receiverId);
                    lastReadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                long lastRead = snapshot.getValue(Long.class);
                                // Nếu timestamp của tin nhắn này <= lastReadTimestamp của receiver, thì đã xem
                                if (messages.getTimestamp() <= lastRead) {
                                    viewHolder.seenText.setVisibility(View.VISIBLE);
                                } else {
                                    viewHolder.seenText.setVisibility(View.GONE);
                                }
                            } else {
                                viewHolder.seenText.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            viewHolder.seenText.setVisibility(View.GONE);
                        }
                    });
                } else {
                    viewHolder.seenText.setVisibility(View.GONE);
                }
            } else {
                viewHolder.seenText.setVisibility(View.GONE);
            }
        }else if (holder instanceof reciverViewHolder) {
            reciverViewHolder viewHolder = (reciverViewHolder) holder;
            
            // Kiểm tra loại tin nhắn
            if ("image".equals(messages.getType()) && messages.getImageUrl() != null) {
                // Hiển thị hình ảnh
                viewHolder.msgtxt.setVisibility(View.GONE);
                viewHolder.imageView.setVisibility(View.VISIBLE);
                
                // Kiểm tra xem có phải Base64 không
                if (ImageUtils.isBase64Image(messages.getImageUrl())) {
                    // Load từ Base64
                    Bitmap bitmap = ImageUtils.convertBase64ToBitmap(messages.getImageUrl());
                    if (bitmap != null) {
                        viewHolder.imageView.setImageBitmap(bitmap);
                    } else {
                        viewHolder.imageView.setImageResource(R.drawable.photocamera);
                    }
                } else {
                    // Load từ URL
                    Picasso.get().load(messages.getImageUrl())
                            .placeholder(R.drawable.photocamera)
                            .error(R.drawable.photocamera)
                            .into(viewHolder.imageView);
                }
                
                // Thêm click listener cho ảnh
                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ImageViewerActivity.class);
                        intent.putExtra("image_data", messages.getImageUrl());
                        intent.putExtra("message_type", messages.getType());
                        context.startActivity(intent);
                    }
                });
            } else {
                // Hiển thị tin nhắn text
                viewHolder.msgtxt.setVisibility(View.VISIBLE);
                viewHolder.imageView.setVisibility(View.GONE);
                viewHolder.msgtxt.setText(messages.getMessage());
            }
            
            // Hiển thị thời gian
            String timeText = formatTime(messages.getTimestamp());
            viewHolder.timeText.setText(timeText);
            
            // Kiểm tra reciverIImg trước khi load
            if (this.reciverIImg != null && !this.reciverIImg.isEmpty()) {
                if (ImageUtils.isBase64Image(this.reciverIImg)) {
                    Bitmap bitmap = ImageUtils.convertBase64ToBitmap(this.reciverIImg);
                    if (bitmap != null) {
                        viewHolder.circleImageView.setImageBitmap(bitmap);
                    } else {
                        viewHolder.circleImageView.setImageResource(R.drawable.photocamera);
                    }
                } else {
                    Picasso.get().load(this.reciverIImg)
                            .placeholder(R.drawable.photocamera)
                            .error(R.drawable.photocamera)
                            .into(viewHolder.circleImageView);
                }
            } else {
                viewHolder.circleImageView.setImageResource(R.drawable.photocamera);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        msgModelclass messages = messagesArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECIVE;
        }
    }

    static class senderVierwHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgtxt;
        ImageView imageView;
        TextView timeText;
        TextView seenText;
        public senderVierwHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.profilerggg);
            msgtxt = itemView.findViewById(R.id.msgsendertyp);
            imageView = itemView.findViewById(R.id.sender_image);
            timeText = itemView.findViewById(R.id.sender_time);
            seenText = itemView.findViewById(R.id.sender_seen);
        }
    }
    static class reciverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgtxt;
        ImageView imageView;
        TextView timeText;
        public reciverViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.pro);
            msgtxt = itemView.findViewById(R.id.recivertextset);
            imageView = itemView.findViewById(R.id.receiver_image);
            timeText = itemView.findViewById(R.id.receiver_time);
        }
    }
    
    // Method để cập nhật hình ảnh
    public void updateImages(String senderImg, String reciverIImg) {
        this.senderImg = senderImg;
        this.reciverIImg = reciverIImg;
        notifyDataSetChanged();
    }
    

    
    // Method để format thời gian
    private String formatTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    // Hàm lấy key của message (dựa vào timeStamp và senderid)
    private String getMessageKey(msgModelclass msg) {
        // Nếu bạn lưu key ở msgModelclass thì dùng luôn, nếu không thì cần truyền key vào khi load tin nhắn
        // Ở đây tạm thời trả về timeStamp+senderid (không tối ưu, nên lưu key khi load từ Firebase)
        return String.valueOf(msg.getTimestamp()) + "_" + msg.getSenderId();
    }
    
    // Method để xóa tin nhắn
    private void deleteMessage(int position, msgModelclass message) {
        // Kiểm tra xem có phải tin nhắn của mình không
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!message.getSenderId().equals(currentUserId)) {
            Toast.makeText(context, "Bạn chỉ có thể xóa tin nhắn của mình", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lấy roomId từ context
        String roomId = null;
        if (context instanceof chatWin) {
            roomId = ((chatWin) context).roomId;
        }
        
        if (roomId == null) {
            Toast.makeText(context, "Không thể xác định phòng chat", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Sử dụng message key để xóa chính xác
        String messageKey = message.getMessageKey();
        if (messageKey == null || messageKey.isEmpty()) {
            Toast.makeText(context, "Không thể xác định tin nhắn để xóa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Xóa tin nhắn khỏi Firebase bằng key
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference()
                .child("chats").child(roomId).child("messages").child(messageKey);
        
        messageRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Tìm và xóa tin nhắn theo message key thay vì position
                                            String messageKey = message.getMessageKey();
                                            boolean found = false;
                                            
                                            if (messageKey != null && !messageKey.isEmpty()) {
                                                // Xóa khỏi messagesArrayList
                                                for (int i = 0; i < messagesArrayList.size(); i++) {
                                                    msgModelclass msg = messagesArrayList.get(i);
                                                    if (msg.getMessageKey() != null && msg.getMessageKey().equals(messageKey)) {
                                                        messagesArrayList.remove(i);
                                                        notifyItemRemoved(i);
                                                        // Chỉ notify range changed nếu còn items
                                                        if (i < messagesArrayList.size()) {
                                                            notifyItemRangeChanged(i, messagesArrayList.size() - i);
                                                        }
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                                

                                            }
                                            
                                            if (!found) {
                                                // Nếu không tìm thấy, refresh toàn bộ adapter
                                                notifyDataSetChanged();
                                            }
                                            
                                            Toast.makeText(context, "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show();
                                            
                                            // Cập nhật hiển thị tin nhắn gần nhất nếu cần
                                            if (context instanceof chatWin) {
                                                ((chatWin) context).updateLastMessageDisplay();
                                            }
                                        } else {
                                            Toast.makeText(context, "Không thể xóa tin nhắn", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
    }
}
