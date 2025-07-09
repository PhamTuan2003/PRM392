package com.vishwajeeth.medicinetime.addmedicine;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.Pills;
import com.vishwajeeth.medicinetime.databinding.FragmentAddMedicineBinding;

import java.util.Arrays;
import java.util.List;
import android.util.Log;

/**
 * Add Medicine Fragment
 */

public class AddMedicineFragment extends Fragment implements AddMedicineContract.View {

    public static final String ARGUMENT_EDIT_MEDICINE_ID = "ARGUMENT_EDIT_MEDICINE_ID";

    public static final String ARGUMENT_EDIT_MEDICINE_NAME = "ARGUMENT_EDIT_MEDICINE_NAME";

    private FragmentAddMedicineBinding binding;
    private List<String> doseUnitList;
    private boolean[] dayOfWeekList = new boolean[7];
    private int hour, minute;
    private AddMedicineContract.Presenter mPresenter;
    private String doseUnit;

    static AddMedicineFragment newInstance() {
        Bundle args = new Bundle();
        AddMedicineFragment fragment = new AddMedicineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Không cần FloatingActionButton nữa
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddMedicineBinding.inflate(inflater, container, false);
        setupViews();
        setCurrentTime();
        setSpinnerDoseUnits();
        binding.btnSaveMedicine.setOnClickListener(v -> saveMedicine());
        return binding.getRoot();
    }

    private void setupViews() {
        // Setup click listeners for checkboxes
        binding.everyDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Arrays.fill(dayOfWeekList, true);
                binding.dvMonday.setChecked(true);
                binding.dvTuesday.setChecked(true);
                binding.dvWednesday.setChecked(true);
                binding.dvThursday.setChecked(true);
                binding.dvFriday.setChecked(true);
                binding.dvSaturday.setChecked(true);
                binding.dvSunday.setChecked(true);
            } else {
                Arrays.fill(dayOfWeekList, false);
                binding.dvMonday.setChecked(false);
                binding.dvTuesday.setChecked(false);
                binding.dvWednesday.setChecked(false);
                binding.dvThursday.setChecked(false);
                binding.dvFriday.setChecked(false);
                binding.dvSaturday.setChecked(false);
                binding.dvSunday.setChecked(false);
            }
        });

        binding.dvMonday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dayOfWeekList[1] = isChecked;
            if (!isChecked)
                binding.everyDay.setChecked(false);
        });

        binding.dvTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dayOfWeekList[2] = isChecked;
            if (!isChecked)
                binding.everyDay.setChecked(false);
        });

        binding.dvWednesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dayOfWeekList[3] = isChecked;
            if (!isChecked)
                binding.everyDay.setChecked(false);
        });

        binding.dvThursday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dayOfWeekList[4] = isChecked;
            if (!isChecked)
                binding.everyDay.setChecked(false);
        });

        binding.dvFriday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dayOfWeekList[5] = isChecked;
            if (!isChecked)
                binding.everyDay.setChecked(false);
        });

        binding.dvSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dayOfWeekList[6] = isChecked;
            if (!isChecked)
                binding.everyDay.setChecked(false);
        });

        binding.dvSunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dayOfWeekList[0] = isChecked;
            if (!isChecked)
                binding.everyDay.setChecked(false);
        });

        // Setup time picker click listener
        binding.tvMedicineTime.setOnClickListener(v -> onMedicineTimeClick());

        // Setup spinner listener
        binding.spinnerDoseUnits.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                doseUnit = doseUnitList.get(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                doseUnit = doseUnitList.get(0);
            }
        });
    }

    @Override
    public void setPresenter(AddMedicineContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showEmptyMedicineError() {
        // Snackbar.make(mTitle, getString(R.string.empty_task_message),
        // Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMedicineList() {
        requireActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void onMedicineTimeClick() {
        showTimePicker();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        hour = selectedHour;
                        minute = selectedMinute;
                        setCurrentTime();
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void setCurrentTime() {
        String timeSet = "";
        if (hour > 12) {
            hour -= 12;
            timeSet = "PM";
        } else if (hour == 0) {
            hour += 12;
            timeSet = "AM";
        } else if (hour == 12) {
            timeSet = "PM";
        } else {
            timeSet = "AM";
        }

        String minutes = "";
        if (minute < 10) {
            minutes = "0" + minute;
        } else {
            minutes = String.valueOf(minute);
        }

        String aTime = String.valueOf(hour) + ":" + minutes + " " + timeSet;
        binding.tvMedicineTime.setText(aTime);
    }

    private void setSpinnerDoseUnits() {
        doseUnitList = Arrays.asList("mg", "mcg", "g", "ml", "drops", "puffs", "units", "tablets", "capsules");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, doseUnitList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDoseUnits.setAdapter(adapter);
    }

    private void saveMedicine() {
        String medicineName = binding.editMedName.getText().toString();
        String doseQuantity = binding.tvDoseQuantity.getText().toString();

        if (medicineName.isEmpty()) {
            showEmptyMedicineError();
            return;
        }

        if (doseQuantity.isEmpty()) {
            doseQuantity = "1";
        }

        boolean[] days = dayOfWeekList.clone();

        // Lấy userProfileId đang chọn
        int userProfileId = requireActivity().getSharedPreferences("MedicineApp", android.content.Context.MODE_PRIVATE)
                .getInt("current_profile_id", -1);
        Log.d("DEBUG_ADD_MED", "UserProfileId: " + userProfileId);
        if (userProfileId == -1) {
            android.widget.Toast.makeText(getContext(), "Vui lòng chọn thành viên trước khi thêm thuốc!",
                    android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        // Tạo Pills bằng constructor rỗng
        Pills pills = new Pills();
        pills.setPillName(medicineName);
        // Gắn userProfileId vào pills (nếu Pills có trường này, hoặc truyền vào DB khi
        // lưu)
        // pills.setUserProfileId(userProfileId); // Nếu có setter
        pills.userProfileId = userProfileId; // Nếu public field
        Log.d("DEBUG_ADD_MED", "Created Pills: " + pills.getPillName() + ", ProfileId: " + pills.userProfileId);

        // Tạo MedicineAlarm và set các thuộc tính
        MedicineAlarm alarm = new MedicineAlarm();
        alarm.setPillName(medicineName);
        alarm.setDoseQuantity(doseQuantity);
        alarm.setDoseUnit(doseUnit);
        alarm.setDayOfWeek(days);
        alarm.setHour(hour);
        alarm.setMinute(minute);
        // Có thể set thêm các thuộc tính khác nếu cần

        // Thêm alarm vào pills
        pills.addAlarm(alarm);

        mPresenter.saveMedicine(alarm, pills);
    }
}
