package com.vishwajeeth.medicinetime.data.source;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Medicine Alarm Data Model
 */
public class MedicineAlarm implements Comparable<MedicineAlarm> {

    private long id; // DB id number

    private long pillId;

    private String pillName;

    private int hour; //

    private int minute;

    private boolean[] dayOfWeek = new boolean[7];

    private String doseQuantity;

    private String doseUnit;

    private String dateString;

    private List<Long> ids = new LinkedList<Long>();

    private boolean isTaken;

    private int alarmId;

    public MedicineAlarm() {

    }

    public MedicineAlarm(long id, long pillId, String pillName, int hour, int minute, boolean[] dayOfWeek,
            String doseQuantity, String doseUnit, String dateString, List<Long> ids, boolean isTaken, int alarmId) {
        this.id = id;
        this.pillId = pillId;
        this.pillName = pillName;
        this.hour = hour;
        this.minute = minute;
        this.dayOfWeek = dayOfWeek;
        this.doseQuantity = doseQuantity;
        this.doseUnit = doseUnit;
        this.dateString = dateString;
        this.ids = ids;
        this.isTaken = isTaken;
        this.alarmId = alarmId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPillId() {
        return pillId;
    }

    public void setPillId(long pillId) {
        this.pillId = pillId;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean[] getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(boolean[] dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDoseQuantity() {
        return doseQuantity;
    }

    public void setDoseQuantity(String doseQuantity) {
        this.doseQuantity = doseQuantity;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public List<Long> getIds() {
        return Collections.unmodifiableList(ids);
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isTaken() {
        return isTaken;
    }

    public void setTaken(boolean taken) {
        isTaken = taken;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public void addId(long id) {
        ids.add(id);
    }

    private String getAm_pm() {
        return (hour < 12) ? "am" : "pm";
    }

    /**
     * Overrides the compareTo() method so that alarms can be sorted by time of day
     * from earliest to
     * latest.
     */
    @Override
    public int compareTo(@NonNull MedicineAlarm medicineAlarm) {
        if (hour < medicineAlarm.getHour())
            return -1;
        else if (hour > medicineAlarm.getHour())
            return 1;
        else {
            if (minute < medicineAlarm.getMinute())
                return -1;
            else if (minute > medicineAlarm.getMinute())
                return 1;
            else
                return 0;
        }
    }

    /**
     * A helper method which returns the time of the alarm in string form
     * hour:minutes am/pm
     */
    public String getStringTime() {
        int nonMilitaryHour = hour % 12;
        if (nonMilitaryHour == 0)
            nonMilitaryHour = 12;
        String min = Integer.toString(minute);
        if (minute < 10)
            min = "0" + minute;
        return String.format(Locale.getDefault(), "%d:%s %s", nonMilitaryHour, min, getAm_pm());
    }

    /**
     * A helper method which returns the formatted medicine dose
     * doseQuantity doseUnit
     */
    public String getFormattedDose() {
        return String.format(Locale.getDefault(), "%s %s", doseQuantity, doseUnit);
    }

}
