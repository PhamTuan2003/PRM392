package com.example.spendsmart.firebase.viewmodels;



import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.Query;

import com.example.spendsmart.firebase.FirebaseElement;
import com.example.spendsmart.firebase.FirebaseObserver;
import com.example.spendsmart.firebase.FirebaseQueryLiveDataSet;
import com.example.spendsmart.firebase.ListDataSet;
import com.example.spendsmart.firebase.models.WalletEntry;

public class WalletEntriesBaseViewModel extends ViewModel
{
    protected final FirebaseQueryLiveDataSet<WalletEntry> liveData;
    protected final String uid;

    public WalletEntriesBaseViewModel(String uid, Query query)
    {
        this.uid=uid;
        liveData = new FirebaseQueryLiveDataSet<>(WalletEntry.class, query);
    }

    public void observe(LifecycleOwner owner, FirebaseObserver<FirebaseElement<ListDataSet<WalletEntry>>> observer)
    {
        observer.onChanged(liveData.getValue());
        liveData.observe(owner, element -> {
            if(element != null) observer.onChanged(element);
        });
    }

    public void removeObserver(Observer<FirebaseElement<ListDataSet<WalletEntry>>> observer)
    {
        liveData.removeObserver(observer);
    }
}
