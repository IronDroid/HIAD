package org.ajcm.hiad.adapters;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;

import org.ajcm.hiad.R;
import org.ajcm.hiad.activities.MusicActivity;
import org.ajcm.hiad.models.Himno;
import org.ajcm.hiad.models.Himno2008;
import org.ajcm.hiad.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by jhonlimaster on 21-07-16.
 */
public class MusicViewAdapter extends RecyclerView.Adapter<MusicViewAdapter.ViewHolder> {

    private static final String TAG = "MusicViewAdapter";
    private MusicActivity context;
    private int param;
    private ArrayList<Himno2008> himnosDescargados;
    private ArrayList<Himno2008> himnosPendientes;
    private FirebaseStorage storage;
    private ArrayList<Integer> himnosDownloaded;

    public MusicViewAdapter(FragmentActivity context, int param, ArrayList<Himno2008> himnosDescargados, ArrayList<Himno2008> himnosPendientes) {
        this.context = (MusicActivity) context;
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
    public void onBindViewHolder(final MusicViewAdapter.ViewHolder holder, final int position) {
        final Himno2008 himno = (param == 0) ? himnosDescargados.get(position) : himnosPendientes.get(position);
        holder.musicNumber.setText(String.valueOf(himno.getNumero()));
        holder.musicTitle.setText(himno.getTitulo());
        holder.musicSize.setText(FileUtils.humanReadableByteCount(himno.getFileSize()));
        if (param == 0){
            holder.icAction.setImageResource(R.drawable.ic_delete_black_24dp);
        } else{
            holder.icAction.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.setResult(RESULT_OK, new Intent().putExtra("numero", himno.getNumero()));
                context.finish();
            }
        });
        holder.icAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = FileUtils.getStringNumber(himno.getNumero());
                File dirHimnos = new File(context.getFilesDir().getAbsolutePath() + "/himnos/");
                File file = new File(dirHimnos.getAbsolutePath() + "/" + number + ".ogg");
                Log.e(TAG, "on Click Delete: " + file.delete());
                himnosDescargados.remove(position);
                notifyDataSetChanged();
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
        public ImageButton icAction;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            musicNumber = (TextView) view.findViewById(R.id.music_number);
            musicTitle = (TextView) view.findViewById(R.id.music_title);
            musicSize = (TextView) view.findViewById(R.id.music_size);
            musicProcent = (TextView) view.findViewById(R.id.music_procent);
            progressBar = (ProgressBar) view.findViewById(R.id.music_progress);
            icAction = (ImageButton) view.findViewById(R.id.ic_action_music);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + musicTitle.getText() + "'";
        }
    }
}
