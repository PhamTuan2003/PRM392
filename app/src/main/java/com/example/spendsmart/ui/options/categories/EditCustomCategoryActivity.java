package com.example.spendsmart.ui.options.categories;

import android.content.res.ColorStateList;
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
import com.example.spendsmart.firebase.models.WalletEntryCategory;

public class EditCustomCategoryActivity extends BaseActivity {
    private TextInputEditText selectNameEditText;
    private Button selectColorButton;
    private Button editCustomCategoryButton;
    private ImageView iconImageView;
    private int selectedColor;
    private String categoryID;
    private Button removeCustomCategoryButton;
    private String categoryName;
    private TextInputLayout selectNameInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryID = getIntent().getExtras().getString("category-id");
        categoryName = getIntent().getExtras().getString("category-name");
        selectedColor = getIntent().getExtras().getInt("category-color");

        setContentView(R.layout.activity_edit_custom_category);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit custom category");

        iconImageView = findViewById(R.id.icon_imageview);
        iconImageView.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
        selectNameEditText = findViewById(R.id.select_name_edittext);
        selectNameEditText.setText(categoryName);
        selectNameInputLayout = findViewById(R.id.select_name_inputlayout);
        selectColorButton = findViewById(R.id.select_color_button);
        selectColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sử dụng dialog custom với SeekBar để chọn màu
                final LinearLayout layout = new LinearLayout(EditCustomCategoryActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final SeekBar seekRed = new SeekBar(EditCustomCategoryActivity.this);
                final SeekBar seekGreen = new SeekBar(EditCustomCategoryActivity.this);
                final SeekBar seekBlue = new SeekBar(EditCustomCategoryActivity.this);
                seekRed.setMax(255);
                seekGreen.setMax(255);
                seekBlue.setMax(255);
                seekRed.setProgress((selectedColor >> 16) & 0xFF);
                seekGreen.setProgress((selectedColor >> 8) & 0xFF);
                seekBlue.setProgress((selectedColor) & 0xFF);
                layout.addView(seekRed);
                layout.addView(seekGreen);
                layout.addView(seekBlue);
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(EditCustomCategoryActivity.this)
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

        editCustomCategoryButton = findViewById(R.id.edit_custom_category_button);
        editCustomCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    editCustomCategory(selectNameEditText.getText().toString(),
                            "#" + Integer.toHexString(selectedColor));
                } catch (EmptyStringException e) {
                    selectNameInputLayout.setError(e.getMessage());
                }

            }
        });

        removeCustomCategoryButton = findViewById(R.id.remove_custom_category_button);
        removeCustomCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child("users").child(getUid()).child("customCategories").child(categoryID).removeValue();
                finish();
            }
        });
    }

    private void editCustomCategory(String categoryName, String categoryHtmlCode) throws EmptyStringException {
        if (categoryName == null || categoryName.length() == 0)
            throw new EmptyStringException("Entry name length should be > 0");

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid()).child("customCategories").child(categoryID).setValue(
                        new WalletEntryCategory(categoryName, categoryHtmlCode));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
    }
}