package com.mirea.kt.ribo.contactscw;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Person extends RealmObject{
    @PrimaryKey private String phone;
    private String name;
    private String avatar;

    public Person(String phone, String name, String avatar) {
        this.phone = phone;
        this.name = name;
        this.avatar = avatar;
    }
    public Person() {}
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
