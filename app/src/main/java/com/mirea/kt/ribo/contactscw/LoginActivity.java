package com.mirea.kt.ribo.contactscw;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.realm.Realm;

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button loginButton;
    TextView errorText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Realm.init(this);

        ImageView ivLogo = findViewById(R.id.mireaLogo);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        errorText = findViewById(R.id.errorText);
        loginButton = findViewById(R.id.loginButton);
        Switch remember = findViewById(R.id.remember);


        loginButton.setOnClickListener(view -> {
            String user = username.getText().toString();
            String pswrd = password.getText().toString();
            if ((!user.isEmpty() && !pswrd.isEmpty())) {
                try {
                    int resAuthCheck = AuthCheck(user, pswrd);
                    switch (resAuthCheck) {
                        case -1:
                            errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            errorText.setTextColor(Color.RED);
                            errorText.setText(R.string.errorWrongInput);
                            break;
                        case 1:
                            errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            errorText.setTextColor(Color.GREEN);
                            errorText.setText(R.string.enterIsSuccessfull);
                            findViewById(R.id.cwLogin).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
                            doAnimation(ivLogo, AnimationUtils.loadAnimation(this, R.anim.slide_center_upsize));
                            if (remember.isChecked()) {
                                saveData();
                            } else {
                                clearData();
                            }
                            break;
                        default:
                            errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            errorText.setTextColor(Color.RED);
                            errorText.setText(R.string.errorConnectServer);
                            break;
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                errorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                errorText.setTextColor(Color.RED);
                username.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake));
                username.setHintTextColor(Color.RED);
                password.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake_rev));
                password.setHintTextColor(Color.RED);
                errorText.setText(R.string.errorNoLoginPassword);
            }
        });

        if(checkData()){
            username.setText(getUsernameData());
            password.setText(getPasswordData());
            remember.setChecked(true);
        }
    }

    private String getUsernameData() {
        SharedPreferences preferences = getSharedPreferences ("contactsCW_prefs", MODE_PRIVATE);
        return preferences.getString ("save_login","");
    }
    private String getPasswordData() {
        SharedPreferences preferences = getSharedPreferences ("contactsCW_prefs", MODE_PRIVATE);
        return preferences.getString ("save_password","");
    }

    private boolean checkData() {
        SharedPreferences preferences = getSharedPreferences ("contactsCW_prefs", MODE_PRIVATE);
        return preferences.getBoolean ("save", false);
    }

    private void clearData() {
        SharedPreferences preferences = getSharedPreferences ("contactsCW_prefs", MODE_PRIVATE);
        SharedPreferences. Editor editor = preferences.edit();
        editor.putBoolean("save", false);
        editor.putString("save_login", "");
        editor.putString("save_password", "");
        editor.apply();
    }

    private void saveData() {
        SharedPreferences preferences = getSharedPreferences ("contactsCW_prefs", MODE_PRIVATE);
        SharedPreferences. Editor editor = preferences.edit();
        editor.putBoolean("save", true);
        editor.putString("save_login", username.getText().toString());
        editor.putString("save_password", password.getText().toString());
        editor.apply();
    }

    private void doAnimation(final ImageView affectedView, Animation animation) {
        animation.setDuration(3000L);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doLogin();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        affectedView.setAnimation(animation);
        animation.start();
    }
    private void doLogin(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private Integer AuthCheck(String login, String password) throws JSONException {

        String server = "https://android-for-students.ru";
        String serverPath = "/coursework/login.php";
        HashMap<String, String> map = new HashMap<>();
        map.put("lgn", login);
        map.put("pwd", password);
        map.put("g", "RIBO-02-22");
        HTTPRunnable httpRunnable = new HTTPRunnable(server + serverPath, map);
        Thread th = new Thread(httpRunnable);
        th.start();
        int resultCode = 0;
        try {
            th.join();
        } catch (InterruptedException ex) {
            Log.e("LoginActivity", "AuthCheck InterruptedException");
        } finally {
            String rbody = httpRunnable.getResponseBody();
            if (rbody==null) {
                Log.e("LoginActivity", "AuthCheck no response body");
            }else {
                JSONObject jSONObject = new JSONObject(rbody);
                resultCode = jSONObject.getInt("result_code");
                Log.i("LoginActivity", "AuthCheck_ResponseCode: " + resultCode);
            }
        }
        return resultCode;
    }
}