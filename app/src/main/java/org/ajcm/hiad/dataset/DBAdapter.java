package org.ajcm.hiad.dataset;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno1962;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.models.Historial;
import org.ajcm.hiad.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jhon_Li
 */
public class DBAdapter {

    private static final String TAG = "DBAdapter";
    public static final String DATABASE_TABLE_2008 = "himnario2008";
    public static final String DATABASE_TABLE_1962 = "himnario1962";
    public static final String DATABASE_TABLE_CATEGORIA = "categorias";
    public static final String DATABASE_TABLE_HISTORIAL = "historial";
    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context context) {
        this.context = context;
        DBHelper = new DatabaseHelper(this.context);
    }

    public Himno getHimno(int number, boolean version2008) {
        open();
        Himno himno = null;
        Cursor query = db.query(tableVersion(version2008), null, Himno1962.Columns.numero.name() + " = " + number, null, null, null, null);
        query.moveToFirst();
        if (version2008) {
            himno = Himno2008.fromCursor(query);
        } else {
            himno = Himno1962.fromCursor(query);
        }
        query.close();
        return himno;
    }

    public ArrayList<? extends Himno> getAllHimno(boolean version2008) {
        open();
        ArrayList<Himno> himnos = new ArrayList<>();
        Cursor query = db.query(tableVersion(version2008), null, null, null, null, null, null);
        while (query.moveToNext()) {
            if (version2008) {
                himnos.add(Himno2008.fromCursor(query));
            } else {
                himnos.add(Himno1962.fromCursor(query));
            }
        }

        query.close();
        return himnos;
    }


    public ArrayList<? extends Himno> getAllHimnoFav(boolean version2008) {
        open();
        ArrayList<Himno> himnos = new ArrayList<>();
        Cursor query = db.query(tableVersion(version2008), null, Himno2008.Columns.favorito + " = 1", null, null, null, null);
        while (query.moveToNext()) {
            if (version2008) {
                himnos.add(Himno2008.fromCursor(query));
            } else {
                himnos.add(Himno1962.fromCursor(query));
            }
        }

        query.close();
        return himnos;
    }

    public ArrayList<Integer> getAllHimnoFavCursor(boolean version2008) {
        open();
        ArrayList<Integer> himnos = new ArrayList<>();
        Cursor query = db.query(tableVersion(version2008), null, Himno2008.Columns.favorito + " = 1", null, null, null, null);
        while (query.moveToNext()) {
            himnos.add(query.getInt(Himno2008.Columns.numero.ordinal()));
        }
        query.close();
        return himnos;
    }

    public void setFav(int numero, boolean fav, boolean version2008) {
        open();
        ContentValues values = new ContentValues();
        values.put(Himno2008.Columns.favorito.name(), fav);
        db.update(tableVersion(version2008), values, Himno2008.Columns.numero.name() + " = " + numero, null);
    }

    public void setHistorial(int numero) {
        open();
        Log.e(TAG, "setHistorial: open");
        Cursor query = db.query(DATABASE_TABLE_HISTORIAL,
                null,
                Historial.Columns.numero.name() + " = " + numero,
                null, null, null, null, null);
        int frecuency = 1;
        boolean newHistory = true;
        if (query.moveToFirst()) {
            newHistory = false;
            frecuency = query.getInt(query.getColumnIndex(Historial.Columns.frecuencia.name())) + 1;
        }
        Log.e(TAG, "setHistorial: frecuency " + frecuency);
        ContentValues values = new ContentValues();
        values.put(Historial.Columns.numero.name(), numero);
        values.put(Historial.Columns.frecuencia.name(), frecuency);
        values.put(Historial.Columns.ultima_fecha.name(), StringUtils.getCurrentDateString());
        Log.e(TAG, "setHistorial: new " + newHistory);
        if (newHistory) {
            Log.e(TAG, "setHistorial: save");
            db.insert(DATABASE_TABLE_HISTORIAL, null, values);
        } else {
            Log.e(TAG, "setHistorial: update");
            db.update(DATABASE_TABLE_HISTORIAL, values, Historial.Columns.numero.name() + " = " + numero, null);
        }
    }

    public void setDuration(int numero, String duration, boolean version2008) {
        open();
        ContentValues values = new ContentValues();
        values.put(Himno2008.Columns.duracion.name(), duration);
        db.update(tableVersion(version2008), values, Himno2008.Columns.numero.name() + " = " + numero, null);
    }

    public Cursor getHimnoForTitle(String filter, boolean version2008) {
        open();
        return db.query(tableVersion(version2008), null, DatabaseHelper.Columns.indice.name() + " LIKE '%" + filter + "%'", null, null, null, Himno2008.Columns.favorito + " DESC, " + DatabaseHelper.Columns.indice.name() + " ASC");
    }

    public Cursor getAllHimnoASC(boolean version2008) {
        open();
        return db.query(tableVersion(version2008), null, null, null, null, null, Himno2008.Columns.favorito + " DESC, " + DatabaseHelper.Columns.indice.name() + " ASC");
    }

    public Cursor getCategorias() {
        open();
        return db.query(DATABASE_TABLE_CATEGORIA, null, null, null, null, null, null, "9");
    }

    public Cursor getHimnoByCategoria(int cat) {
        open();
        return db.query(DATABASE_TABLE_2008, null, "categoria = " + cat, null, null, null, null, null);
    }

    public String getCategoria(int cat) {
        open();
        Cursor query = db.query(DATABASE_TABLE_CATEGORIA, null, "id = " + cat, null, null, null, null, null);
        query.moveToFirst();
        if (query.getCount() == 0) {
            return "";
        }
        String titulo = query.getString(1);
        query.close();
        return titulo;
    }

    public ArrayList<Historial> getHistorial() {
        ArrayList<Historial> historials = new ArrayList<>();
        open();
        Cursor query = db.query(DATABASE_TABLE_HISTORIAL,
                null,
                null,
                null, null, null, Historial.Columns.frecuencia + " DESC", null);
        while (query.moveToNext()) {
            historials.add(Historial.fromCursor(query));
        }
        query.close();
        return historials;
    }

    private void open() throws SQLException {
        db = DBHelper.getWritableDatabase();
    }

    public void close() {
        DBHelper.close();
    }

    public String tableVersion(boolean version2008) {
        return version2008 ? DATABASE_TABLE_2008 : DATABASE_TABLE_1962;
    }


}
