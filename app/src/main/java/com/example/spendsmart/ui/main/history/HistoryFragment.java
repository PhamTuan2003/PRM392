package com.example.spendsmart.ui.main.history;

import static android.app.PendingIntent.getActivity;

import static androidx.core.content.ContextCompat.startActivity;
import static java.security.AccessController.getContext;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.webauthn.Cbor;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import androidx.fragment.app.FragmentActivity;
import androidx.core.util.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.example.spendsmart.R;
import com.example.spendsmart.firebase.viewmodel_factories.WalletEntriesHistoryViewModelFactory;
import com.example.spendsmart.base.BaseFragment;
import com.example.spendsmart.ui.options.OptionsActivity;

public class HistoryFragment extends BaseFragment {
    public static final CharSequence TITLE = "History";
    Calendar calendarStart;
    Calendar calendarEnd;
    private RecyclerView historyRecyclerView;
    private WalletEntriesRecyclerViewAdapter historyRecyclerViewAdapter;
    private Menu menu;
    private TextView dividerTextView;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        dividerTextView = view.findViewById(R.id.divider_textview);
        dividerTextView.setText("Last 100 payments:");
        historyRecyclerView = view.findViewById(R.id.history_recycler_view);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        historyRecyclerViewAdapter = new WalletEntriesRecyclerViewAdapter(getActivity(), getUid());
        historyRecyclerView.setAdapter(historyRecyclerViewAdapter);

        historyRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                historyRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_fragment_menu, menu);
        this.menu = menu;
        updateCalendarIcon();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1000034:
                showSelectDateRangeDialog();
                return true;
            case 1000041:
                startActivity(new Intent(getActivity(), OptionsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateCalendarIcon() {
        MenuItem calendarIcon = menu.findItem(R.id.action_date_range);
        if (calendarIcon == null)
            return;
        WalletEntriesHistoryViewModelFactory.Model model = WalletEntriesHistoryViewModelFactory.getModel(getUid(),
                getActivity());
        if (model.hasDateSet()) {
            calendarIcon.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.icon_calendar_active));

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

            dividerTextView.setText("Date range: " + dateFormat.format(model.getStartDate().getTime())
                    + "  -  " + dateFormat.format(model.getEndDate().getTime()));
        } else {
            calendarIcon.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.icon_calendar));

            dividerTextView.setText("Last 100 payments:");
        }
    }

    private void showSelectDateRangeDialog() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select date range");
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                if (selection != null) {
                    calendarStart = Calendar.getInstance();
                    calendarStart.setTimeInMillis(selection.first);
                    calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                    calendarStart.set(Calendar.MINUTE, 0);
                    calendarStart.set(Calendar.SECOND, 0);

                    calendarEnd = Calendar.getInstance();
                    calendarEnd.setTimeInMillis(selection.second);
                    calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
                    calendarEnd.set(Calendar.MINUTE, 59);
                    calendarEnd.set(Calendar.SECOND, 59);
                    calendarUpdated();
                    updateCalendarIcon();
                }
            }
        });
        picker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Không làm gì khi cancel
            }
        });
        picker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                calendarStart = null;
                calendarEnd = null;
                calendarUpdated();
                updateCalendarIcon();
            }
        });
        picker.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "TAG");
    }

    private void calendarUpdated() {
        historyRecyclerViewAdapter.setDateRange(calendarStart, calendarEnd);
    }
}
