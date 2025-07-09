package com.vishwajeeth.medicinetime.medicine;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.vishwajeeth.medicinetime.Injection;
import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.databinding.ActivityMedicineBinding;
import com.vishwajeeth.medicinetime.report.MonthlyReportActivity;
import com.vishwajeeth.medicinetime.utils.ActivityUtils;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.vishwajeeth.medicinetime.profile.ProfileManagerActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vishwajeeth.medicinetime.addmedicine.AddMedicineActivity;
import android.util.Log;
import com.vishwajeeth.medicinetime.data.source.local.MedicineDBHelper;
import com.vishwajeeth.medicinetime.data.source.Pills;
import com.vishwajeeth.medicinetime.data.source.local.UserProfile;

import java.util.List;

public class MedicineActivity extends AppCompatActivity {

    private ActivityMedicineBinding binding;
    private MedicinePresenter presenter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.compactcalendarView.setLocale(TimeZone.getDefault(), Locale.ENGLISH);
        binding.compactcalendarView.setShouldDrawDaysHeader(true);

        binding.compactcalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                setSubtitle(dateFormat.format(dateClicked));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateClicked);

                int day = calendar.get(Calendar.DAY_OF_WEEK);

                if (isExpanded) {
                    ViewCompat.animate(binding.datePickerArrow).rotation(0).start();
                } else {
                    ViewCompat.animate(binding.datePickerArrow).rotation(180).start();
                }
                isExpanded = !isExpanded;
                binding.appBarLayout.setExpanded(isExpanded, true);
                presenter.reload(day);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                setSubtitle(dateFormat.format(firstDayOfNewMonth));
            }
        });

        setCurrentDate(new Date());

        // Set up click listener for date picker button
        binding.datePickerButton.setOnClickListener(v -> {
            if (isExpanded) {
                ViewCompat.animate(binding.datePickerArrow).rotation(0).start();
            } else {
                ViewCompat.animate(binding.datePickerArrow).rotation(180).start();
            }
            isExpanded = !isExpanded;
            binding.appBarLayout.setExpanded(isExpanded, true);
        });

        MedicineFragment medicineFragment = (MedicineFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);
        if (medicineFragment == null) {
            medicineFragment = MedicineFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), medicineFragment, R.id.contentFrame);
        }

        // Create MedicinePresenter
        presenter = new MedicinePresenter(Injection.provideMedicineRepository(MedicineActivity.this), medicineFragment);
        int currentProfileId = getSharedPreferences("MedicineApp", MODE_PRIVATE).getInt("current_profile_id", -1);
        presenter.setUserProfileId(currentProfileId);

        // Debug: In ra thông tin database
        debugDatabaseInfo();

        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentProfileId = getSharedPreferences("MedicineApp", MODE_PRIVATE).getInt("current_profile_id",
                        -1);
                if (currentProfileId == -1) {
                    Toast.makeText(MedicineActivity.this, "Vui lòng tạo thành viên trước!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MedicineActivity.this,
                            com.vishwajeeth.medicinetime.profile.ProfileManagerActivity.class);
                    startActivity(intent);
                    return;
                } else {
                    Intent intent = new Intent(MedicineActivity.this, AddMedicineActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentProfileId = getSharedPreferences("MedicineApp", MODE_PRIVATE).getInt("current_profile_id", -1);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int day = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        android.util.Log.d("DEBUG_DB",
                "MedicineActivity.onResume: currentProfileId=" + currentProfileId + ", day=" + day);
        if (presenter != null) {
            presenter.setUserProfileId(currentProfileId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.medicine_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_stats) {
            Intent statsIntent = new Intent(this, MonthlyReportActivity.class);
            startActivity(statsIntent);
            return true;
        } else if (id == R.id.action_profile_manager) {
            Intent profileIntent = new Intent(this, ProfileManagerActivity.class);
            startActivity(profileIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setCurrentDate(Date date) {
        setSubtitle(dateFormat.format(date));
        binding.compactcalendarView.setCurrentDate(date);
    }

    public void setSubtitle(String subtitle) {
        binding.datePickerTextView.setText(subtitle);
    }

    private void debugDatabaseInfo() {
        try {
            MedicineDBHelper dbHelper = new MedicineDBHelper(this);

            // In ra danh sách user profiles
            List<UserProfile> profiles = dbHelper.getAllUserProfiles();
            Log.d("DEBUG_DB", "=== USER PROFILES ===");
            for (UserProfile profile : profiles) {
                Log.d("DEBUG_DB", "Profile ID: " + profile.id + ", Name: " + profile.name);
            }

            // In ra danh sách pills
            List<Pills> pills = dbHelper.getAllPills();
            Log.d("DEBUG_DB", "=== PILLS ===");
            for (Pills pill : pills) {
                Log.d("DEBUG_DB", "Pill ID: " + pill.getPillId() + ", Name: " + pill.getPillName() + ", Profile ID: "
                        + pill.userProfileId);
            }

            // In ra current profile ID
            int currentProfileId = getSharedPreferences("MedicineApp", MODE_PRIVATE).getInt("current_profile_id", -1);
            Log.d("DEBUG_DB", "Current Profile ID: " + currentProfileId);

        } catch (Exception e) {
            Log.e("DEBUG_DB", "Error debugging database: " + e.getMessage());
        }
    }
}
