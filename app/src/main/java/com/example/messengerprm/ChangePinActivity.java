package com.example.messengerprm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePinActivity extends AppCompatActivity {
    private EditText oldPin, newPin, confirmPin;
    private Button btnSave, btnReset;
    private static final String PREFS_NAME = "HiddenChatPrefs";
    private static final String PIN_KEY = "user_pin";
    private static final String DEFAULT_PIN = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);
        oldPin = findViewById(R.id.oldPin);
        newPin = findViewById(R.id.newPin);
        confirmPin = findViewById(R.id.confirmPin);
        btnSave = findViewById(R.id.btnSave);
        btnReset = findViewById(R.id.btnReset);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleChangePin();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResetPin();
            }
        });
    }

    private void handleChangePin() {
        String oldPinStr = oldPin.getText().toString();
        String newPinStr = newPin.getText().toString();
        String confirmPinStr = confirmPin.getText().toString();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentPin = prefs.getString(PIN_KEY, DEFAULT_PIN);
        if (!oldPinStr.equals(currentPin)) {
            Toast.makeText(this, "Sai mã PIN cũ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPinStr) || newPinStr.length() != 4) {
            Toast.makeText(this, "Mã PIN mới phải gồm 4 số!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPinStr.equals(confirmPinStr)) {
            Toast.makeText(this, "Xác nhận mã PIN không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().putString(PIN_KEY, newPinStr).apply();
        Toast.makeText(this, "Đổi mã PIN thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleResetPin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PIN_KEY, DEFAULT_PIN).apply();
        Toast.makeText(this, "Đã reset mã PIN về mặc định (1234)", Toast.LENGTH_SHORT).show();
        finish();
    }
} 