package org.ajcm.hiad.activities;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.ajcm.hiad.R;
import org.ajcm.hiad.utils.GlideApp;

public class PartitureActivity extends AppCompatActivity {

    private static final String TAG = "PartitureActivity";

    private PhotoView photoView;
    private StorageReference himnoRef;
    private ContentLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partiture);
        photoView = findViewById(R.id.photo_view);
//        String url_image = "https://cdn-images-1.medium.com/max/800/1*QlX_DwTAY9Q7UDS3tFb9sg.png";

        String urlFirebase = getResources().getString(R.string.url_firebase);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.setMaxOperationRetryTimeMillis(5000);
        StorageReference reference = storage.getReferenceFromUrl(urlFirebase);
        int numeroHimno = getIntent().getExtras().getInt(MainActivity.NUMERO);

        himnoRef = reference.child("partituras/himno" + formatNumber(numeroHimno) + ".jpg");

        himnoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                progressBar.hide();
                photoView.setVisibility(View.VISIBLE);

                // hide status bar
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);

                if (task.isSuccessful()) {
                    GlideApp.with(getApplicationContext())
                            .load(task.getResult().toString())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(photoView);
                } else {
                    Toast.makeText(PartitureActivity.this, "Upps, vuelva a intentarlo.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
                Log.e(TAG, "onFailure: ");
            }
        });
        progressBar = findViewById(R.id.loading_progress_bar);
        photoView.setVisibility(View.GONE);
        progressBar.show();

//        StorageReference himnoRef = reference.child("partituras/himno" + number + ".ogg");
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
