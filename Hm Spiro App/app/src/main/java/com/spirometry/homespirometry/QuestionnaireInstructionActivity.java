package com.spirometry.homespirometry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.spirometry.homespirometry.classes.MyParcelable;
import com.spirometry.homespirometry.R;
import com.spirometry.homespirometry.classes.NewParcelable;
import com.spirometry.homespirometry.classes.SuperActivity;

/**
 * Created by ASUS on 6/28/2018.
 */

public class QuestionnaireInstructionActivity  extends SuperActivity {

    NewParcelable mBundleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_questionnaire_instruction);
        super.onCreate(savedInstanceState);
        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBundleData = getIntent().getParcelableExtra("bundle-data");

    }

    public void onClickNext(View view) {
        Intent intent = new Intent(QuestionnaireInstructionActivity.this, Q1Activity.class);
        intent.putExtra("bundle-data", mBundleData);
        startActivity(intent);
    }

    public void onClickHelp(View view) {
        Intent intent = new Intent(QuestionnaireInstructionActivity.this, HelpActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, 1);
    }

}