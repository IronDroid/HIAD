package org.ajcm.hiad.models;

import android.database.Cursor;

/**
 * Created by jhonlimaster on 12-07-17.
 */

public class Himno1962 extends Himno{

    private boolean favorito;

    public Himno1962(){
        super();
    }

    public enum Columns{
        numero,
        titulo,
        letra,
        indice,
        favorito,
    }

    public static Himno1962 fromCursor(Cursor cursor) {
        Himno1962 himno1962 = new Himno1962();
        himno1962.setNumero(cursor.getInt(Columns.numero.ordinal()));
        himno1962.setTitulo(cursor.getString(Columns.titulo.ordinal()));
        himno1962.setLetra(cursor.getString(Columns.letra.ordinal()));
        himno1962.setIndice(cursor.getString(Columns.indice.ordinal()));
        himno1962.setFavorito(cursor.getInt(Columns.favorito.ordinal()) == 1);
        return himno1962;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
}
