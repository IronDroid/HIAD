package org.ajcm.hiad.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.ajcm.hiad.R;
import org.ajcm.hiad.utils.GlideApp;

import java.io.File;

public class PartitureActivity extends AppCompatActivity {

    private static final String TAG = "PartitureActivity";
    private static final String DOWNLOAD_PARTITURE_ACTION = "download_partiture_action";

    private PhotoView photoView;
    private StorageReference himnoRef;
    private FirebaseAnalytics firebaseAnalytics;
    private ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partiture);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

        progressBar = findViewById(R.id.loading_progress_bar);
        photoView = findViewById(R.id.photo_view);

        progressBar.show();
        photoView.setVisibility(View.GONE);

        String urlFirebase = getResources().getString(R.string.url_firebase);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.setMaxOperationRetryTimeMillis(5000);
        StorageReference reference = storage.getReferenceFromUrl(urlFirebase);
        final int numeroHimno = getIntent().getExtras().getInt(MainActivity.NUMERO);

        File dirPartiture = new File(getFilesDir().getAbsolutePath() + "/partituras/");
        dirPartiture.mkdirs();

        himnoRef = reference.child("partituras/himno" + formatNumber(numeroHimno) + ".jpg");

        final File file = new File(dirPartiture.getAbsolutePath() + "/himno" + formatNumber(numeroHimno) + ".jpg");
        if (!file.exists()) {
            himnoRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progressBar.hide();
                    photoView.setVisibility(View.VISIBLE);

                    View decorView = getWindow().getDecorView();
                    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);

                    if (file.exists()) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, formatNumber(numeroHimno));
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, DOWNLOAD_PARTITURE_ACTION);
                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);

                        GlideApp.with(getApplicationContext())
                                .load(file)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(photoView);
                    } else {
                        Toast.makeText(PartitureActivity.this, "Upps, vuelva a intentarlo.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    finish();
                    Log.e(TAG, "onFailure: ");
                }
            });
        } else {
            GlideApp.with(getApplicationContext())
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(photoView);
            photoView.setVisibility(View.VISIBLE);
            progressBar.hide();
        }
    }

    private String formatNumber(int numero) {
        String numberS = String.valueOf(numero);
        if (numberS.length() == 1) {
            numberS = "00" + numberS;
        } else if (numberS.length() == 2) {
            numberS = "0" + numberS;
        }
        return numberS;
    }
}
