package com.example.messengerprm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.widget.Toast;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.viewholder> {
    private Context context;
    ArrayList<Users> usersArrayList;
    private boolean isSearchMode = false;
    
    public UserAdpter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
    }
    
    public void setSearchMode(boolean isSearchMode) {
        this.isSearchMode = isSearchMode;
    }

    @NonNull
    @Override
    public UserAdpter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdpter.viewholder holder, int position) {
        Users users = usersArrayList.get(position);
        holder.username.setText(users.userName);
        
        holder.lastMessage.setVisibility(View.GONE);
        
        String profilePic = users.profilepic;
        if (profilePic != null && !profilePic.isEmpty()) {
            if (ImageUtils.isBase64Image(profilePic)) {
                holder.userimg.setImageBitmap(ImageUtils.convertBase64ToBitmap(profilePic));
            } else {
                Picasso.get().load(profilePic).placeholder(R.drawable.photocamera).into(holder.userimg);
            }
        } else {
            holder.userimg.setImageResource(R.drawable.photocamera);
        }
        
        int unreadCount = users.getUnreadCount();
        Log.d("UserAdapter", "User: " + users.userName + ", UnreadCount: " + unreadCount + ", Badge visibility: " + (unreadCount > 0 ? "VISIBLE" : "GONE"));
        if (unreadCount > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(unreadCount));
            Log.d("UserAdapter", "Showing badge for " + users.userName + ": " + unreadCount);
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
            Log.d("UserAdapter", "Hiding badge for " + users.userName);
        }
        
        holder.btnReject.setVisibility(View.GONE);

        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String otherUid = users.userId;
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        
        db.child("friends").child(currentUid).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (isSearchMode) {
                        holder.btnAddFriend.setVisibility(View.VISIBLE);
                        holder.btnAddFriend.setEnabled(true);
                        holder.btnAddFriend.setText(context.getString(R.string.unfriend_button));
                        
                        holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                db.child("friends").child(currentUid).child(otherUid).removeValue();
                                db.child("friends").child(otherUid).child(currentUid).removeValue();
                                holder.btnAddFriend.setText(context.getString(R.string.add_friend_button));
                                holder.btnAddFriend.setEnabled(true);
                                holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        db.child("friendRequests").child(otherUid).child(currentUid).setValue(true);
                                        holder.btnAddFriend.setText(context.getString(R.string.pending_button));
                                        holder.btnAddFriend.setEnabled(false);
                                    }
                                });
                                Toast.makeText(context, context.getString(R.string.unfriend_success), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        holder.btnAddFriend.setVisibility(View.GONE);
                    }
                    
                    String roomId;
                    if (currentUid.compareTo(otherUid) < 0) {
                        roomId = currentUid + otherUid;
                    } else {
                        roomId = otherUid + currentUid;
                    }
                    
                    db.child("chats").child(roomId).child("messages").orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                    String message = messageSnapshot.child("message").getValue(String.class);
                                    String sender = messageSnapshot.child("senderId").getValue(String.class);
                                    if (sender == null) sender = messageSnapshot.child("senderid").getValue(String.class);
                                    String messageType = messageSnapshot.child("type").getValue(String.class);
                                    if (messageType == null) messageType = messageSnapshot.child("messageType").getValue(String.class);
                                    
                                    if (message != null && sender != null) {
                                        if (sender.equals(currentUid)) {
                                            if ("image".equals(messageType)) {
                                                holder.lastMessage.setText("Bạn: [Hình ảnh]");
                                            } else {
                                                holder.lastMessage.setText("Bạn: " + message);
                                            }
                                        } else {
                                            if ("image".equals(messageType)) {
                                                holder.lastMessage.setText("[Hình ảnh]");
                                            } else {
                                                holder.lastMessage.setText(message);
                                            }
                                        }
                                        holder.lastMessage.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                holder.lastMessage.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            holder.lastMessage.setVisibility(View.GONE);
                        }
                    });
                } else {
                    if (isSearchMode) {
                        
                        db.child("friendRequests").child(otherUid).child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    holder.btnAddFriend.setVisibility(View.VISIBLE);
                                    holder.btnAddFriend.setEnabled(false);
                                    holder.btnAddFriend.setText(context.getString(R.string.pending_button));
                                } else {
                                    db.child("friendRequests").child(currentUid).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                holder.btnAddFriend.setVisibility(View.VISIBLE);
                                                holder.btnAddFriend.setEnabled(true);
                                                holder.btnAddFriend.setText(context.getString(R.string.accept_button));
                                                holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        db.child("friends").child(currentUid).child(otherUid).setValue(true);
                                                        db.child("friends").child(otherUid).child(currentUid).setValue(true);
                                                        db.child("friendRequests").child(currentUid).child(otherUid).removeValue();
                                                        holder.btnAddFriend.setText(context.getString(R.string.unfriend_button));
                                                        holder.btnAddFriend.setEnabled(true);
                                                        holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                db.child("friends").child(currentUid).child(otherUid).removeValue();
                                                                db.child("friends").child(otherUid).child(currentUid).removeValue();
                                                                holder.btnAddFriend.setText(context.getString(R.string.add_friend_button));
                                                                holder.btnAddFriend.setEnabled(true);
                                                                holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        db.child("friendRequests").child(otherUid).child(currentUid).setValue(true);
                                                                        holder.btnAddFriend.setText(context.getString(R.string.pending_button));
                                                                        holder.btnAddFriend.setEnabled(false);
                                                                    }
                                                                });
                                                                Toast.makeText(context, context.getString(R.string.unfriend_success), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        Toast.makeText(context, context.getString(R.string.accept_friend_success), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                holder.btnAddFriend.setVisibility(View.VISIBLE);
                                                holder.btnAddFriend.setEnabled(true);
                                                holder.btnAddFriend.setText(context.getString(R.string.add_friend_button));
                                                holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        db.child("friendRequests").child(otherUid).child(currentUid).setValue(true);
                                                        holder.btnAddFriend.setText(context.getString(R.string.pending_button));
                                                        holder.btnAddFriend.setEnabled(false);
                                                    }
                                                });
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    } else {
                        holder.itemView.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.child("friends").child(currentUid).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Intent intent = new Intent(context, chatWin.class);
                            intent.putExtra("nameeee",users.getUserName());
                            intent.putExtra("reciverImg",users.getProfilepic());
                            intent.putExtra("uid",users.getUserId());
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, context.getString(R.string.friends_only_message), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public void filterList(ArrayList<Users> filteredList) {
        Log.d("UserAdapter", "filterList called with " + filteredList.size() + " users");
        this.usersArrayList = filteredList;
        notifyDataSetChanged();
        Log.d("UserAdapter", "Adapter notified of data change");
        
        for (Users user : filteredList) {
            Log.d("UserAdapter", "User in list: " + user.userName + ", UnreadCount: " + user.getUnreadCount());
        }
    }
    
    public void updateLastMessage(String userId, String message, boolean isFromCurrentUser) {
        for (int i = 0; i < usersArrayList.size(); i++) {
            Users user = usersArrayList.get(i);
            if (user.getUserId().equals(userId)) {
                if (isFromCurrentUser) {
                    user.setLastMessage("Bạn: " + message);
                } else {
                    user.setLastMessage(message);
                }
                notifyItemChanged(i);
                break;
            }
        }
    }
    


    public class viewholder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username;
        TextView lastMessage;
        Button btnAddFriend;
        Button btnReject;
        TextView unreadBadge;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
            btnReject = itemView.findViewById(R.id.btnReject);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
        }
    }
}
