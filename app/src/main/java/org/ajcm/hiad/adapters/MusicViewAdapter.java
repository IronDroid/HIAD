package org.ajcm.hiad.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import org.ajcm.hiad.R;
import org.ajcm.hiad.dataset.DBAdapter;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhonlimaster on 21-07-16.
 */
public class MusicViewAdapter extends RecyclerView.Adapter<MusicViewAdapter.ViewHolder> {

    private static final String TAG = "MusicViewAdapter";
    private Context context;
    private int param;
    private ArrayList<Himno2008> himnosDescargados;
    private ArrayList<Himno2008> himnosPendientes;
    private FirebaseStorage storage;
    private ArrayList<Integer> himnosDownloaded;

    public MusicViewAdapter(Context context, int param, ArrayList<Himno2008> himnosDescargados, ArrayList<Himno2008> himnosPendientes) {
        this.context = context;
        this.param = param;
        this.himnosDescargados = himnosDescargados;
        this.himnosPendientes = himnosPendientes;
        himnosDownloaded = FileUtils.getHimnosDownloaded(context);
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public MusicViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MusicViewAdapter.ViewHolder holder, int position) {
        final Himno2008 himno = (param == 0) ? himnosDescargados.get(position) : himnosPendientes.get(position);
        holder.musicNumber.setText(String.valueOf(himno.getNumero()));
        holder.musicTitle.setText(himno.getTitulo());
        holder.musicSize.setText(FileUtils.humanReadableByteCount(himno.getFileSize()));
        if (himnosDownloaded.contains(himno.getNumero())) {
            holder.musicSize.setText(FileUtils.humanReadableByteCount(himno.getFileSize()) + " - Descargado");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return param == 0 ? himnosDescargados.size() : himnosPendientes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView musicNumber;
        public TextView musicTitle;
        public TextView musicSize;
        public TextView musicProcent;
        public ProgressBar progressBar;
        public Himno item;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            musicNumber = (TextView) view.findViewById(R.id.music_number);
            musicTitle = (TextView) view.findViewById(R.id.music_title);
            musicSize = (TextView) view.findViewById(R.id.music_size);
            musicProcent = (TextView) view.findViewById(R.id.music_procent);
            progressBar = (ProgressBar) view.findViewById(R.id.music_progress);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + musicTitle.getText() + "'";
        }
    }
}
