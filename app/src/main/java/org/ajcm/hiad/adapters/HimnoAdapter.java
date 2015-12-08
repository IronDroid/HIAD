package org.ajcm.hiad.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.ajcm.hiad.R;
import org.ajcm.hiad.models.Himno;

import java.util.ArrayList;

/**
 * Created by jhonlimaster on 07-12-15.
 */
public class HimnoAdapter extends BaseAdapter {

    private static final String TAG = "HimnoAdapter";
    private Context context;
    private ArrayList<Himno> himnos;

    public HimnoAdapter(Context context, ArrayList<Himno> himnos) {
        this.context = context;
        this.himnos = himnos;
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
        final Himno himno = himnos.get(i);

        viewHolder.numeroHimno.setText(String.valueOf(himno.getNumero()));
        viewHolder.tituloHimno.setText(himno.getTitulo());

        return view;
    }

    public void setData(ArrayList<Himno> data){
        this.himnos = data;
    }

    public class ViewHolder {
        TextView numeroHimno;
        TextView tituloHimno;

        public ViewHolder(View itemView) {
            numeroHimno = (TextView) itemView.findViewById(R.id.numero_himno);
            tituloHimno = (TextView) itemView.findViewById(R.id.titulo_himno);
        }
    }
}