package com.example.spendsmart.base;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity
{
    public String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public abstract boolean onOptionsItemSelected(MenuItem menuItem);
}
