package com.spirometry.spirobanksmartsdksample;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String activityName = getCallingActivity().getShortClassName().toString();
        activityName = activityName.substring(1);
        TextView helpTV = (TextView) this.findViewById(R.id.helpTV);
        int stringId = this.getResources().getIdentifier(activityName, "string", this.getPackageName());
        helpTV.setText(stringId);

        Button helpButton = (Button) this.findViewById(R.id.helpButton);
        helpButton.setVisibility(View.GONE);
    }

    public void onClickBack(View view) {
        finish();
    }


}
