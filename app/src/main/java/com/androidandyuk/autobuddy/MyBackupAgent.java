package com.androidandyuk.autobuddy;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.util.Log;

/**
 * Created by AndyCr15 on 24/06/2017.
 */

public class MyBackupAgent extends BackupAgentHelper {

    // The name of the SharedPreferences file
    static final String PREFS = "com.androidandyuk.autobuddy";

    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "myprefs";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
        Log.i("MyBackupAgent","onCreate");
    }

}
