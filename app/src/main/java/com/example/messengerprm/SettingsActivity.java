package com.example.messengerprm;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST = 101;
    CircleImageView settingsAvatar;
    TextView tvUserName, tvUserEmail;
    Button btnToggleTheme, btnLogout, btnChangeName, btnChangePassword, btnScheduledMessages;
    RecyclerView settingsFriendRecyclerView, settingsFriendRequestRecyclerView;
    UserAdpter friendAdapter;
    FriendRequestAdapter friendRequestAdapter;
    SettingsFriendAdapter settingsFriendAdapter;
    ArrayList<Users> friendList = new ArrayList<>();
    ArrayList<Users> friendRequestList = new ArrayList<>();
    ArrayList<Users> allUsersList = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseDatabase database;
    LinearLayout layoutFriendHeader, layoutFriendRequestHeader;
    TextView tvFriendHeader, tvFriendRequestHeader;
    ImageView ivArrowFriend, ivArrowFriendRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        settingsAvatar = findViewById(R.id.settingsAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnToggleTheme = findViewById(R.id.btnToggleTheme);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangeName = findViewById(R.id.btnChangeName);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnScheduledMessages = findViewById(R.id.btnScheduledMessages);
        settingsFriendRecyclerView = findViewById(R.id.settingsFriendRecyclerView);
        settingsFriendRequestRecyclerView = findViewById(R.id.settingsFriendRequestRecyclerView);
        layoutFriendHeader = findViewById(R.id.layoutFriendHeader);
        layoutFriendRequestHeader = findViewById(R.id.layoutFriendRequestHeader);
        tvFriendHeader = findViewById(R.id.tvFriendHeader);
        tvFriendRequestHeader = findViewById(R.id.tvFriendRequestHeader);
        ivArrowFriend = findViewById(R.id.ivArrowFriend);
        ivArrowFriendRequest = findViewById(R.id.ivArrowFriendRequest);

        settingsFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        settingsFriendRequestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        settingsFriendAdapter = new SettingsFriendAdapter(this, friendList);
        settingsFriendRecyclerView.setAdapter(settingsFriendAdapter);
        
        friendRequestAdapter = new FriendRequestAdapter(this, friendRequestList);
        settingsFriendRequestRecyclerView.setAdapter(friendRequestAdapter);
        
        settingsFriendRecyclerView.setVisibility(View.GONE);
        settingsFriendRequestRecyclerView.setVisibility(View.GONE);
        ivArrowFriend.setRotation(0);
        ivArrowFriendRequest.setRotation(0);

        settingsAvatar.setOnClickListener(v -> openGallery());
        btnToggleTheme.setOnClickListener(v -> toggleTheme());
        btnLogout.setOnClickListener(v -> logout());
        btnChangeName.setOnClickListener(v -> showChangeNameDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnScheduledMessages.setOnClickListener(v -> openScheduledMessages());

        layoutFriendHeader.setOnClickListener(new View.OnClickListener() {
            boolean expanded = false;
            @Override
            public void onClick(View v) {
                if (expanded) {
                    settingsFriendRecyclerView.setVisibility(View.GONE);
                    ivArrowFriend.setRotation(0);
                } else {
                    settingsFriendRecyclerView.setVisibility(View.VISIBLE);
                    ivArrowFriend.setRotation(180);
                }
                expanded = !expanded;
            }
        });

        layoutFriendRequestHeader.setOnClickListener(new View.OnClickListener() {
            boolean expanded = false;
            @Override
            public void onClick(View v) {
                if (expanded) {
                    settingsFriendRequestRecyclerView.setVisibility(View.GONE);
                    ivArrowFriendRequest.setRotation(0);
                } else {
                    settingsFriendRequestRecyclerView.setVisibility(View.VISIBLE);
                    ivArrowFriendRequest.setRotation(180);
                }
                expanded = !expanded;
            }
        });

        loadAllUsers();
        loadUserProfile();
        loadAvatar();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText || v instanceof TextInputEditText) {
                View outSide = findViewById(android.R.id.content);
                int[] outLocation = new int[2];
                outSide.getLocationOnScreen(outLocation);
                float x = ev.getX() + outLocation[0];
                float y = ev.getY() + outLocation[1];
                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void loadAllUsers() {
        DatabaseReference reference = database.getReference().child("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsersList.clear();
                String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (users != null && users.userId != null && !users.userId.equals(currentUid)) {
                        allUsersList.add(users);
                    }
                }
                loadFriends();
                loadFriendRequests();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_name, null);
        
        TextInputEditText etChangeName = dialogView.findViewById(R.id.etChangeName);
        Button btnCancelName = dialogView.findViewById(R.id.btnCancelName);
        Button btnSaveName = dialogView.findViewById(R.id.btnSaveName);
        
        etChangeName.setText(tvUserName.getText().toString());
        
        AlertDialog dialog = builder.setView(dialogView).create();
        
        btnCancelName.setOnClickListener(v -> dialog.dismiss());
        btnSaveName.setOnClickListener(v -> {
            String newName = etChangeName.getText().toString().trim();
            
            if (TextUtils.isEmpty(newName)) {
                etChangeName.setError(getString(R.string.name_required));
                return;
            }
            if (newName.length() < 2) {
                etChangeName.setError(getString(R.string.name_too_short));
                return;
            }
            
            String currentUid = auth.getCurrentUser().getUid();
            DatabaseReference userRef = database.getReference().child("user").child(currentUid);
            userRef.child("userName").setValue(newName).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.name_updated), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.name_update_error), Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etEmailDisplay = dialogView.findViewById(R.id.etEmailDisplay);
        Button btnCancelPassword = dialogView.findViewById(R.id.btnCancelPassword);
        Button btnSavePassword = dialogView.findViewById(R.id.btnSavePassword);
        
        etEmailDisplay.setText(tvUserEmail.getText().toString());
        
        AlertDialog dialog = builder.setView(dialogView).create();
        
        btnCancelPassword.setOnClickListener(v -> dialog.dismiss());
        btnSavePassword.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            boolean hasError = false;
            if (TextUtils.isEmpty(oldPassword)) {
                etOldPassword.setError(getString(R.string.old_password_required));
                hasError = true;
            }
            if (TextUtils.isEmpty(newPassword)) {
                etNewPassword.setError(getString(R.string.password_required));
                hasError = true;
            } else if (newPassword.length() < 6) {
                etNewPassword.setError(getString(R.string.password_too_short));
                hasError = true;
            }
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError(getString(R.string.password_not_match));
                hasError = true;
            }
            if (hasError) return;
            // Re-authenticate user with old password
            String email = etEmailDisplay.getText().toString().trim();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword);
            auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    auth.getCurrentUser().updatePassword(newPassword)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                etNewPassword.setError(getString(R.string.password_update_error));
                            }
                        });
                } else {
                    etOldPassword.setError(getString(R.string.old_password_wrong));
                }
            });
        });
        
        dialog.show();
    }

    private void loadUserProfile() {
        String currentUid = auth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference().child("user").child(currentUid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("userName").getValue(String.class);
                    String email = snapshot.child("mail").getValue(String.class);
                    
                    if (userName != null && !userName.isEmpty()) {
                        tvUserName.setText(userName);
                    } else {
                        tvUserName.setText(getString(R.string.no_name));
                    }
                    
                    if (email != null && !email.isEmpty()) {
                        tvUserEmail.setText(email);
                    } else {
                        tvUserEmail.setText(getString(R.string.no_email));
                    }
                } else {
                    // Nếu không tìm thấy user trong database
                    tvUserName.setText(getString(R.string.no_name));
                    tvUserEmail.setText(getString(R.string.no_email));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
                tvUserName.setText(getString(R.string.load_error));
                tvUserEmail.setText(getString(R.string.load_error));
            }
        });
    }

    private void loadAvatar() {
        String currentUid = auth.getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference().child("user").child(currentUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profilePic = snapshot.child("profilepic").getValue(String.class);
                if (profilePic != null && !profilePic.isEmpty()) {
                    if (ImageUtils.isBase64Image(profilePic)) {
                        settingsAvatar.setImageBitmap(ImageUtils.convertBase64ToBitmap(profilePic));
                    } else {
                        com.squareup.picasso.Picasso.get().load(profilePic).placeholder(R.drawable.photocamera).into(settingsAvatar);
                    }
                } else {
                    settingsAvatar.setImageResource(R.drawable.photocamera);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST) {
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                updateAvatar(imageUri);
            }
        }
    }

    private void updateAvatar(Uri imageUri) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUid = auth.getCurrentUser().getUid();
        String base64Image = ImageUtils.convertImageToBase64(this, imageUri);
        if (base64Image == null) {
            Toast.makeText(this, "Không thể xử lý ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference userRef = database.getReference().child("user").child(currentUid);
        userRef.child("profilepic").setValue(base64Image).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SettingsActivity.this, "Cập nhật avatar thành công!", Toast.LENGTH_SHORT).show();
                loadAvatar();
            } else {
                Toast.makeText(SettingsActivity.this, "Lỗi cập nhật avatar!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFriends() {
        String currentUid = auth.getCurrentUser().getUid();
        DatabaseReference friendsRef = database.getReference().child("friends").child(currentUid);
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendList.clear();
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendUid = friendSnapshot.getKey();
                    for (Users user : allUsersList) {
                        if (user.userId.equals(friendUid)) {
                            friendList.add(user);
                            break;
                        }
                    }
                }
                settingsFriendAdapter.filterList(new ArrayList<>(friendList));
                tvFriendHeader.setText("Danh sách bạn bè (" + friendList.size() + ")");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadFriendRequests() {
        String currentUid = auth.getCurrentUser().getUid();
        DatabaseReference friendReqRef = database.getReference().child("friendRequests").child(currentUid);
        friendReqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendRequestList.clear();
                for (DataSnapshot reqSnapshot : snapshot.getChildren()) {
                    String fromUid = reqSnapshot.getKey();
                    for (Users user : allUsersList) {
                        if (user.userId.equals(fromUid)) {
                            friendRequestList.add(user);
                            break;
                        }
                    }
                }
                friendRequestAdapter.filterList(new ArrayList<>(friendRequestList));
                tvFriendRequestHeader.setText("Lời mời kết bạn (" + friendRequestList.size() + ")");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(SettingsActivity.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_theme", false);
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        prefs.edit().putBoolean("dark_theme", !isDark).apply();
    }

    private void openScheduledMessages() {
        Intent intent = new Intent(SettingsActivity.this, ScheduledMessagesActivity.class);
        startActivity(intent);
    }
} 