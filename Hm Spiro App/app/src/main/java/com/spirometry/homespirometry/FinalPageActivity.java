package com.spirometry.homespirometry;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.spirometry.homespirometry.classes.SuperActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class FinalPageActivity extends SuperActivity {

    public static final String FILE_NAME_ONE = "timeKeeping.txt";

    ImageView imageView2;
    TextView finalText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView must be called before super.onCreate to set the title bar correctly in the super class
        setContentView(R.layout.activity_final_page);
        super.onCreate(savedInstanceState);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        finalText = (TextView) findViewById(R.id.finalText);
// *from here erase
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME_ONE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            finalText.setText(sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally { // this will be executed even if the exception is thrown
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    // until here erase

        //Glide.with(getActivity()).load(R.raw.alarm).asGif().into(imageView2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               /* int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid); */
              //  Intent intent = new Intent(FinalPageActivity.this, ApplicationChooseActivity.class);
                //intent.putExtra("bundle-data", mBundleData);
              //  FinalPageActivity.this.startActivity(intent);
                finish();
                System.exit(0);

            }
        }, 6000);
    }
}
