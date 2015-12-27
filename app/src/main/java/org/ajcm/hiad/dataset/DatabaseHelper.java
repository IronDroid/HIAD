package org.ajcm.hiad.dataset;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jhonlimaster on 07-12-15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private Context context;
    private static final String DATABASE_NAME = "himnario";
    private static final int DATABASE_VERSION = 1;
    private String pathDB;

    public enum Columns {
        numero, titulo, letra, indice
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        pathDB = "/data/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME;
        loadDB();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "onCreate: ");
        loadDB();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade: ");
        db.execSQL("DROP TABLE IF EXISTS " + DBAdapter.DATABASE_TABLE_2008);
        db.execSQL("DROP TABLE IF EXISTS " + DBAdapter.DATABASE_TABLE_1962);
        onCreate(db);
    }

    public void copydatabase(Context context) throws IOException {
        try {
            getReadableDatabase();
        } catch (Exception ignored){
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
        Log.e(TAG, "copydatabase: ");
    }

    public boolean checkDataBase() {
        SQLiteDatabase checkDB;
        try {
            checkDB = SQLiteDatabase.openDatabase(pathDB, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
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
