package com.vishwajeeth.medicinetime.data.source.local;

import androidx.annotation.NonNull;

public class UserProfile {
    public int id;

    @NonNull
    public String name;

    public Integer age;

    public String relation;

    public String avatarUri;

    public UserProfile() {
        // Constructor mặc định
    }

    public UserProfile(String name, Integer age, String relation, String avatarUri) {
        this.name = name;
        this.age = age;
        this.relation = relation;
        this.avatarUri = avatarUri;
    }
}