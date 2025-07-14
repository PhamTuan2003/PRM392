package com.example.messengerprm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    Context context;
    ArrayList<Users> friendRequestList;

    public FriendRequestAdapter(Context context, ArrayList<Users> friendRequestList) {
        this.context = context;
        this.friendRequestList = friendRequestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = friendRequestList.get(position);
        holder.username.setText(user.userName);
        // Đã xóa userstatus
        if (user.profilepic != null && !user.profilepic.isEmpty()) {
            if (ImageUtils.isBase64Image(user.profilepic)) {
                holder.userimg.setImageBitmap(ImageUtils.convertBase64ToBitmap(user.profilepic));
            } else {
                // Nếu là URL
                com.squareup.picasso.Picasso.get().load(user.profilepic).placeholder(R.drawable.photocamera).into(holder.userimg);
            }
        } else {
            holder.userimg.setImageResource(R.drawable.photocamera);
        }
        
        // Hiển thị nút "Chấp nhận"
        holder.btnAddFriend.setVisibility(View.VISIBLE);
        holder.btnAddFriend.setEnabled(true);
        holder.btnAddFriend.setText(context.getString(R.string.accept_button));
        holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String otherUid = user.userId;
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                // Chấp nhận kết bạn
                db.child("friends").child(currentUid).child(otherUid).setValue(true);
                db.child("friends").child(otherUid).child(currentUid).setValue(true);
                db.child("friendRequests").child(currentUid).child(otherUid).removeValue();
                Toast.makeText(context, context.getString(R.string.accept_friend_success), Toast.LENGTH_SHORT).show();
            }
        });
        
        // Thêm nút "Hủy" (từ chối lời mời)
        holder.btnReject.setVisibility(View.VISIBLE);
        holder.btnReject.setEnabled(true);
        holder.btnReject.setText(context.getString(R.string.reject_button));
        holder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String otherUid = user.userId;
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                // Từ chối lời mời kết bạn
                db.child("friendRequests").child(currentUid).child(otherUid).removeValue();
                Toast.makeText(context, context.getString(R.string.reject_friend_success), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }

    public void filterList(ArrayList<Users> filteredList) {
        this.friendRequestList = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username;
        Button btnAddFriend;
        Button btnReject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
} 