package com.spirometry.homespirometry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.spirometry.homespirometry.R;

public class HelpActivity extends AppCompatActivity {

    /*

    This activity serves as the page that users see when "HELP" is pressed in the header. It gets the name of the activity that called on this one
    and uses that to display the appropriate String from the String resources. Whenever you add any new activities, make sure that you have a string
    that is named so it corresponds to the activity.

    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        //set screen always ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get name of the previous activity
        String activityName = getCallingActivity().getShortClassName();
        activityName = activityName.substring(1);
        TextView helpTV = (TextView) this.findViewById(R.id.helpTV);
        // Need to get the ID of the String in the resources folder
        int stringId = this.getResources().getIdentifier(activityName, "string", this.getPackageName());
        if(stringId == 0){
            helpTV.setText(R.string.NoActivityForHelp);
        } else {
            helpTV.setText(stringId);
        }

        // Hide HELP button
        Button helpButton = (Button) this.findViewById(R.id.helpButton);
        helpButton.setVisibility(View.GONE);
    }

    public void onClickBack(View view) {
        finish();
    }
}
