package org.ajcm.hiad.models;

import android.database.Cursor;

public class Historial {

    private int id;
    private int numero;
    private int frecuencia;
    private String ultima_fecha;

    public enum Columns {
        id,
        numero,
        frecuencia,
        ultima_fecha
    }

    public static Historial fromCursor(Cursor cursor) {
        Historial historial = new Historial();
        historial.setId(cursor.getInt(Historial.Columns.id.ordinal()));
        historial.setNumero(cursor.getInt(Historial.Columns.numero.ordinal()));
        historial.setFrecuencia(cursor.getInt(Columns.frecuencia.ordinal()));
        historial.setUltima_fecha(cursor.getString(Columns.ultima_fecha.ordinal()));
        return historial;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(int frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getUltima_fecha() {
        return ultima_fecha;
    }

    public void setUltima_fecha(String ultima_fecha) {
        this.ultima_fecha = ultima_fecha;
    }
}
