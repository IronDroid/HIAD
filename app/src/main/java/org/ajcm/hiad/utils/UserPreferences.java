package org.ajcm.hiad.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserPreferences {

    private Context context;

    public UserPreferences(Context context){
        this.context = context;
    }

    private SharedPreferences.Editor getEditor(){
        return getPreferences().edit();
    }

    private SharedPreferences getPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.commit();
    }

    public void putInt(String tag, int value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(tag, value);
        editor.commit();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public String getString(String key) {
        SharedPreferences sharedPreferences = getPreferences();
        return sharedPreferences.getString(key, "");
    }

    public int getInt(String key) {
        SharedPreferences sharedPreferences = getPreferences();
        return sharedPreferences.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        SharedPreferences sharedPreferences = getPreferences();
        return sharedPreferences.getBoolean(key, false);
    }

    public void clear(){
        SharedPreferences.Editor editor = getEditor();
        editor.clear();
        editor.commit();
    }

}