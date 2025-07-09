package com.vishwajeeth.medicinetime.data.source;

import androidx.annotation.NonNull;

public class Medicine {
    public int id;

    @NonNull
    public String pillName;

    public int userProfileId; // Foreign key đến UserProfile

    public Medicine() {
        // Constructor mặc định
    }

    public Medicine(String pillName, int userProfileId) {
        this.pillName = pillName;
        this.userProfileId = userProfileId;
    }

    // Thêm các trường khác nếu cần
    // public String doseQuantity;
    // public String doseUnit;
    // ...
}