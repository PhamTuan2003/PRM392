package com.vishwajeeth.medicinetime.report;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.data.source.History;
import com.vishwajeeth.medicinetime.views.RobotoBoldTextView;
import com.vishwajeeth.medicinetime.views.RobotoRegularTextView;

import java.util.List;

/**
 * History Adapter
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<History> mHistoryList;

    HistoryAdapter(List<History> historyList) {
        setList(historyList);
    }

    void replaceData(List<History> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<History> historyList) {
        this.mHistoryList = historyList;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        History history = mHistoryList.get(position);
        if (history == null) {
            return;
        }
        holder.tvMedDate.setText(history.getFormattedDate());
        setMedicineAction(holder, history.getAction());
        holder.tvMedicineName.setText(history.getPillName());
        holder.tvDoseDetails.setText(history.getFormattedDose());

    }

    private void setMedicineAction(HistoryViewHolder holder, int action) {
        switch (action) {
            case 0:
            default:
                holder.ivMedicineAction.setVisibility(View.GONE);
                break;
            case 1:
                holder.ivMedicineAction.setVisibility(View.VISIBLE);
                holder.ivMedicineAction.setImageResource(R.drawable.image_reminder_taken);
                break;
            case 2:
                holder.ivMedicineAction.setImageResource(R.drawable.image_reminder_not_taken);
                holder.ivMedicineAction.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return (mHistoryList != null && !mHistoryList.isEmpty()) ? mHistoryList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        RobotoBoldTextView tvMedDate;
        RobotoBoldTextView tvMedicineName;
        RobotoRegularTextView tvDoseDetails;
        ImageView ivMedicineAction;

        HistoryViewHolder(View itemView) {
            super(itemView);
            tvMedDate = itemView.findViewById(R.id.tv_med_date);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvDoseDetails = itemView.findViewById(R.id.tv_dose_details);
            ivMedicineAction = itemView.findViewById(R.id.iv_medicine_action);
        }
    }
}
