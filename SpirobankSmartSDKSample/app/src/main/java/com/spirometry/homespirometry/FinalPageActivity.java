package com.spirometry.homespirometry;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.spirometry.homespirometry.R;

public class FinalPageActivity extends AppCompatActivity {

    ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_page);
        imageView2 = (ImageView) findViewById(R.id.imageView2);

        //Glide.with(getActivity()).load(R.raw.alarm).asGif().into(imageView2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               /* int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid); */
                finish();
                System.exit(0);

            }
        }, 6000);
    }
}
