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

public class SettingsFriendAdapter extends RecyclerView.Adapter<SettingsFriendAdapter.ViewHolder> {
    Context context;
    ArrayList<Users> friendList;

    public SettingsFriendAdapter(Context context, ArrayList<Users> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = friendList.get(position);
        holder.username.setText(user.userName);
        
        if (user.profilepic != null && !user.profilepic.isEmpty()) {
            if (ImageUtils.isBase64Image(user.profilepic)) {
                holder.userimg.setImageBitmap(ImageUtils.convertBase64ToBitmap(user.profilepic));
            } else {
                com.squareup.picasso.Picasso.get().load(user.profilepic).placeholder(R.drawable.photocamera).into(holder.userimg);
            }
        } else {
            holder.userimg.setImageResource(R.drawable.photocamera);
        }
        
        holder.btnAddFriend.setVisibility(View.VISIBLE);
        holder.btnAddFriend.setEnabled(true);
        holder.btnAddFriend.setText(context.getString(R.string.unfriend_button));
        holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String otherUid = user.userId;
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                
                db.child("friends").child(currentUid).child(otherUid).removeValue();
                db.child("friends").child(otherUid).child(currentUid).removeValue();
                
                Toast.makeText(context, context.getString(R.string.unfriend_success), Toast.LENGTH_SHORT).show();
            }
        });
        
        holder.btnReject.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void filterList(ArrayList<Users> filteredList) {
        this.friendList = filteredList;
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