package com.example.spendsmart.ui.options.categories;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.SeekBar;
import android.widget.LinearLayout;
import android.graphics.Color;

import com.example.spendsmart.R;
import com.example.spendsmart.base.BaseActivity;
import com.example.spendsmart.exceptions.EmptyStringException;
import com.example.spendsmart.firebase.FirebaseElement;
import com.example.spendsmart.firebase.FirebaseObserver;
import com.example.spendsmart.firebase.models.User;
import com.example.spendsmart.firebase.models.WalletEntryCategory;
import com.example.spendsmart.firebase.viewmodel_factories.UserProfileViewModelFactory;

public class AddCustomCategoryActivity extends BaseActivity {
    private TextInputEditText selectNameEditText;
    private Button selectColorButton;
    private Button addCustomCategoryButton;
    private User user;
    private ImageView iconImageView;
    private int selectedColor = Color.parseColor("#000000");
    private TextInputLayout selectNameInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom_category);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add custom category");

        UserProfileViewModelFactory.getModel(getUid(), this).observe(this,
                new FirebaseObserver<FirebaseElement<User>>() {
                    @Override
                    public void onChanged(FirebaseElement<User> firebaseElement) {
                        if (firebaseElement.hasNoError()) {
                            AddCustomCategoryActivity.this.user = firebaseElement.getElement();
                            dataUpdated();
                        }
                    }
                });
    }

    private void dataUpdated() {
        if (user == null)
            return;
        iconImageView = findViewById(R.id.icon_imageview);
        iconImageView.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
        selectNameEditText = findViewById(R.id.select_name_edittext);
        selectNameInputLayout = findViewById(R.id.select_name_inputlayout);
        selectColorButton = findViewById(R.id.select_color_button);
        selectColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sử dụng dialog custom với SeekBar để chọn màu
                final LinearLayout layout = new LinearLayout(AddCustomCategoryActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final SeekBar seekRed = new SeekBar(AddCustomCategoryActivity.this);
                final SeekBar seekGreen = new SeekBar(AddCustomCategoryActivity.this);
                final SeekBar seekBlue = new SeekBar(AddCustomCategoryActivity.this);
                seekRed.setMax(255);
                seekGreen.setMax(255);
                seekBlue.setMax(255);
                seekRed.setProgress((selectedColor >> 16) & 0xFF);
                seekGreen.setProgress((selectedColor >> 8) & 0xFF);
                seekBlue.setProgress((selectedColor) & 0xFF);
                layout.addView(seekRed);
                layout.addView(seekGreen);
                layout.addView(seekBlue);
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AddCustomCategoryActivity.this)
                        .setTitle("Chọn màu")
                        .setView(layout)
                        .setPositiveButton("OK", (dialog, which) -> {
                            selectedColor = Color.rgb(seekRed.getProgress(), seekGreen.getProgress(),
                                    seekBlue.getProgress());
                            iconImageView.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                        })
                        .setNegativeButton("Hủy", null);
                builder.show();
            }
        });

        addCustomCategoryButton = findViewById(R.id.add_custom_category_button);
        addCustomCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addCustomCategory(selectNameEditText.getText().toString(),
                            "#" + Integer.toHexString(selectedColor));
                } catch (EmptyStringException e) {
                    selectNameInputLayout.setError(e.getMessage());
                }
            }
        });
    }

    private void addCustomCategory(String categoryName, String categoryHtmlCode) throws EmptyStringException {
        if (categoryName == null || categoryName.length() == 0)
            throw new EmptyStringException("Entry name length should be > 0");

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid()).child("customCategories").push().setValue(
                        new WalletEntryCategory(categoryName, categoryHtmlCode));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
    }
}