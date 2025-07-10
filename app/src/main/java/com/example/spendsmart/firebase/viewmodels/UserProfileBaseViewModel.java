package com.example.spendsmart.firebase.viewmodels;


import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.FirebaseDatabase;

import com.example.spendsmart.firebase.FirebaseElement;
import com.example.spendsmart.firebase.FirebaseObserver;
import com.example.spendsmart.firebase.FirebaseQueryLiveDataElement;
import com.example.spendsmart.firebase.models.User;

public class UserProfileBaseViewModel extends ViewModel
{
    private final FirebaseQueryLiveDataElement<User> liveData;

    public UserProfileBaseViewModel(String uid)
    {
        liveData = new FirebaseQueryLiveDataElement<>(User.class, FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid));
    }

    public void observe(LifecycleOwner owner, FirebaseObserver<FirebaseElement<User>> observer)
    {
        if(liveData.getValue() != null) observer.onChanged(liveData.getValue());
        liveData.observe(owner, firebaseElement -> {
            if(firebaseElement != null) observer.onChanged(firebaseElement);
        });
    }

}