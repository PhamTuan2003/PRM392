package com.vishwajeeth.medicinetime.medicine;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vishwajeeth.medicinetime.R;
import com.vishwajeeth.medicinetime.addmedicine.AddMedicineActivity;
import com.vishwajeeth.medicinetime.data.source.MedicineAlarm;
import com.vishwajeeth.medicinetime.views.RobotoLightTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Medicine Fragment
 */

public class MedicineFragment extends Fragment implements MedicineContract.View, MedicineAdapter.OnItemClickListener {

    private RecyclerView rvMedList;
    private ImageView noMedIcon;
    private RobotoLightTextView noMedText;
    private TextView addMedNow;
    private View noMedView;
    private ProgressBar progressLoader;

    private MedicineContract.Presenter presenter;
    private MedicineAdapter medicineAdapter;

    public MedicineFragment() {

    }

    public static MedicineFragment newInstance() {
        Bundle args = new Bundle();
        MedicineFragment fragment = new MedicineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        medicineAdapter = new MedicineAdapter(new ArrayList<>(0));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine, container, false);

        // Initialize views
        rvMedList = view.findViewById(R.id.medicine_list);
        noMedIcon = view.findViewById(R.id.noMedIcon);
        noMedText = view.findViewById(R.id.noMedText);
        addMedNow = view.findViewById(R.id.add_med_now);
        noMedView = view.findViewById(R.id.no_med_view);
        progressLoader = view.findViewById(R.id.progressLoader);

        setAdapter();
        setupClickListeners();
        return view;
    }

    private void setupClickListeners() {
        addMedNow.setOnClickListener(v -> addMedicine());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = Objects.requireNonNull(getActivity()).findViewById(R.id.fab_add_task);
        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(v -> presenter.addNewMedicine());
    }

    private void setAdapter() {
        rvMedList.setAdapter(medicineAdapter);
        rvMedList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMedList.setHasFixedSize(true);
        medicineAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        presenter.onStart(day);
    }

    @Override
    public void setPresenter(MedicineContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        if (getView() == null) {
            return;
        }
        progressLoader.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMedicineList(List<MedicineAlarm> medicineAlarmList) {
        medicineAdapter.replaceData(medicineAlarmList);
        rvMedList.setVisibility(View.VISIBLE);
        noMedView.setVisibility(View.GONE);
    }

    @Override
    public void showAddMedicine() {
        Intent intent = new Intent(getContext(), AddMedicineActivity.class);
        startActivityForResult(intent, AddMedicineActivity.REQUEST_ADD_TASK);
    }

    @Override
    public void showMedicineDetails(long taskId, String medName) {
        Intent intent = new Intent(getContext(), AddMedicineActivity.class);
        intent.putExtra(AddMedicineActivity.EXTRA_TASK_ID, taskId);
        intent.putExtra(AddMedicineActivity.EXTRA_TASK_NAME, medName);
        startActivity(intent);
    }

    @Override
    public void showLoadingMedicineError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    @Override
    public void showNoMedicine() {
        showNoTasksViews(
                getResources().getString(R.string.no_medicine_added));
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_me_message));
    }

    @Override
    public void showMedicineDeletedSuccessfully() {
        showMessage(getString(R.string.successfully_deleted_message));
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        presenter.onStart(day);
    }

    private void showMessage(String message) {
        if (getView() != null)
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void addMedicine() {
        showAddMedicine();
    }

    private void showNoTasksViews(String mainText) {
        rvMedList.setVisibility(View.GONE);
        noMedView.setVisibility(View.VISIBLE);
        noMedText.setText(mainText);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode);
    }

    @Override
    public void onMedicineDeleteClicked(MedicineAlarm medicineAlarm) {
        presenter.deleteMedicineAlarm(medicineAlarm, getActivity());
    }
}
