package org.ajcm.hiad.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import org.ajcm.hiad.R;

public class PartitureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partiture);
        PhotoView photoView = findViewById(R.id.photo_view);
        String url_image = "https://cdn-images-1.medium.com/max/800/1*QlX_DwTAY9Q7UDS3tFb9sg.png";
        Glide.with(this).load(url_image).into(photoView);
    }
}
