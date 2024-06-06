package com.mirea.kt.ribo.contactscw;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity
        implements PersonAdapter.OnItemSelectedListener{
    private DBManager dbManager;
    private PersonAdapter adapter;
    Realm realmInstance;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.dbManager = new DBManager();
        //тулбар
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_menu));
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        if (ab!=null){
            ab.setTitle(R.string.app_name);
        }
        //Проверяем на первый старт (Если таковой подгружаем первые контакты)
        if(CheckFirstStart()){
            Log.i("MainActivity", "This is first start");
            try {
                JSONArray getContacts = GettingContacts();
                if (getContacts != null) {
                    for (int i = 0; i < getContacts.length(); i++) {
                        dbManager.SavePersonToDB(new Person(
                                getContacts.getJSONObject(i).getString("phone"),
                                getContacts.getJSONObject(i).getString("name"),
                                getContacts.getJSONObject(i).getString("avatar")
                        ));
                    }
                    Log.i("MainActivity", "Getting first contacts successful!");
                    SaveFirstStart();
                }
            } catch (JSONException e) {
                Log.e("MainActivity", "GettingContacts JSONException: " + e);
            }
        }
        //Подключаем адаптер
        RecyclerView rcView = findViewById(R.id.recyclerView);
        ArrayList<Person> persons = dbManager.GetAllPersonFromDB();
        Collections.sort(persons, Comparator.comparing(Person::getName));
        adapter = new PersonAdapter(persons,this, true);
        rcView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rcView.setAdapter(adapter);
    }
    //Функция проверки на первый старт
    private boolean CheckFirstStart(){
        SharedPreferences preferences = getSharedPreferences ("contactsCW_prefs", MODE_PRIVATE);
        return preferences.getBoolean ("first_start", true);
    }
    //Сохраняем, что первый запуск уже был
    private void SaveFirstStart(){
        SharedPreferences preferences = getSharedPreferences ("contactsCW_prefs", MODE_PRIVATE);
        SharedPreferences. Editor editor = preferences.edit();
        editor.putBoolean("first_start", false);
        editor.apply();
    }
    //Функция вызова меню в тулбаре
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    //Функция действий меню тулбара
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_update) {
            Toast.makeText(this, R.string.update, Toast.LENGTH_LONG).show();
            notifyPersonsUpdate();
        }else if(item.getItemId() == R.id.action_exit) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else if(item.getItemId() == R.id.action_new) {
            Person person = new Person("","","");
            showDialog(person);
        }else if(item.getItemId() == R.id.action_deleteAll) {
            showConfirmDialog(null);
        }
        return true;
    }
    //Функция запроса начальных контактов с сервера
    private JSONArray GettingContacts() throws JSONException {
        String server = "https://android-for-students.ru";
        String serverPath = "/coursework/login.php";
        HashMap<String, String> map = new HashMap<>();
        map.put("lgn", "Student32784");
        map.put("pwd", "sZ1i0L5");
        map.put("g", "RIBO-02-22");
        HTTPRunnable httpRunnable = new HTTPRunnable(server + serverPath, map);
        Thread th = new Thread(httpRunnable);
        th.start();
        int resultCode = 0;
        try {
            th.join();
        } catch (InterruptedException ex) {
            Log.e("LoginActivity", "AuthCheck_InterruptedException");
        } finally {
            String rbody = httpRunnable.getResponseBody();
            if (rbody!=null) {
                JSONObject jSONObject = new JSONObject(rbody);
                resultCode = jSONObject.getInt("result_code");
                JSONArray jsonArray = jSONObject.getJSONArray("data");
                Log.i("MainActivity", "GettingContacts_ResponseCode: " + resultCode);
                Log.i("MainActivity", "Rbody: " + jsonArray);

                return jsonArray;
            }else{
                Log.e("MainActivity", "GettingContacts no response body");
                return null;
            }
        }
    }
    //Функция вызываемая при нажатии в списке RecView
    @Override
    public void onSelected(Person person) {

    }
    //функция меню при долгом нажатии
    @Override
    public void onMenuAction(Person person, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_call){
            Intent i = new Intent(Intent.ACTION_DIAL);
            String p = "tel:" + person.getPhone();
            i.setData(Uri.parse(p));
            startActivity(i);
        }else if (id == R.id.action_edit){
            dbManager.DeletePersonFromDB(person);
            showDialog(person);
        } else if (id == R.id.action_share){
            sharePerson(person);
        }else if (id == R.id.action_delete){
            showConfirmDialog(person);
        }
    }
    //Функция отправки контакта в другие приложения
    private void sharePerson(Person person) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Контакт: \n" +
                        "Имя : " +
                        person.getName() +
                        "\n" +
                        "Номер телефона: " +
                        person.getPhone());
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
    //Функция оповещения и обновления списка
    private void notifyPersonsUpdate(){
        adapter.setPersons(dbManager.GetAllPersonFromDB());
        adapter.notifyDataSetChanged();
    }
    //Функция вызова диалога подтверждения на удаление одного/всех
    private void showConfirmDialog(Person person){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_confirm);
        Button btnConfirm = dialog.findViewById(R.id.saveButton);
        Button btnCancel = dialog.findViewById(R.id.cancelButton);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(person!=null){
                    dbManager.DeletePersonFromDB(person);
                    notifyPersonsUpdate();
                }else{
                    dbManager.DeleteAllPersonFromDB();
                    notifyPersonsUpdate();
                }
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
    //Функция вызова диалога для редактирования и создания
    private void showDialog(Person person){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_person);
        dialog.setCancelable(false);

        ImageView imAva = dialog.findViewById(R.id.ivAvatar);
        EditText etAvatar = dialog.findViewById(R.id.url);
        EditText etName = dialog.findViewById(R.id.name);
        EditText etNumber = dialog.findViewById(R.id.number);
        Button btnSave = dialog.findViewById(R.id.saveButton);
        Button btnCancel = dialog.findViewById(R.id.cancelButton);
        TextView errorText = dialog.findViewById(R.id.errorText);

        String personAvatarSaver = person.getAvatar();
        if(person != null){
            etAvatar.setText(person.getAvatar());
            etName.setText(person.getName());
            etNumber.setText(person.getPhone());
            String imagename = person.getPhone();
            if(ImageStorage.checkifImageExists(imagename)) {
                File file = ImageStorage.getImage("/"+imagename+".jpg");
                String path = file.getAbsolutePath();
                if (path != null){
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    imAva.setImageBitmap(bitmap);
                }
            } else {
                new GetImages(person.getAvatar(), imAva, imagename).execute() ;
            }
        }
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etName.getText().toString().isEmpty()
                        && !etNumber.getText().toString().isEmpty()) {
                    person.setAvatar(etAvatar.getText().toString());
                    person.setName(etName.getText().toString());
                    person.setPhone(etNumber.getText().toString());
                    if(dbManager.CheckCollisionInDB(person)){
                        etNumber.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_rev));
                        errorText.setText(R.string.errorColision);
                        errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    }else {
                        errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
                        dbManager.DeletePersonFromDB(person);
                        dbManager.SavePersonToDB(person);
                        notifyPersonsUpdate();
                        dialog.dismiss();
                    }
                }else {
                    etName.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake));
                    etName.setHintTextColor(Color.RED);
                    etNumber.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_rev));
                    etNumber.setHintTextColor(Color.RED);
                    errorText.setText(R.string.errorNoNameAndNumber);
                    errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                person.setAvatar(personAvatarSaver);
                if(!person.getPhone().isEmpty()){
                    if(!dbManager.CheckCollisionInDB(person)) {
                        dbManager.SavePersonToDB(person);
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}