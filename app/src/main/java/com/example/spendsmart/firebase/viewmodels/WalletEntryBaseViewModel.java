package com.example.spendsmart.firebase.viewmodels;



import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.FirebaseDatabase;

import com.example.spendsmart.firebase.FirebaseElement;
import com.example.spendsmart.firebase.FirebaseObserver;
import com.example.spendsmart.firebase.FirebaseQueryLiveDataElement;
import com.example.spendsmart.firebase.models.WalletEntry;

public class WalletEntryBaseViewModel extends ViewModel
{
    protected final FirebaseQueryLiveDataElement<WalletEntry> liveData;
    protected final String uid;

    public WalletEntryBaseViewModel(String uid, String walletEntryId)
    {
        this.uid=uid;
        liveData = new FirebaseQueryLiveDataElement<>(WalletEntry.class, FirebaseDatabase.getInstance().getReference()
                .child("wallet-entries").child(uid).child("default").child(walletEntryId));
    }

    public void observe(LifecycleOwner owner, FirebaseObserver<FirebaseElement<WalletEntry>> observer)
    {
        if(liveData.getValue() != null) observer.onChanged(liveData.getValue());
        liveData.observe(owner, element -> {
            if(element != null) observer.onChanged(element);
        });
    }

    public void removeObserver(Observer<FirebaseElement<WalletEntry>> observer)
    {
        liveData.removeObserver(observer);
    }


}
