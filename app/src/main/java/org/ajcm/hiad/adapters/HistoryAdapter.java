package org.ajcm.hiad.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.ajcm.hiad.CallbackFragments;
import org.ajcm.hiad.R;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.fragments.MusicFragment;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.models.Historial;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<Historial> histories;
    private CallbackFragments callbackFragments;

    public HistoryAdapter(FragmentActivity context, ArrayList<Historial> histories) {
        this.histories = histories;
        this.callbackFragments = (CallbackFragments) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Historial historial = histories.get(position);
        DBAdapter dbAdapter = new DBAdapter(holder.itemView.getContext());
        final Himno himno = dbAdapter.getHimno(historial.getNumero(), true);
        holder.nameHimno.setText(String.format("%d - %s", himno.getNumero(), himno.getTitulo()));
        holder.frecuenciaHimno.setText(String.valueOf(historial.getFrecuencia()));

        holder.itemView.setOnClickListener(view -> callbackFragments.callbackOK(MusicFragment.class, himno));
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameHimno;
        public TextView frecuenciaHimno;

        public ViewHolder(View view) {
            super(view);
            nameHimno = view.findViewById(R.id.name_himno);
            frecuenciaHimno = view.findViewById(R.id.frecuencia_himno);
        }
    }
}
