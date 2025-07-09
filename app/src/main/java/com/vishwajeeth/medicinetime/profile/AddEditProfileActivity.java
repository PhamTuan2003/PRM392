package com.vishwajeeth.medicinetime.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.local.MedicineDBHelper;
import com.vishwajeeth.medicinetime.data.source.local.UserProfile;

public class AddEditProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private EditText etProfileAge;
    private Spinner spinnerRelation;
    private MedicineDBHelper dbHelper;
    private int profileId = -1; // -1 = thêm mới, > 0 = sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_profile);

        dbHelper = new MedicineDBHelper(this);

        // Thiết lập toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo views
        etProfileName = findViewById(R.id.et_profile_name);
        etProfileAge = findViewById(R.id.et_profile_age);
        spinnerRelation = findViewById(R.id.spinner_relation);

        Button btnSave = findViewById(R.id.btn_save_profile);
        btnSave.setOnClickListener(v -> saveProfile());

        // Kiểm tra có phải đang sửa profile không
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            profileId = extras.getInt("profile_id", -1);
            if (profileId > 0) {
                // Đang sửa profile
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Sửa thành viên");
                }

                // Load dữ liệu cũ
                etProfileName.setText(extras.getString("profile_name", ""));
                Integer age = extras.getInt("profile_age", -1);
                if (age > 0) {
                    etProfileAge.setText(String.valueOf(age));
                }

                String relation = extras.getString("profile_relation", "");
                // Set spinner relation (cần implement)
            } else {
                // Thêm mới profile
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Thêm thành viên");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        String name = etProfileName.getText().toString().trim();
        String ageStr = etProfileAge.getText().toString().trim();
        String relation = spinnerRelation.getSelectedItem().toString();

        // Validate
        if (TextUtils.isEmpty(name)) {
            etProfileName.setError("Vui lòng nhập tên");
            return;
        }

        Integer age = null;
        if (!TextUtils.isEmpty(ageStr)) {
            try {
                age = Integer.parseInt(ageStr);
                if (age <= 0 || age > 150) {
                    etProfileAge.setError("Tuổi không hợp lệ");
                    return;
                }
            } catch (NumberFormatException e) {
                etProfileAge.setError("Tuổi không hợp lệ");
                return;
            }
        }

        // Lưu vào database
        if (profileId > 0) {
            // Cập nhật profile
            UserProfile profile = new UserProfile();
            profile.id = profileId;
            profile.name = name;
            profile.age = age;
            profile.relation = relation;
            profile.avatarUri = ""; // Có thể thêm sau

            int result = dbHelper.updateUserProfile(profile);
            if (result > 0) {
                Toast.makeText(this, "Đã cập nhật thành viên", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Thêm mới profile
            long result = dbHelper.insertUserProfile(name, age, relation, "");
            if (result > 0) {
                // Lưu ID thành viên mới vào SharedPreferences
                getSharedPreferences("MedicineApp", MODE_PRIVATE)
                        .edit()
                        .putInt("current_profile_id", (int) result)
                        .putString("current_profile_name", name)
                        .apply();
                Toast.makeText(this, "Đã thêm thành viên mới", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi thêm thành viên", Toast.LENGTH_SHORT).show();
            }
        }
    }
}