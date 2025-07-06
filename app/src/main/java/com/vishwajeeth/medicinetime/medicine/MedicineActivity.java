package com.vishwajeeth.medicinetime.medicine;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

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
        
        MedicineFragment medicineFragment = (MedicineFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (medicineFragment == null) {
            medicineFragment = MedicineFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), medicineFragment, R.id.contentFrame);
        }

        //Create MedicinePresenter
        presenter = new MedicinePresenter(Injection.provideMedicineRepository(MedicineActivity.this), medicineFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.medicine_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_stats) {
            Intent intent = new Intent(this, MonthlyReportActivity.class);
            startActivity(intent);
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
}
