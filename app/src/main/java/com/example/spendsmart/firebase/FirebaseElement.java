package com.example.spendsmart.firebase;

import com.google.firebase.database.DatabaseError;

public class FirebaseElement<T>
{
    private T element;

    public FirebaseElement(T element)
    {
        this.element = element;
    }

    public FirebaseElement(DatabaseError databaseError){}

    public T getElement()
    {
        return element;
    }

    public boolean hasNoError()
    {
        return element != null;
    }
}
