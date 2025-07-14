package com.example.messengerprm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import com.example.messengerprm.Users;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ImageView btnSettings;

    private FirebaseDatabase database;
    private ArrayList<Users> usersArrayList;
    private ArrayList<Users> friendsList;
    private UserAdpter userAdapter;
    private RecyclerView userRecyclerView;
    private TextView tvNoFriends;
    private LinearLayout searchLayout;
    private boolean isSearching = false;

    private static final int GALLERY_REQUEST = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        DatabaseReference reference = database.getReference().child("user");

        usersArrayList = new ArrayList<>();
        friendsList = new ArrayList<>();

        userRecyclerView = findViewById(R.id.userRecyclerView);
        tvNoFriends = findViewById(R.id.tvNoFriends);
        searchLayout = findViewById(R.id.searchLayout);

        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdpter(this, friendsList);
        userRecyclerView.setAdapter(userAdapter);

        EditText searchUser = findViewById(R.id.searchUser);
        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    isSearching = false;
                    showFriends();
                } else {
                    isSearching = true;
                    filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                String currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (users != null && users.userId != null && !users.userId.equals(currentUid)) {
                        usersArrayList.add(users);
                    }
                }
                loadFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnSettings = findViewById(R.id.btnSettings);

        btnSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;
            private boolean isDoubleTap = false;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < 500) {
                    Log.d("MainActivity", "Double tap detected - testing fake message");
                    isDoubleTap = true;
                    lastClickTime = 0;
                } else {
                    Log.d("MainActivity", "Single tap detected - waiting for potential double tap");
                    lastClickTime = currentTime;

                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isDoubleTap) {
                                Log.d("MainActivity", "Single tap confirmed - opening settings");
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                            }
                            isDoubleTap = false;
                        }
                    }, 300);
                }
            }
        });

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
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

    private void loadFriends() {
        if (auth.getCurrentUser() == null) return;

        String currentUid = auth.getCurrentUser().getUid();
        DatabaseReference friendsRef = database.getReference().child("friends").child(currentUid);

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsList.clear();
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendUid = friendSnapshot.getKey();
                    for (Users user : usersArrayList) {
                        if (user.userId.equals(friendUid)) {
                            friendsList.add(user);
                            break;
                        }
                    }
                }
                updateUnreadCountsForFriends();
                setupMessageListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupMessageListeners() {
        if (auth.getCurrentUser() == null) return;

        String currentUid = auth.getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        for (Users friend : friendsList) {
            String roomId;
            if (currentUid.compareTo(friend.userId) < 0) {
                roomId = currentUid + friend.userId;
            } else {
                roomId = friend.userId + currentUid;
            }

            DatabaseReference messagesRef = db.getReference().child("chats").child(roomId).child("messages");

            messagesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    updateUnreadCountForFriend(friend);

                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        DataSnapshot lastMessage = null;
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            lastMessage = messageSnapshot;
                        }

                        if (lastMessage != null) {
                            String message = lastMessage.child("message").getValue(String.class);
                            String sender = lastMessage.child("senderId").getValue(String.class);
                            if (sender == null)
                                sender = lastMessage.child("senderid").getValue(String.class);
                            String messageType = lastMessage.child("type").getValue(String.class);
                            if (messageType == null)
                                messageType = lastMessage.child("messageType").getValue(String.class);

                            if (message != null && sender != null) {
                                boolean isFromCurrentUser = sender.equals(currentUid);
                                String displayMessage = "image".equals(messageType) ? "[Hình ảnh]" : message;

                                if (userAdapter != null) {
                                    userAdapter.updateLastMessage(friend.userId, displayMessage, isFromCurrentUser);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void updateUnreadCountForFriend(Users friend) {
        if (auth.getCurrentUser() == null) return;
        String currentUid = auth.getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        String roomId;
        if (currentUid.compareTo(friend.userId) < 0) {
            roomId = currentUid + friend.userId;
        } else {
            roomId = friend.userId + currentUid;
        }

        Log.d("UnreadCount", "RoomId for " + friend.userName + ": " + roomId + " (currentUid: " + currentUid + ", friendId: " + friend.userId + ")");

        DatabaseReference lastReadRef = db.getReference().child("chats").child(roomId).child("lastReadTimestamp").child(currentUid);
        DatabaseReference messagesRef = db.getReference().child("chats").child(roomId).child("messages");

        lastReadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot lastReadSnap) {
                long lastRead = 0;
                if (lastReadSnap.exists()) {
                    try {
                        lastRead = lastReadSnap.getValue(Long.class);
                        Log.d("UnreadCount", "LastRead for " + friend.userName + ": " + lastRead);
                    } catch (Exception e) {
                        lastRead = 0;
                        Log.d("UnreadCount", "LastRead error for " + friend.userName + ": " + e.getMessage());
                    }
                } else {
                    Log.d("UnreadCount", "No LastRead for " + friend.userName + ", using 0");
                }
                final long finalLastRead = lastRead;

                messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot messagesSnap) {
                        int unread = 0;
                        Log.d("UnreadCount", "Checking messages for " + friend.userName + ", total messages: " + messagesSnap.getChildrenCount());

                        for (DataSnapshot msgSnap : messagesSnap.getChildren()) {
                            Long ts = msgSnap.child("timestamp").getValue(Long.class);
                            if (ts == null)
                                ts = msgSnap.child("timeStamp").getValue(Long.class);
                            String senderId = msgSnap.child("senderId").getValue(String.class);
                            if (senderId == null)
                                senderId = msgSnap.child("senderid").getValue(String.class);

                            Log.d("UnreadCount", "Message: ts=" + ts + ", senderId=" + senderId + ", lastRead=" + finalLastRead + ", currentUid=" + currentUid);

                            if (ts != null && ts > finalLastRead && senderId != null && !senderId.equals(currentUid)) {
                                unread++;
                                Log.d("UnreadCount", "Found unread message: ts=" + ts + ", lastRead=" + finalLastRead + ", sender=" + senderId + ", currentUid=" + currentUid);
                            }
                        }

                        friend.setUnreadCount(unread);
                        Log.d("UnreadCount", "Final unread count for " + friend.userName + ": " + unread);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UnreadCount", "Error getting messages for " + friend.userName + ": " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UnreadCount", "Error getting lastRead for " + friend.userName + ": " + error.getMessage());
            }
        });
    }

    private void updateUnreadCountsForFriends() {
        if (auth.getCurrentUser() == null) return;
        String currentUid = auth.getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        Log.d("MainActivity", "updateUnreadCountsForFriends called with " + friendsList.size() + " friends");

        final int[] processedCount = {0};
        final int totalFriends = friendsList.size();

        if (totalFriends == 0) {
            Log.d("MainActivity", "No friends to process");
            showFriends();
            return;
        }

        for (Users friend : friendsList) {
            String roomId;
            if (currentUid.compareTo(friend.userId) < 0) {
                roomId = currentUid + friend.userId;
            } else {
                roomId = friend.userId + currentUid;
            }

            Log.d("UnreadCount", "RoomId for " + friend.userName + ": " + roomId + " (currentUid: " + currentUid + ", friendId: " + friend.userId + ")");

            DatabaseReference lastReadRef = db.getReference().child("chats").child(roomId).child("lastReadTimestamp").child(currentUid);
            DatabaseReference messagesRef = db.getReference().child("chats").child(roomId).child("messages");

            Log.d("MainActivity", "Processing friend: " + friend.userName + ", roomId: " + roomId);

            lastReadRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot lastReadSnap) {
                    long lastRead = 0;
                    if (lastReadSnap.exists()) {
                        try {
                            lastRead = lastReadSnap.getValue(Long.class);
                            Log.d("UnreadCount", "LastRead for " + friend.userName + ": " + lastRead);
                        } catch (Exception e) {
                            lastRead = 0;
                            Log.d("UnreadCount", "LastRead error for " + friend.userName + ": " + e.getMessage());
                        }
                    } else {
                        Log.d("UnreadCount", "No LastRead for " + friend.userName + ", using 0");
                    }
                    final long finalLastRead = lastRead;

                    messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot messagesSnap) {
                            int unread = 0;
                            Log.d("UnreadCount", "Checking messages for " + friend.userName + ", total messages: " + messagesSnap.getChildrenCount());

                            for (DataSnapshot msgSnap : messagesSnap.getChildren()) {
                                Long ts = msgSnap.child("timestamp").getValue(Long.class);
                                if (ts == null)
                                    ts = msgSnap.child("timeStamp").getValue(Long.class);
                                String senderId = msgSnap.child("senderId").getValue(String.class);
                                if (senderId == null)
                                    senderId = msgSnap.child("senderid").getValue(String.class);

                                Log.d("UnreadCount", "Message: ts=" + ts + ", senderId=" + senderId + ", lastRead=" + finalLastRead + ", currentUid=" + currentUid);

                                if (ts != null && ts > finalLastRead && senderId != null && !senderId.equals(currentUid)) {
                                    unread++;
                                    Log.d("UnreadCount", "Found unread message: ts=" + ts + ", lastRead=" + finalLastRead + ", sender=" + senderId);
                                }
                            }

                            friend.setUnreadCount(unread);
                            Log.d("UnreadCount", "Final unread count for " + friend.userName + ": " + unread);

                            processedCount[0]++;
                            Log.d("UnreadCount", "Processed " + processedCount[0] + "/" + totalFriends + " friends");

                            if (processedCount[0] >= totalFriends) {
                                Log.d("UnreadCount", "All friends processed, updating adapter");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        userAdapter.notifyDataSetChanged();
                                        showFriends();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("UnreadCount", "Error getting messages for " + friend.userName + ": " + error.getMessage());
                            processedCount[0]++;
                            if (processedCount[0] >= totalFriends) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        userAdapter.notifyDataSetChanged();
                                        showFriends();
                                    }
                                });
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UnreadCount", "Error getting lastRead for " + friend.userName + ": " + error.getMessage());
                    processedCount[0]++;
                    if (processedCount[0] >= totalFriends) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userAdapter.notifyDataSetChanged();
                                showFriends();
                            }
                        });
                    }
                }
            });
        }
    }

    private void showFriends() {
        Log.d("MainActivity", "showFriends called, friendsList size: " + friendsList.size());
        if (friendsList.isEmpty()) {
            userRecyclerView.setVisibility(View.GONE);
            tvNoFriends.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.VISIBLE);
        } else {
            userRecyclerView.setVisibility(View.VISIBLE);
            tvNoFriends.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
            userAdapter.setSearchMode(false);
            Log.d("MainActivity", "Creating filtered list with " + friendsList.size() + " friends");
            for (Users friend : friendsList) {
                Log.d("MainActivity", "Friend: " + friend.userName + ", UnreadCount: " + friend.getUnreadCount());
            }
            userAdapter.filterList(new ArrayList<>(friendsList));
            Log.d("MainActivity", "Filtered friends list with " + friendsList.size() + " friends");
        }
    }

    private void filter(String text) {
        ArrayList<Users> filteredList = new ArrayList<>();

        for (Users user : usersArrayList) {
            if (user.userName != null && user.userName.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            } else if (user.mail != null && user.mail.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }

        userRecyclerView.setVisibility(View.VISIBLE);
        tvNoFriends.setVisibility(View.GONE);
        userAdapter.setSearchMode(true);
        userAdapter.filterList(filteredList);
    }

    public boolean isSearching() {
        return isSearching;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                Toast.makeText(MainActivity.this, "Cập nhật avatar thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Lỗi cập nhật avatar!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            loadFriends();
        }
    }
}


    
