package org.ajcm.hiad.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ajcm.hiad.R;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno1962;
import org.ajcm.hiad.models.Himno2008;

import java.util.ArrayList;

/**
 * Created by jhonlimaster on 07-12-15.
 */
public class HimnoAdapter extends BaseAdapter {

    private static final String TAG = "HimnoAdapter";
    private Context context;
    private ArrayList<Himno> himnos;
    private boolean version2008;

    public HimnoAdapter(Context context, ArrayList<Himno> himnos, boolean version2008) {
        this.context = context;
        this.himnos = himnos;
        this.version2008 = version2008;
    }

    @Override
    public int getCount() {
        return himnos.size();
    }

    @Override
    public Object getItem(int i) {
        return himnos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return himnos.indexOf(himnos.get(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.row_himno, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        Himno himno = himnos.get(i);

        viewHolder.numeroHimno.setText(String.valueOf(himno.getNumero()));
        viewHolder.tituloHimno.setText(himno.getTitulo());
        boolean fav;
        if (version2008){
            fav = ((Himno2008)himno).isFavorito();
        } else {
            fav = ((Himno1962)himno).isFavorito();
        }
        if (fav){
            viewHolder.imageFav.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageFav.setVisibility(View.GONE);
        }

        return view;
    }

    public void setData(ArrayList<Himno> data){
        this.himnos = data;
    }

    public class ViewHolder {
        TextView numeroHimno;
        TextView tituloHimno;
        ImageView imageFav;

        public ViewHolder(View itemView) {
            numeroHimno = (TextView) itemView.findViewById(R.id.numero_himno);
            tituloHimno = (TextView) itemView.findViewById(R.id.titulo_himno);
            imageFav = (ImageView) itemView.findViewById(R.id.image_fav);
        }
    }
}