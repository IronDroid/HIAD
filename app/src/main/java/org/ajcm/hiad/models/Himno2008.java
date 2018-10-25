package org.ajcm.hiad.models;

import android.database.Cursor;

/**
 * Created by jhonlimaster on 12-07-17.
 */

public class Himno2008 extends Himno{

    private long fileSize;
    private boolean favorito;
    private int categoria;
    private int subCategoria;
    private String duracion;

    public enum Columns {
        numero,
        titulo,
        letra,
        file_size,
        indice,
        favorito,
        categoria,
        sub_categoria,
        duracion,
    }

    public static Himno2008 fromCursor(Cursor cursor) {
        Himno2008 himno2008 = new Himno2008();
        himno2008.setNumero(cursor.getInt(Columns.numero.ordinal()));
        himno2008.setTitulo(cursor.getString(Columns.titulo.ordinal()));
        himno2008.setLetra(cursor.getString(Columns.letra.ordinal()));
        himno2008.setFileSize(cursor.getLong(Columns.file_size.ordinal()));
        himno2008.setIndice(cursor.getString(Columns.indice.ordinal()));
        himno2008.setFavorito(cursor.getInt(Columns.favorito.ordinal()) == 1);
        himno2008.setCategoria(cursor.getInt(Columns.categoria.ordinal()));
        himno2008.setSubCategoria(cursor.getInt(Columns.sub_categoria.ordinal()));
        himno2008.setDuracion(cursor.getString(Columns.duracion.ordinal()));
        return himno2008;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public int getSubCategoria() {
        return subCategoria;
    }

    public void setSubCategoria(int subCategoria) {
        this.subCategoria = subCategoria;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }
}
