package com.vishwajeeth.medicinetime.addmedicine;

import androidx.annotation.NonNull;
import android.util.Log;

import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.Pills;

import java.util.List;

/**
 * Add Medicine Presenter
 */

public class AddMedicinePresenter implements AddMedicineContract.Presenter, MedicineDataSource.GetTaskCallback {

    @NonNull
    private final MedicineDataSource mMedicineRepository;

    private final AddMedicineContract.View mAddMedicineView;

    private int mMedicineId;

    private boolean mIsDataMissing;

    public AddMedicinePresenter(int mMedicineId, @NonNull MedicineDataSource mMedicineRepository,
            AddMedicineContract.View mAddMedicineView, boolean mIsDataMissing) {
        this.mMedicineId = mMedicineId;
        this.mMedicineRepository = mMedicineRepository;
        this.mAddMedicineView = mAddMedicineView;
        this.mIsDataMissing = mIsDataMissing;

        mAddMedicineView.setPresenter(this);
    }

    private boolean isNewTask() {
        return mMedicineId <= 0;
    }

    @Override
    public void start() {

    }

    @Override
    public void saveMedicine(MedicineAlarm alarm, Pills pills) {
        Log.d("AddMedicinePresenter",
                "Saving medicine: " + alarm.getPillName() + ", ProfileId: " + pills.userProfileId);
        mMedicineRepository.saveMedicine(alarm, pills);
        mAddMedicineView.showMedicineList();
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    @Override
    public boolean isMedicineExits(String pillName) {
        return mMedicineRepository.medicineExits(pillName);
    }

    @Override
    public long addPills(Pills pills) {
        return mMedicineRepository.savePills(pills);
    }

    @Override
    public Pills getPillsByName(String pillName) {
        return mMedicineRepository.getPillsByName(pillName);
    }

    @Override
    public List<MedicineAlarm> getMedicineByPillName(String pillName) {
        return mMedicineRepository.getMedicineByPillName(pillName);
    }

    @Override
    public List<Long> tempIds() {
        return mMedicineRepository.tempIds();
    }

    @Override
    public void deleteMedicineAlarm(long alarmId) {
        mMedicineRepository.deleteAlarm(alarmId);
    }

    @Override
    public void onTaskLoaded(MedicineAlarm medicineAlarm) {
        // The view may not be able to handle UI updates anymore
        /*
         * if (mAddMedicineView.isActive()){
         * mAddMedicineView.setDose(medicineAlarm.getDose());
         * mAddMedicineView.setMedName(medicineAlarm.getMedicineName());
         * mAddMedicineView.setDays(medicineAlarm.getDays());
         * mAddMedicineView.setTime(medicineAlarm.getTime());
         * }
         * mIsDataMissing = false;
         */
    }

    @Override
    public void onDataNotAvailable() {
        if (mAddMedicineView.isActive()) {
            mAddMedicineView.showEmptyMedicineError();
        }
    }
}
