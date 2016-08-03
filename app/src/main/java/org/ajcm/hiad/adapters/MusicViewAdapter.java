package org.ajcm.hiad.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
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
    private ArrayList<Himno> himnos;
    private FirebaseStorage storage;
    private ArrayList<Integer> himnosDownloaded;

    public MusicViewAdapter(Context context) {
        this.context = context;
        himnos = new ArrayList<>();
        DBAdapter dbAdapter = new DBAdapter(this.context);
        Cursor allHimno = dbAdapter.getAllHimno(false);
        while (allHimno.moveToNext()) {
            himnos.add(Himno.fromCursor(allHimno));
        }
        dbAdapter.close();

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
        final Himno himno = himnos.get(position);
        holder.musicNumber.setText(String.valueOf(himno.getNumero()));
        holder.musicTitle.setText(himno.getTitulo());
        holder.musicSize.setText(FileUtils.humanReadableByteCount(himno.getSize()));
        if (himnosDownloaded.contains(himno.getNumero())) {
            holder.musicSize.setText(FileUtils.humanReadableByteCount(himno.getSize()) + " - Descargado");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number = FileUtils.getStringNumber(himno.getNumero());

                String url = "gs://tu-himnario-adventista.appspot.com";
                StorageReference reference = storage.getReferenceFromUrl(url);
                StorageReference himnoRef = reference.child("himnos/" + number + ".ogg");

                File dirHimnos = new File(context.getFilesDir().getAbsolutePath() + "/himnos/");
                dirHimnos.mkdirs();

                File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
                if (!file.exists()) {
                    holder.musicSize.setVisibility(View.GONE);
                    holder.progressBar.setIndeterminate(true);
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.musicProcent.setVisibility(View.VISIBLE);

                    himnoRef.getFile(file).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            if (taskSnapshot.getBytesTransferred() > 1000) {
                                holder.progressBar.setProgress((int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount()));
                                holder.progressBar.setIndeterminate(false);
                            }
                            holder.musicProcent.setText(holder.progressBar.getProgress() + "%");
                            Log.e(TAG, "onProgress: " + holder.progressBar.getProgress() + "%");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            holder.musicSize.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.GONE);
                            holder.musicProcent.setVisibility(View.GONE);
                            holder.musicSize.setText(FileUtils.humanReadableByteCount(himno.getSize()) + " - Descargado");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                        }
                    });
                } else {
                    holder.musicSize.setText(FileUtils.humanReadableByteCount(himno.getSize()) + " - Descargado");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return himnos.size();
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
