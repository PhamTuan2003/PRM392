

package com.example.messengerprm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatWin extends AppCompatActivity {
    String reciverimg, reciverUid,reciverName,SenderUID;
    String roomId; // Phòng chat chung
    CircleImageView profile;
    TextView reciverNName, lastMessage, lastMessageTime;

    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public  static String senderImg;
    public  static String reciverIImg;
    CardView sendbtn, cameraBtn, galleryBtn, scheduleBtn;
    EditText textmsg;

    String senderRoom,reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;
    LinearLayoutManager linearLayoutManager;
    
    // Biến để theo dõi việc load tin nhắn
    private boolean isLoadingMessages = false;
    private String lastMessageKey = null;
    private static final int MESSAGE_LIMIT = 20; // Số tin nhắn load mỗi lần
    
    // Constants for image handling
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;
    private static final int CAMERA_REQUEST = 102;
    private static final int GALLERY_REQUEST = 103;
    
    private String currentPhotoPath;
    private Uri photoURI;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        // Debug logging
        Log.d("ChatWin", "Receiver Name: " + reciverName);
        Log.d("ChatWin", "Receiver Image: " + (reciverimg != null ? reciverimg.substring(0, Math.min(50, reciverimg.length())) + "..." : "null"));
        Log.d("ChatWin", "Receiver UID: " + reciverUid);

        messagesArrayList = new ArrayList<>();

        sendbtn = findViewById(R.id.sendbtnn);
        cameraBtn = findViewById(R.id.camera_btn);
        galleryBtn = findViewById(R.id.gallery_btn);
        scheduleBtn = findViewById(R.id.schedule_btn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        lastMessage = findViewById(R.id.lastMessage);
        lastMessageTime = findViewById(R.id.lastMessageTime);

        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);
        messageAdpter.setLayoutManager(linearLayoutManager);
        
        // Khởi tạo adapter với placeholder images (sẽ được cập nhật sau)
        mmessagesAdpter = new messagesAdpter(this, messagesArrayList, "", "");
        messageAdpter.setAdapter(mmessagesAdpter);
        
        // Thêm scroll listener để load tin nhắn cũ khi kéo lên và cập nhật lastReadTimestamp
        messageAdpter.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Kiểm tra nếu đã scroll lên đầu và có thể load thêm
                if (!linearLayoutManager.canScrollVertically()) {
                    loadOlderMessages();
                }
                
                // Kiểm tra nếu scroll xuống cuối (đọc tin nhắn mới)
                if (linearLayoutManager.findLastVisibleItemPosition() >= messagesArrayList.size() - 1) {
                    // Người dùng đã scroll xuống cuối, cập nhật lastReadTimestamp
                    updateLastReadTimestamp();
                }
            }
        });


        // Load profile image - handle both Base64 and URL
        if (reciverimg != null && !reciverimg.isEmpty()) {
            Log.d("ChatWin", "Loading image, length: " + reciverimg.length());
            // Debug: Log the image data type
            if (reciverimg.length() > 100) {
                // Likely Base64
                Log.d("ChatWin", "Attempting to load as Base64");
                if (ImageUtils.isBase64Image(reciverimg)) {
                    // Load Base64 image
                    Log.d("ChatWin", "Valid Base64, converting to bitmap");
                    Bitmap bitmap = ImageUtils.convertBase64ToBitmap(reciverimg);
                    if (bitmap != null) {
                        Log.d("ChatWin", "Bitmap created successfully");
                        profile.setImageBitmap(bitmap);
                    } else {
                        Log.d("ChatWin", "Failed to create bitmap, using default");
                        // Fallback to default image
                        profile.setImageResource(R.drawable.photocamera);
                    }
                } else {
                    Log.d("ChatWin", "Invalid Base64, trying as URL");
                    // Invalid Base64, try as URL
                    Picasso.get().load(reciverimg)
                        .placeholder(R.drawable.photocamera)
                        .error(R.drawable.photocamera)
                        .into(profile);
                }
            } else {
                // Likely URL
                Log.d("ChatWin", "Loading as URL");
                Picasso.get().load(reciverimg)
                    .placeholder(R.drawable.photocamera)
                    .error(R.drawable.photocamera)
                    .into(profile);
            }
        } else {
            Log.d("ChatWin", "No image data, using default");
            // Set default image if no image data
            profile.setImageResource(R.drawable.photocamera);
        }

        reciverNName.setText(""+reciverName);

        SenderUID =  firebaseAuth.getUid();

        // Tạo roomId chung cho 2 user
        if (SenderUID.compareTo(reciverUid) < 0) {
            roomId = SenderUID + reciverUid;
        } else {
            roomId = reciverUid + SenderUID;
        }
        
        Log.d("ChatWin", "Room ID: " + roomId + ", SenderUID: " + SenderUID + ", ReceiverUID: " + reciverUid);
        
        // Hiển thị tin nhắn gần nhất và thời gian sau khi roomId đã được khởi tạo
        updateLastMessageDisplay();

        // --- FRIEND CHECK ---
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("friends").child(SenderUID).child(reciverUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Không phải bạn bè, không cho nhắn tin
                    Toast.makeText(chatWin.this, "Chỉ bạn bè mới nhắn tin được!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatreference = database.getReference().child("chats").child(roomId).child("messages");

            // Không cập nhật lastReadTimestamp khi vào chat, chỉ cập nhật khi thực sự đọc tin nhắn
        
        // Load tin nhắn ban đầu và listener cho tin nhắn mới
        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Load tất cả tin nhắn từ Firebase
                messagesArrayList.clear();
                Log.d("ChatWin", "Loading messages from roomId: " + roomId);
                Log.d("ChatWin", "Total messages in Firebase: " + snapshot.getChildrenCount());
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    if (messages != null) {
                        // Lưu message key để có thể xóa chính xác
                        messages.setMessageKey(dataSnapshot.getKey());
                        messagesArrayList.add(messages);
                        Log.d("ChatWin", "Loaded message: " + messages.getMessage() + ", sender: " + messages.getSenderId() + ", timestamp: " + messages.getTimestamp() + ", key: " + dataSnapshot.getKey());
                    } else {
                        // Nếu không load được, có thể do tên trường cũ, thử migrate
                        migrateOldMessage(dataSnapshot);
                    }
                }
                
                Log.d("ChatWin", "Total messages loaded: " + messagesArrayList.size());
                
                // Sắp xếp tin nhắn theo thời gian (cũ nhất lên đầu, mới nhất xuống cuối)
                messagesArrayList.sort(new Comparator<msgModelclass>() {
                    @Override
                    public int compare(msgModelclass o1, msgModelclass o2) {
                        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                    }
                });
                
                if (mmessagesAdpter != null) {
                    mmessagesAdpter.notifyDataSetChanged();
                    // Scroll xuống cuối để hiển thị tin nhắn mới nhất
                    scrollToBottom();
                }
                
                // Đã xóa updateLastMessageDisplay() vì không còn hiển thị last message trong chat window
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatWin", "Error loading messages: " + error.getMessage());
            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderImg= snapshot.child("profilepic").getValue().toString();
                reciverIImg=reciverimg;
                
                // Khởi tạo adapter sau khi có dữ liệu từ Firebase
                if (mmessagesAdpter == null) {
                    mmessagesAdpter = new messagesAdpter(chatWin.this, messagesArrayList, senderImg, reciverIImg);
                    messageAdpter.setAdapter(mmessagesAdpter);
                } else {
                    // Cập nhật adapter nếu đã tồn tại
                    mmessagesAdpter.updateImages(senderImg, reciverIImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Camera button click
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                }
            }
        });

        // Gallery button click
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_REQUEST);
                }
            }
        });

        // Schedule button click
        scheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showScheduleMessageDialog();
            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString();
                if (message.isEmpty()){
                    Toast.makeText(chatWin.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText("");
                
                // Tạo tin nhắn mới với timestamp hiện tại
                long currentTime = System.currentTimeMillis();
                msgModelclass newMessage = new msgModelclass();
                newMessage.setMessage(message);
                newMessage.setSenderId(SenderUID);
                newMessage.setType("text");
                newMessage.setTimestamp(currentTime);
                
                // Thêm vào UI ngay lập tức
                messagesArrayList.add(newMessage);
                mmessagesAdpter.notifyDataSetChanged();
                scrollToBottom();
                
                // Cập nhật hiển thị tin nhắn gần nhất
                updateLastMessageDisplay();
                
                // Lưu vào roomId duy nhất với tên trường đúng
                Log.d("ChatWin", "Sending message to room: " + roomId + ", message: " + message);
                DatabaseReference msgRef = database.getReference().child("chats").child(roomId).child("messages").push();
                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("message", message);
                msgMap.put("senderId", SenderUID); // Đúng tên trường
                msgMap.put("type", "text"); // Đúng tên trường
                msgMap.put("timestamp", currentTime); // Đúng tên trường
                msgRef.setValue(msgMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("ChatWin", "Message sent successfully to room: " + roomId);
                        } else {
                            Log.e("ChatWin", "Failed to send message: " + task.getException());
                        }
                    }
                });
            }
        });



    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, "com.example.messengerprm.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                // Handle camera result
                if (photoURI != null) {
                    uploadImageToFirebase(photoURI, "camera");
                }
            } else if (requestCode == GALLERY_REQUEST) {
                // Handle gallery result
                if (data != null && data.getData() != null) {
                    uploadImageToFirebase(data.getData(), "gallery");
                }
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String source) {
        if (imageUri == null) {
            Toast.makeText(this, "Không tìm thấy file ảnh!", Toast.LENGTH_SHORT).show();
            Log.e("UploadDebug", "imageUri is null!");
            return;
        }

        Log.d("UploadDebug", "imageUri: " + imageUri);
        Log.d("UploadDebug", "source: " + source);
        Log.d("UploadDebug", "roomId: " + roomId);

        Toast.makeText(this, "Processing image...", Toast.LENGTH_SHORT).show();

        // Chuyển ảnh thành Base64 để lưu vào Realtime Database
        try {
            String base64Image = ImageUtils.convertImageToBase64(this, imageUri);
            if (base64Image == null) {
                Toast.makeText(this, "Không thể xử lý ảnh!", Toast.LENGTH_SHORT).show();
                Log.e("UploadDebug", "Cannot convert image to Base64");
                return;
            }

            Log.d("UploadDebug", "Image converted to Base64 successfully");
            Log.d("UploadDebug", "Base64 length: " + base64Image.length());

            // Tạo tin nhắn ảnh mới với timestamp hiện tại
            long currentTime = System.currentTimeMillis();
            msgModelclass newImageMessage = new msgModelclass();
            newImageMessage.setMessage("");
            newImageMessage.setSenderId(SenderUID);
            newImageMessage.setImageUrl(base64Image);
            newImageMessage.setType("image");
            newImageMessage.setTimestamp(currentTime);
            
            // Thêm vào UI ngay lập tức
            messagesArrayList.add(newImageMessage);
            mmessagesAdpter.notifyDataSetChanged();
            scrollToBottom();
            
            // Cập nhật hiển thị tin nhắn gần nhất
            updateLastMessageDisplay();

            // Sử dụng timestamp hiện tại thay vì ServerValue.TIMESTAMP
            Log.d("ChatWin", "Sending image to room: " + roomId + ", image length: " + base64Image.length());
            DatabaseReference msgRef = database.getReference().child("chats").child(roomId).child("messages").push();
            java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
            msgMap.put("message", "");
            msgMap.put("senderId", SenderUID); // Đúng tên trường
            msgMap.put("imageUrl", base64Image);
            msgMap.put("type", "image"); // Đúng tên trường
            msgMap.put("timestamp", currentTime); // Đúng tên trường
            msgRef.setValue(msgMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(chatWin.this, "Image sent successfully", Toast.LENGTH_SHORT).show();
                        Log.d("UploadDebug", "Image saved to Realtime Database successfully");
                        // Không cần scrollToBottom() ở đây, UI đã được cập nhật
                    } else {
                        Toast.makeText(chatWin.this, "Failed to send image to receiver", Toast.LENGTH_SHORT).show();
                        Log.e("UploadDebug", "Failed to save to room: " + task.getException());
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("UploadDebug", "Error processing image: " + e.getMessage());
            Log.e("UploadDebug", "Exception type: " + e.getClass().getSimpleName());
            Log.e("UploadDebug", "Full exception: ", e);
        }
    }
    
    // Method để scroll xuống tin nhắn cuối cùng
    private void scrollToBottom() {
        if (messagesArrayList.size() > 0) {
            messageAdpter.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Scroll xuống tin nhắn cuối cùng
                    int lastPosition = messagesArrayList.size() - 1;
                    messageAdpter.scrollToPosition(lastPosition);
                    
                    // Đảm bảo tin nhắn cuối hiển thị đầy đủ
                    linearLayoutManager.scrollToPositionWithOffset(lastPosition, 0);
                    
                    // KHÔNG cập nhật lastReadTimestamp ở đây nữa
                    // Chỉ cập nhật khi thoát khỏi chat hoặc khi thực sự đọc tin nhắn
                }
            }, 150); // Tăng delay để đảm bảo bàn phím đã hiển thị
        }
    }
    
    // Load tin nhắn mới nhất ban đầu
    private String firstMessageKey = null;
    private void loadLatestMessages() {
        DatabaseReference chatreference = database.getReference().child("chats").child(roomId).child("messages");
        chatreference.orderByKey().limitToLast(MESSAGE_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                boolean isFirst = true;
                boolean hasUnreadMessages = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (isFirst) {
                        firstMessageKey = dataSnapshot.getKey();
                        isFirst = false;
                    }
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    if (messages != null) {
                        messagesArrayList.add(messages);
                        // Đánh dấu tin nhắn đã đọc nếu không phải do mình gửi
                        if (messages != null && messages.getSenderId() != null && !messages.getSenderId().equals(SenderUID)) {
                            hasUnreadMessages = true;
                        }
                    }
                }
                
                // Không cần cập nhật lastReadTimestamp ở đây vì đã được gọi từ onCreate()
                
                // Sắp xếp tin nhắn theo thời gian
                messagesArrayList.sort(new Comparator<msgModelclass>() {
                    @Override
                    public int compare(msgModelclass o1, msgModelclass o2) {
                        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                    }
                });
                if (mmessagesAdpter != null) {
                    mmessagesAdpter.notifyDataSetChanged();
                    scrollToBottom();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    
    // Load tin nhắn cũ hơn khi kéo lên
    private void loadOlderMessages() {
        if (isLoadingMessages || messagesArrayList.isEmpty() || firstMessageKey == null) {
            return;
        }
        isLoadingMessages = true;
        DatabaseReference chatreference = database.getReference().child("chats").child(roomId).child("messages");
        chatreference.orderByKey().endBefore(firstMessageKey).limitToLast(MESSAGE_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<msgModelclass> olderMessages = new ArrayList<>();
                String newFirstKey = null;
                boolean isFirst = true;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (isFirst) {
                        newFirstKey = dataSnapshot.getKey();
                        isFirst = false;
                    }
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    if (messages != null) {
                        olderMessages.add(messages);
                    }
                }
                if (!olderMessages.isEmpty()) {
                    messagesArrayList.addAll(0, olderMessages);
                    firstMessageKey = newFirstKey;
                    if (mmessagesAdpter != null) {
                        mmessagesAdpter.notifyDataSetChanged();
                        messageAdpter.scrollToPosition(olderMessages.size());
                    }
                }
                isLoadingMessages = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoadingMessages = false;
            }
        });
    }
    
    // Cập nhật lastReadTimestamp khi thoát khỏi phòng chat
    @Override
    protected void onPause() {
        super.onPause();
        try {
            updateLastReadTimestamp();
        } catch (Exception e) {
            Log.e("ChatWin", "Error in onPause: " + e.getMessage(), e);
        }
    }
    
    // Cập nhật lastReadTimestamp khi nhấn nút back
    @Override
    public void onBackPressed() {
        updateLastReadTimestamp();
        super.onBackPressed();
    }
    
    // Method để migrate tin nhắn cũ sang format mới
    private void migrateOldMessage(DataSnapshot dataSnapshot) {
        try {
            // Lấy dữ liệu với tên trường cũ
            String message = dataSnapshot.child("message").getValue(String.class);
            String senderId = dataSnapshot.child("senderid").getValue(String.class);
            Long timestamp = dataSnapshot.child("timeStamp").getValue(Long.class);
            String type = dataSnapshot.child("messageType").getValue(String.class);
            String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
            
            if (message != null && senderId != null && timestamp != null) {
                // Tạo tin nhắn mới với tên trường đúng
                msgModelclass newMessage = new msgModelclass();
                newMessage.setMessage(message);
                newMessage.setSenderId(senderId);
                newMessage.setTimestamp(timestamp);
                newMessage.setType(type != null ? type : "text");
                if (imageUrl != null) {
                    newMessage.setImageUrl(imageUrl);
                }
                
                // Thêm vào UI
                messagesArrayList.add(newMessage);
                Log.d("ChatWin", "Migrated old message: " + message + ", sender: " + senderId + ", timestamp: " + timestamp);
                
                // Cập nhật lại vào Firebase với tên trường đúng
                DatabaseReference msgRef = dataSnapshot.getRef();
                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("message", message);
                msgMap.put("senderId", senderId);
                msgMap.put("timestamp", timestamp);
                msgMap.put("type", type != null ? type : "text");
                if (imageUrl != null) {
                    msgMap.put("imageUrl", imageUrl);
                }
                msgRef.setValue(msgMap);
                
                Log.d("ChatWin", "Updated old message in Firebase with correct field names");
            }
        } catch (Exception e) {
            Log.e("ChatWin", "Error migrating old message: " + e.getMessage());
        }
    }
    
    // Method để cập nhật lastReadTimestamp
    private void updateLastReadTimestamp() {
        if (SenderUID != null && roomId != null) {
            long currentTime = System.currentTimeMillis();
            DatabaseReference lastReadRef = database.getReference()
                .child("chats").child(roomId)
                .child("lastReadTimestamp").child(SenderUID);
            lastReadRef.setValue(currentTime);
            Log.d("ChatWin", "Updated lastReadTimestamp for room " + roomId + ": " + currentTime);
        } else {
            Log.e("ChatWin", "Cannot update lastReadTimestamp: SenderUID=" + SenderUID + ", roomId=" + roomId);
        }
    }
    
    

    // Method để hiển thị tin nhắn gần nhất trong header
    public void updateLastMessageDisplay() {
        // Kiểm tra roomId có null không
        if (roomId == null) {
            Log.e("ChatWin", "roomId is null, cannot update last message display");
            return;
        }
        
        // Lấy tin nhắn gần nhất từ Firebase
        database.getReference().child("chats").child(roomId).child("messages")
                .orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                String message = messageSnapshot.child("message").getValue(String.class);
                                String sender = messageSnapshot.child("senderId").getValue(String.class);
                                if (sender == null) sender = messageSnapshot.child("senderid").getValue(String.class); // Fallback
                                String messageType = messageSnapshot.child("type").getValue(String.class);
                                if (messageType == null) messageType = messageSnapshot.child("messageType").getValue(String.class); // Fallback
                                Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                                if (timestamp == null) timestamp = messageSnapshot.child("timeStamp").getValue(Long.class); // Fallback
                                
                                if (message != null && sender != null) {
                                    // Hiển thị tin nhắn
                                    if (sender.equals(SenderUID)) {
                                        if ("image".equals(messageType)) {
                                            lastMessage.setText("Bạn: [Hình ảnh]");
                                        } else {
                                            lastMessage.setText("Bạn: " + message);
                                        }
                                    } else {
                                        if ("image".equals(messageType)) {
                                            lastMessage.setText("[Hình ảnh]");
                                        } else {
                                            lastMessage.setText(message);
                                        }
                                    }
                                    lastMessage.setVisibility(View.VISIBLE);
                                    
                                    // Hiển thị thời gian
                                    if (timestamp != null) {
                                        String timeText = formatTime(timestamp);
                                        lastMessageTime.setText(timeText);
                                        lastMessageTime.setVisibility(View.VISIBLE);
                                    } else {
                                        lastMessageTime.setVisibility(View.GONE);
                                    }
                                } else {
                                    lastMessage.setVisibility(View.GONE);
                                    lastMessageTime.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            lastMessage.setVisibility(View.GONE);
                            lastMessageTime.setVisibility(View.GONE);
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        lastMessage.setVisibility(View.GONE);
                        lastMessageTime.setVisibility(View.GONE);
                    }
                });
    }
    
    // Method để format thời gian
    private String formatTime(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;
        
        if (diff < 60000) { // Dưới 1 phút
            return "Vừa xong";
        } else if (diff < 3600000) { // Dưới 1 giờ
            long minutes = diff / 60000;
            return minutes + " phút trước";
        } else if (diff < 86400000) { // Dưới 1 ngày
            long hours = diff / 3600000;
            return hours + " giờ trước";
        } else {
            long days = diff / 86400000;
            return days + " ngày trước";
        }
    }

    // Method để xóa tin nhắn cũ (debug only)
    private void clearOldMessages() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear Old Messages")
               .setMessage("Do you want to clear all old messages with wrong field names?")
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       DatabaseReference messagesRef = database.getReference().child("chats").child(roomId).child("messages");
                       messagesRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()) {
                                   Toast.makeText(chatWin.this, "Old messages cleared", Toast.LENGTH_SHORT).show();
                                   messagesArrayList.clear();
                                   mmessagesAdpter.notifyDataSetChanged();
                               } else {
                                   Toast.makeText(chatWin.this, "Failed to clear messages", Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                   }
               })
               .setNegativeButton("No", null)
               .show();
    }

    // Method để hiển thị dialog lên lịch tin nhắn
    private void showScheduleMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_schedule_message, null);
        builder.setView(dialogView);

        EditText etMessage = dialogView.findViewById(R.id.et_scheduled_message);
        Button btnDate = dialogView.findViewById(R.id.btn_date);
        Button btnTime = dialogView.findViewById(R.id.btn_time);
        TextView tvScheduledTime = dialogView.findViewById(R.id.tv_scheduled_time);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSchedule = dialogView.findViewById(R.id.btn_schedule);

        final Calendar selectedDateTime = Calendar.getInstance();
        selectedDateTime.add(Calendar.MINUTE, 1); // Default to 1 minute from now

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Update time display
        updateTimeDisplay(tvScheduledTime, selectedDateTime);

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(selectedDateTime, tvScheduledTime);
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(selectedDateTime, tvScheduledTime);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String message = etMessage.getText().toString().trim();
                    if (message.isEmpty()) {
                        Toast.makeText(chatWin.this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long scheduledTime = selectedDateTime.getTimeInMillis();
                    long currentTime = System.currentTimeMillis();
                    
                    if (scheduledTime <= currentTime) {
                        Toast.makeText(chatWin.this, "Thời gian phải lớn hơn thời gian hiện tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate roomId
                    if (roomId == null || roomId.isEmpty()) {
                        Toast.makeText(chatWin.this, "Lỗi: Không tìm thấy phòng chat", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate sender UID
                    if (SenderUID == null || SenderUID.isEmpty()) {
                        Toast.makeText(chatWin.this, "Lỗi: Không xác định được người gửi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("ChatWin", "Scheduling message with roomId: " + roomId + ", senderUID: " + SenderUID);
                    
                    scheduleMessage(message, scheduledTime);
                    dialog.dismiss();
                    Toast.makeText(chatWin.this, "Đã lên lịch tin nhắn", Toast.LENGTH_SHORT).show();
                    
                } catch (Exception e) {
                    Log.e("ChatWin", "Error in schedule button click: " + e.getMessage(), e);
                    Toast.makeText(chatWin.this, "Lỗi khi lên lịch: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Method để hiển thị DatePicker
    private void showDatePicker(final Calendar calendar, final TextView tvTime) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateTimeDisplay(tvTime, calendar);
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Method để hiển thị TimePicker
    private void showTimePicker(final Calendar calendar, final TextView tvTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    updateTimeDisplay(tvTime, calendar);
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    // Method để cập nhật hiển thị thời gian
    private void updateTimeDisplay(TextView tvTime, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvTime.setText(sdf.format(calendar.getTime()));
    }

    // Method để lên lịch tin nhắn
    private void scheduleMessage(String message, long scheduledTime) {
        try {
            // Check exact alarm permission first
            if (!checkExactAlarmPermission()) {
                return;
            }
            
            Log.d("ChatWin", "Scheduling message: " + message + " at " + scheduledTime);
            
            ScheduledMessage scheduledMessage = new ScheduledMessage(
                message,
                SenderUID,
                reciverUid,
                roomId,
                "text",
                scheduledTime
            );

            // Chỉ gọi service để lưu và lên lịch, không tự push vào Firebase nữa
            MessageSchedulerService.scheduleMessage(chatWin.this, scheduledMessage);
            Toast.makeText(chatWin.this, "Đã lên lịch tin nhắn", Toast.LENGTH_SHORT).show();
            
            Log.d("ChatWin", "Message scheduling completed successfully");
            
        } catch (Exception e) {
            Log.e("ChatWin", "Error scheduling message: " + e.getMessage(), e);
            Toast.makeText(chatWin.this, "Lỗi khi lên lịch tin nhắn: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    // Method để kiểm tra và yêu cầu quyền exact alarm
    private boolean checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Show dialog to guide user to settings
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Quyền cần thiết")
                       .setMessage("Để lên lịch tin nhắn chính xác, ứng dụng cần quyền 'Lên lịch báo thức chính xác'. Vui lòng vào Cài đặt để cấp quyền.")
                       .setPositiveButton("Cài đặt", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                               startActivity(intent);
                           }
                       })
                       .setNegativeButton("Hủy", null)
                       .show();
                return false;
            }
        }
        return true;
    }
}