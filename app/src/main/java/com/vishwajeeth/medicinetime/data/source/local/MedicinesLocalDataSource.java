package com.vishwajeeth.medicinetime.data.source.local;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.data.source.MedicineDataSource;
import com.vishwajeeth.medicinetime.data.source.Pills;
import com.vishwajeeth.medicinetime.data.source.local.UserProfile;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Medicines Local Data Source
 */

public class MedicinesLocalDataSource implements MedicineDataSource {

    private static MedicinesLocalDataSource mInstance;

    private MedicineDBHelper mDbHelper;

    private Context context;

    private MedicinesLocalDataSource(Context context) {
        mDbHelper = new MedicineDBHelper(context);
        this.context = context;
    }

    public static MedicinesLocalDataSource getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MedicinesLocalDataSource(context);
        }
        return mInstance;
    }

    @Override
    public void getMedicineHistory(LoadHistoryCallbacks loadHistoryCallbacks) {
        List<History> historyList = mDbHelper.getHistory();
        loadHistoryCallbacks.onHistoryLoaded(historyList);
    }

    @Override
    public void getMedicineAlarmById(long id, GetTaskCallback callback) {

        try {
            MedicineAlarm medicineAlarm = getAlarmById(id);
            if (medicineAlarm != null) {
                callback.onTaskLoaded(medicineAlarm);
            } else {
                callback.onDataNotAvailable();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            callback.onDataNotAvailable();
        }

    }

    @Override
    public void saveMedicine(MedicineAlarm medicineAlarm, Pills pill) {
        android.util.Log.d("DEBUG_DB", "MedicinesLocalDataSource.saveMedicine: pill=" + pill.getPillName()
                + ", userProfileId=" + pill.userProfileId);
        long pillId = mDbHelper.createPill(pill); // Lưu pill mới và lấy id
        pill.setPillId(pillId);
        long[] alarmIds = mDbHelper.createAlarm(medicineAlarm, pillId);

        // Đặt báo thức cho từng alarmId
        for (int i = 0; i < alarmIds.length; i++) {
            if (alarmIds[i] == 0)
                continue;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, medicineAlarm.getHour());
            calendar.set(Calendar.MINUTE, medicineAlarm.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            // Nếu thời gian đã qua thì đặt cho ngày hôm sau
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            // Lấy tên thành viên từ userProfileId
            String memberName = "";
            List<UserProfile> profiles = mDbHelper.getAllUserProfiles();
            for (UserProfile profile : profiles) {
                if (profile.id == pill.userProfileId) {
                    memberName = profile.name;
                    break;
                }
            }
            Intent intent = new Intent(context, com.vishwajeeth.medicinetime.alarm.AlarmReceiver.class);
            intent.putExtra("pillName", pill.getPillName());
            intent.putExtra("userProfileId", pill.userProfileId);
            intent.putExtra("memberName", memberName);
            intent.putExtra("time", medicineAlarm.getHour() + ":" + medicineAlarm.getMinute());
            intent.putExtra("alarmId", (int) alarmIds[i]);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) alarmIds[i], intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void getMedicineListByDay(int day, LoadMedicineCallbacks callbacks) {
        List<MedicineAlarm> medicineAlarmList = mDbHelper.getAlarmsByDay(day);
        callbacks.onMedicineLoaded(medicineAlarmList);
    }

    @Override
    public boolean medicineExits(String pillName) {
        for (Pills pill : getPills()) {
            if (pill.getPillName().equals(pillName))
                return true;
        }
        return false;
    }

    @Override
    public List<Long> tempIds() {
        return null;
    }

    @Override
    public void deleteAlarm(long alarmId) {
        deleteAlarmById(alarmId);
    }

    @Override
    public List<MedicineAlarm> getMedicineByPillName(String pillName) {
        try {
            return getMedicineByPill(pillName);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<MedicineAlarm> getAllAlarms(String pillName) {
        try {
            return getAllAlarmsByName(pillName);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Pills getPillsByName(String pillName) {
        return getPillByName(pillName);
    }

    @Override
    public long savePills(Pills pills) {
        return savePill(pills);
    }

    @Override
    public void saveToHistory(History history) {
        mDbHelper.createHistory(history);
    }

    private List<Pills> getPills() {
        return mDbHelper.getAllPills();
    }

    private long savePill(Pills pill) {
        long pillId = mDbHelper.createPill(pill);
        pill.setPillId(pillId);
        return pillId;
    }

    private Pills getPillByName(String pillName) {
        return mDbHelper.getPillByName(pillName);
    }

    private List<MedicineAlarm> getMedicineByPill(String pillName) throws URISyntaxException {
        return mDbHelper.getAllAlarmsByPill(pillName);
    }

    private List<MedicineAlarm> getAllAlarmsByName(String pillName) throws URISyntaxException {
        return mDbHelper.getAllAlarms(pillName);
    }

    public void deletePill(String pillName) throws URISyntaxException {
        mDbHelper.deletePill(pillName);
    }

    private void deleteAlarmById(long alarmId) {
        mDbHelper.deleteAlarm(alarmId);
    }

    public void addToHistory(History h) {
        mDbHelper.createHistory(h);
    }

    public List<History> getHistory() {
        return mDbHelper.getHistory();
    }

    private MedicineAlarm getAlarmById(long alarm_id) throws URISyntaxException {
        return mDbHelper.getAlarmById(alarm_id);
    }

    public int getDayOfWeek(long alarm_id) throws URISyntaxException {
        return mDbHelper.getDayOfWeek(alarm_id);
    }

    public List<Pills> getPillsByProfile(int userProfileId) {
        return mDbHelper.getPillsByProfile(userProfileId);
    }

    public void getMedicineListByDayAndProfile(int day, int userProfileId, LoadMedicineCallbacks callbacks) {
        List<MedicineAlarm> medicineAlarmList = mDbHelper.getAlarmsByDayAndProfile(day, userProfileId);
        callbacks.onMedicineLoaded(medicineAlarmList);
    }

}
