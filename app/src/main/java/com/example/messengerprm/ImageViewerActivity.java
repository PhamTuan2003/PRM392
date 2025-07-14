package com.example.messengerprm;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {
    
    private ZoomableImageView imageView;
    private String imageData; // Có thể là Base64 hoặc URL
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        
        imageView = findViewById(R.id.fullscreen_image);
//        ImageView closeButton = findViewById(R.id.close_button);
        
        // Lấy dữ liệu ảnh từ Intent
        imageData = getIntent().getStringExtra("image_data");
        String messageType = getIntent().getStringExtra("message_type");
        
        if (imageData != null && !imageData.isEmpty()) {
            // Kiểm tra xem có phải Base64 không
            if (ImageUtils.isBase64Image(imageData)) {
                // Load từ Base64
                android.graphics.Bitmap bitmap = ImageUtils.convertBase64ToBitmap(imageData);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(this, "Không thể hiển thị ảnh", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Load từ URL
                Picasso.get().load(imageData)
                        .placeholder(R.drawable.photocamera)
                        .error(R.drawable.photocamera)
                        .into(imageView);
            }
        } else {
            Toast.makeText(this, "Không có dữ liệu ảnh", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        // Click listener cho nút đóng
//        closeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        
        // Click listener cho ảnh
        imageView.setOnImageClickListener(new ZoomableImageView.OnImageClickListener() {
            @Override
            public void onImageClick() {
                finish();
            }
        });
    }
} 