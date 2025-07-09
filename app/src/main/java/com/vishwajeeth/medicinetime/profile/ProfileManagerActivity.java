package com.vishwajeeth.medicinetime.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.local.MedicineDBHelper;
import com.vishwajeeth.medicinetime.data.source.local.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class ProfileManagerActivity extends AppCompatActivity implements ProfileAdapter.OnProfileClickListener {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<UserProfile> profileList;
    private MedicineDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manager);

        // Khởi tạo database helper
        dbHelper = new MedicineDBHelper(this);

        // Thiết lập toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý thành viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recycler_profiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        profileList = new ArrayList<>();
        adapter = new ProfileAdapter(profileList, this);
        recyclerView.setAdapter(adapter);

        // Nút thêm profile mới
        FloatingActionButton fabAddProfile = findViewById(R.id.fab_add_profile);
        fabAddProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileManagerActivity.this, AddEditProfileActivity.class);
                startActivity(intent);
            }
        });

        // Load danh sách profile
        loadProfiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfiles(); // Reload khi quay lại màn hình
    }

    private void loadProfiles() {
        profileList.clear();
        profileList.addAll(dbHelper.getAllUserProfiles());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onProfileClick(UserProfile profile) {
        // Chọn profile này làm profile đang sử dụng
        saveCurrentProfile(profile);
        Toast.makeText(this, "Đã chọn: " + profile.name, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onProfileEdit(UserProfile profile) {
        Intent intent = new Intent(this, AddEditProfileActivity.class);
        intent.putExtra("profile_id", profile.id);
        intent.putExtra("profile_name", profile.name);
        intent.putExtra("profile_age", profile.age);
        intent.putExtra("profile_relation", profile.relation);
        intent.putExtra("profile_avatar", profile.avatarUri);
        startActivity(intent);
    }

    @Override
    public void onProfileDelete(UserProfile profile) {
        // Xóa profile
        int result = dbHelper.deleteUserProfile(profile.id);
        if (result > 0) {
            Toast.makeText(this, "Đã xóa: " + profile.name, Toast.LENGTH_SHORT).show();
            loadProfiles();
        } else {
            Toast.makeText(this, "Lỗi khi xóa profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentProfile(UserProfile profile) {
        // Lưu profile đang sử dụng vào SharedPreferences
        getSharedPreferences("MedicineApp", MODE_PRIVATE)
                .edit()
                .putInt("current_profile_id", profile.id)
                .putString("current_profile_name", profile.name)
                .apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}