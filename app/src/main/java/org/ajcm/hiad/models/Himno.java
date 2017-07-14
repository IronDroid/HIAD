package org.ajcm.hiad.models;

import android.database.Cursor;

import org.ajcm.hiad.dataset.DatabaseHelper;

/**
 * Created by jhonlimaster on 07-12-15.
 */
public abstract class Himno {
    private int numero;
    private String titulo;
    private String letra;
    private String indice;

    public static Himno fromCursor(Cursor cursor, boolean version1962){
        if (version1962){
            return Himno1962.fromCursor(cursor);
        } else {
            return Himno2008.fromCursor(cursor);
        }
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public String getIndice() {
        return indice;
    }

    public void setIndice(String indice) {
        this.indice = indice;
    }
}
