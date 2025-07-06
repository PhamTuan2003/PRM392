package com.vishwajeeth.medicinetime.medicine;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.views.RobotoBoldTextView;
import com.vishwajeeth.medicinetime.views.RobotoRegularTextView;

import java.util.List;

/**
 * Medicine Adapter
 */

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<MedicineAlarm> medicineAlarmList;
    private OnItemClickListener onItemClickListener;

    public MedicineAdapter(List<MedicineAlarm> medicineAlarmList) {
        this.medicineAlarmList = medicineAlarmList;
    }

    public void replaceData(List<MedicineAlarm> medicineAlarmList) {
        this.medicineAlarmList = medicineAlarmList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        final MedicineAlarm medicineAlarm = medicineAlarmList.get(position);
        if (medicineAlarm == null) {
            return;
        }
        holder.tvMedTime.setText(medicineAlarm.getStringTime());
        holder.tvMedicineName.setText(medicineAlarm.getPillName());
        holder.tvDoseDetails.setText(medicineAlarm.getFormattedDose());
        holder.ivAlarmDelete.setVisibility(View.VISIBLE);
        holder.ivAlarmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onMedicineDeleteClicked(medicineAlarm);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (medicineAlarmList != null && !medicineAlarmList.isEmpty()) ? medicineAlarmList.size() : 0;
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {

        RobotoBoldTextView tvMedTime;
        RobotoBoldTextView tvMedicineName;
        RobotoRegularTextView tvDoseDetails;
        ImageView ivMedicineAction;
        ImageView ivAlarmDelete;

        MedicineViewHolder(View itemView) {
            super(itemView);
            tvMedTime = itemView.findViewById(R.id.tv_med_time);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvDoseDetails = itemView.findViewById(R.id.tv_dose_details);
            ivMedicineAction = itemView.findViewById(R.id.iv_medicine_action);
            ivAlarmDelete = itemView.findViewById(R.id.iv_alarm_delete);
        }
    }

    interface OnItemClickListener {
        void onMedicineDeleteClicked(MedicineAlarm medicineAlarm);
    }
}
