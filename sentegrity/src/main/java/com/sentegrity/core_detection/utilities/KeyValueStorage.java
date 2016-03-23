package com.sentegrity.core_detection.utilities;

import android.content.SharedPreferences;

/**
 * Created by dmestrov on 23/03/16.
 */
public class KeyValueStorage {
    private SharedPreferences mPrefs;

    public KeyValueStorage(SharedPreferences prefs) {
        mPrefs = prefs;
    }

    public SharedPreferences getPrefs(){
        return mPrefs;
    }

    public void storeBoolean(String key, boolean data) {
        final SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putBoolean(key, data);
        prefsEditor.commit();
    }

    public void storeFloat(String key, float data) {
        final SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putFloat(key, data);
        prefsEditor.commit();
    }

    public void storeInteger(String key, int data) {
        final SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putInt(key, data);
        prefsEditor.commit();
    }

    public void storeLong(String key, long data) {
        final SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putLong(key, data);
        prefsEditor.commit();
    }

    public void storeString(String key, String data) {
        final SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString(key, data);
        prefsEditor.commit();
    }

    public void remove(String key) {
        final SharedPreferences.Editor prefsEditer = mPrefs.edit();
        prefsEditer.remove(key);
        prefsEditer.commit();
    }

    public boolean readBoolean(String key, boolean defValue) {
        return mPrefs.getBoolean(key, defValue);
    }

    public float readFloat(String key, float defValue) {
        return mPrefs.getFloat(key, defValue);
    }

    public int readInteger(String key, int defValue) {
        return mPrefs.getInt(key, defValue);
    }

    public long readLong(String key, long defValue) {
        return mPrefs.getLong(key, defValue);
    }

    public String readString(String key, String defValue) {
        return mPrefs.getString(key, defValue);
    }
}
