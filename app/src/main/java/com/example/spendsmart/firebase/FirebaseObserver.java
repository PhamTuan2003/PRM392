package com.example.spendsmart.firebase;

public interface FirebaseObserver<T>
{
    void onChanged(T t);
}
