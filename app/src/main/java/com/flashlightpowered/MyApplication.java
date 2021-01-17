package com.flashlightpowered;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

public class MyApplication extends Application {

    SharedPreferences sharedPreferences;
    String permissions;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name),
                Activity.MODE_PRIVATE);

        sharedPreferences.getString(permissions, permissions);
        if (permissions.equals("yes")) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        } else {
            startActivity(new Intent(getApplicationContext(), SplashActivity.class));
        }
    }
}
