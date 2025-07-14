package com.example.messengerprm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class CalculatorActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "HiddenChatPrefs";
    private static final String PIN_KEY = "user_pin";
    private static final String DEFAULT_PIN = "1234";
    private ArrayList<String> historyList = new ArrayList<>();
    private LinearLayout historyLayout;
    private ScrollView historyScrollView;
    private boolean isResultShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        // Kết quả hiện tại
        TextView tvResult = findViewById(R.id.tvResult);
        historyLayout = findViewById(R.id.historyLayout);
        historyScrollView = findViewById(R.id.historyScrollView);

        // Khởi tạo nút
        int[] numBtnIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i < numBtnIds.length; i++) {
            int finalI = i;
            findViewById(numBtnIds[i]).setOnClickListener(v -> appendToResult(tvResult, String.valueOf(finalI)));
        }
        findViewById(R.id.btnDot).setOnClickListener(v -> appendDot(tvResult));
        findViewById(R.id.btnPlus).setOnClickListener(v -> appendToResult(tvResult, "+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> appendToResult(tvResult, "-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> appendToResult(tvResult, "×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> appendToResult(tvResult, "÷"));
        findViewById(R.id.btnPercent).setOnClickListener(v -> appendToResult(tvResult, "%"));
        findViewById(R.id.btnAC).setOnClickListener(v -> tvResult.setText("0"));
        findViewById(R.id.btnDel).setOnClickListener(v -> deleteLastChar(tvResult));
        findViewById(R.id.btnEqual).setOnClickListener(v -> calculateAndAddHistory(tvResult));
        findViewById(R.id.btnConvert).setOnClickListener(v -> Toast.makeText(this, "Chức năng chuyển đổi chưa hỗ trợ", Toast.LENGTH_SHORT).show());
    }

    private void appendToResult(TextView tv, String s) {
        String current = tv.getText().toString();
        if (isResultShown) {
            // Nếu vừa bấm '=', nếu bấm số thì reset, nếu bấm phép toán thì nối tiếp
            if (s.matches("[0-9]")) {
                current = "";
            }
            isResultShown = false;
        }
        if (current.equals("0") && !s.equals(".")) current = "";
        tv.setText(current + s);
    }

    private void appendDot(TextView tv) {
        String current = tv.getText().toString();
        if (!current.endsWith(",") && !current.contains(",")) {
            tv.setText(current + ",");
        }
    }

    private void deleteLastChar(TextView tv) {
        String current = tv.getText().toString();
        if (current.length() > 1) {
            tv.setText(current.substring(0, current.length() - 1));
        } else {
            tv.setText("0");
        }
    }

    private void calculateAndAddHistory(TextView tv) {
        String expr = tv.getText().toString();
        if (expr.isEmpty()) return;
        // Đọc mã PIN mới nhất từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String pin = prefs.getString(PIN_KEY, DEFAULT_PIN);
        if (expr.equals(pin)) {
            Intent i = new Intent(this, splash.class);
            startActivity(i);
            finish();
            return;
        } else if (expr.equals("9999")) {
            Intent i = new Intent(this, ChangePinActivity.class);
            startActivity(i);
            return;
        }
        String exprForEval = expr.replace('×', '*').replace('÷', '/').replace(',', '.');
        double result = 0;
        try {
            result = eval(exprForEval);
            String resultStr = (result == (long) result) ? String.format("%d", (long) result) : String.valueOf(result);
            String historyItem = expr + " = " + resultStr.replace('.', ',');
            historyList.add(0, historyItem);
            updateHistoryView();
            tv.setText(resultStr.replace('.', ','));
            isResultShown = true;
        } catch (Exception e) {
            tv.setText("Lỗi");
            isResultShown = true;
        }
    }

    private void updateHistoryView() {
        historyLayout.removeAllViews();
        for (String item : historyList) {
            TextView tv = new TextView(this);
            tv.setText(item);
            tv.setTextSize(16f);
            tv.setTextColor(0xFFAAAAAA);
            tv.setGravity(android.view.Gravity.END);
            historyLayout.addView(tv);
        }
        historyScrollView.post(() -> historyScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    // Hàm eval đơn giản (chỉ hỗ trợ + - × ÷ %)
    private double eval(String expr) {
        expr = expr.replace('×', '*').replace('÷', '/').replace(',', '.');
        // Chỉ hỗ trợ 2 toán hạng và 1 phép toán
        expr = expr.replaceAll("[^-0-9+*/%.]", "");
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+");
            if (parts.length == 2) return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
        } else if (expr.contains("-")) {
            String[] parts = expr.split("-");
            if (parts.length == 2) return Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
        } else if (expr.contains("*")) {
            String[] parts = expr.split("\\*");
            if (parts.length == 2) return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
        } else if (expr.contains("/")) {
            String[] parts = expr.split("/");
            if (parts.length == 2) return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        } else if (expr.contains("%")) {
            String[] parts = expr.split("%");
            if (parts.length == 2) return Double.parseDouble(parts[0]) % Double.parseDouble(parts[1]);
        }
        return Double.parseDouble(expr);
    }

    @Override
    public void onBackPressed() {
        // Không làm gì để ngăn quay lại màn chat
        moveTaskToBack(true);
    }
} 