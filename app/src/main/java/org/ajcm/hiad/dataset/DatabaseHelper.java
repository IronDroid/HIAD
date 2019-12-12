package org.ajcm.hiad.dataset;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import org.ajcm.hiad.utils.UserPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jhonlimaster on 07-12-15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String VERSION_DB_PREF = "version_db";
    private Context context;
    private static final String DATABASE_NAME = "himnario";
    private static final int DATABASE_VERSION = 4;
    private String pathDB;
    private UserPreferences preferences;

    public enum Columns {
        numero, titulo, letra, file_size, indice
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        preferences = new UserPreferences(this.context);
        pathDB = "/data/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME;
        if (!preferences.getBoolean("copy")) {
            loadDB();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "onCreate: ");
        if (!preferences.getBoolean("copy")) {
            loadDB();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade: ");
        preferences.putBoolean("copy", false);
        onCreate(db);
    }

    private void copydatabase(Context context) throws IOException {
        try {
            SQLiteDatabase readableDatabase = getReadableDatabase();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                readableDatabase.close();
            }
        } catch (Exception ignored) {
        }
        InputStream input = context.getAssets().open("hiad2.db");
        OutputStream output = new FileOutputStream(pathDB);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        output.flush();
        output.close();
        input.close();
        preferences.putBoolean("copy", true);
        Log.e(TAG, "copydatabase: ");
    }

    public boolean checkDataBase() {
        SQLiteDatabase checkDB;
        try {
            checkDB = SQLiteDatabase.openDatabase(pathDB, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.e(TAG, "checkDataBase: false");
            return false;
        }
        if (checkDB != null) {
            checkDB.close();
            Log.e(TAG, "checkDataBase: true");
            return true;
        }
        Log.e(TAG, "checkDataBase: false");
        return false;
    }

    public boolean checkUpdate() {
        if (preferences.getInt(VERSION_DB_PREF) < DATABASE_VERSION) {
            File dbFile = new File(pathDB);
            Log.e(TAG, "onUpgrade: delete DB " + dbFile.delete());
            preferences.putBoolean("copy", false);
            loadDB();
            preferences.putInt(VERSION_DB_PREF, DATABASE_VERSION);
            return true;
        } else {
            return false;
        }
    }

    public void loadDB() {
        if (!checkDataBase()) {
            try {
                copydatabase(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
