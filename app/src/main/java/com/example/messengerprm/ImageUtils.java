package com.example.messengerprm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {
    
    public static String convertImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            Bitmap compressedBitmap = compressImage(bitmap);
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap convertBase64ToBitmap(String base64String) {
        try {
            byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap compressImage(Bitmap bitmap) {
        int maxSize = 512;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width > height) {
            if (width > maxSize) {
                height = (height * maxSize) / width;
                width = maxSize;
            }
        } else {
            if (height > maxSize) {
                width = (width * maxSize) / height;
                height = maxSize;
            }
        }
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    
    public static boolean isBase64Image(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return false;
        }
        
        if (imageData.startsWith("http") || imageData.startsWith("data:image")) {
            return false;
        }
        
        try {
            Base64.decode(imageData, Base64.DEFAULT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 