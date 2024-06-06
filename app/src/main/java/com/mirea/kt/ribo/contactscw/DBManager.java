package com.mirea.kt.ribo.contactscw;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class DBManager {
    private Realm realm;
    public DBManager() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("contacts_bd")
                .schemaVersion(1)
                .build();
        this.realm = Realm.getInstance(config);
    }
    public void SavePersonToDB(Person person){
        realm.beginTransaction();
        realm.insert(person);
        realm.commitTransaction();
    }
    public ArrayList<Person> GetAllPersonFromDB(){
        RealmResults<Person> resultRealmList = realm.where(Person.class).findAll();
        ArrayList<Person> personsList = new ArrayList<>(realm.copyFromRealm(resultRealmList));
        return personsList;
    }
    public void DeletePersonFromDB(Person person){
        realm.beginTransaction();
        RealmResults<Person> resultRealmList = realm.where(Person.class).equalTo("phone",
                person.getPhone()).findAll();
        resultRealmList.deleteAllFromRealm();
        realm.commitTransaction();
    }
    public boolean CheckCollisionInDB(Person person){
        Person resultRealmList = realm.where(Person.class).equalTo("phone", person.getPhone()).findFirst();
        if (resultRealmList==null){
            return false;
        }else{
            return true;
        }
    }
    public void DeleteAllPersonFromDB(){
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
}
