package org.ajcm.hiad.activities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.ajcm.hiad.R;
import org.ajcm.hiad.utils.GlideApp;

public class PartitureActivity extends AppCompatActivity implements OnCompleteListener {

    private static final String TAG = "PartitureActivity";

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partiture);
        photoView = findViewById(R.id.photo_view);
//        String url_image = "https://cdn-images-1.medium.com/max/800/1*QlX_DwTAY9Q7UDS3tFb9sg.png";

        String urlFirebase = getResources().getString(R.string.url_firebase);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReferenceFromUrl(urlFirebase);
        int numeroHimno = getIntent().getExtras().getInt(MainActivity.NUMERO);

        StorageReference himnoRef = reference.child("partituras/himno" + formatNumber(numeroHimno) + ".jpg");

        himnoRef.getDownloadUrl().addOnCompleteListener(this).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
            }
        });

//        StorageReference himnoRef = reference.child("partituras/himno" + number + ".ogg");
    }

    @Override
    public void onComplete(@NonNull Task task) {
        GlideApp.with(this)
                .load(task.getResult().toString())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photoView);
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
