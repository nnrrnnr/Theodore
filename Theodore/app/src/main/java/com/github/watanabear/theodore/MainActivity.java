package com.github.watanabear.theodore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_KEY_REPOSITORY_NAME = "com.github.watanabear.theodore.EXTRA_KEY_REPOSITORY_NAME";

    private static final String EXTRA_KEY_SCREENSHOT = "com.github.watanabear.sampleapplication.EXTRA_KEY_SCREENSHOT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        byte[] sc = getIntent().getByteArrayExtra(EXTRA_KEY_SCREENSHOT);
        if (sc == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(sc, 0, sc.length);

        ImageView i = (ImageView) findViewById(R.id.my_image);
        i.setImageBitmap(bitmap);

        String repositoryName = getIntent().getStringExtra(EXTRA_KEY_REPOSITORY_NAME);
        if (repositoryName == null) {
            return;
        }
        TextView t = (TextView) findViewById(R.id.text_repository_name);
        t.setText(repositoryName);

    }
}
