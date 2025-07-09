package com.vishwajeeth.medicinetime.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.local.UserProfile;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<UserProfile> profileList;
    private OnProfileClickListener listener;

    public interface OnProfileClickListener {
        void onProfileClick(UserProfile profile);

        void onProfileEdit(UserProfile profile);

        void onProfileDelete(UserProfile profile);
    }

    public ProfileAdapter(List<UserProfile> profileList, OnProfileClickListener listener) {
        this.profileList = profileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        UserProfile profile = profileList.get(position);
        holder.bind(profile);
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProfileName;
        private TextView tvProfileRelation;
        private TextView tvProfileAge;
        private ImageButton btnEdit;
        private ImageButton btnDelete;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProfileName = itemView.findViewById(R.id.tv_profile_name);
            tvProfileRelation = itemView.findViewById(R.id.tv_profile_relation);
            tvProfileAge = itemView.findViewById(R.id.tv_profile_age);
            btnEdit = itemView.findViewById(R.id.btn_edit_profile);
            btnDelete = itemView.findViewById(R.id.btn_delete_profile);

            // Click vào item để chọn profile
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProfileClick(profileList.get(position));
                    }
                }
            });

            // Nút sửa profile
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProfileEdit(profileList.get(position));
                    }
                }
            });

            // Nút xóa profile
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProfileDelete(profileList.get(position));
                    }
                }
            });
        }

        public void bind(UserProfile profile) {
            tvProfileName.setText(profile.name);
            tvProfileRelation.setText(profile.relation != null ? profile.relation : "");
            if (profile.age != null) {
                tvProfileAge.setText(profile.age + " tuổi");
            } else {
                tvProfileAge.setText("");
            }
        }
    }
}