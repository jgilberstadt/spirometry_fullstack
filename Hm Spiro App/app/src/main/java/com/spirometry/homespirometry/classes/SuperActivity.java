package com.spirometry.homespirometry.classes;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.spirometry.homespirometry.LoginActivity;
import com.spirometry.homespirometry.R;

public class SuperActivity extends AppCompatActivity {
    static protected Resources resources;
    static protected String packageName;
    static protected TextView titleTextView;
    static protected NewParcelable newBundleData;
    private static final String TAG = SuperActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        packageName = getPackageName();
        if(newBundleData == null){
            initiateParcelable();
        }
        setTitleBar();
    }
    protected void initiateParcelable(){
        newBundleData = new NewParcelable();
    }
    protected void setTitleBar(){
        int mode = newBundleData.getMode();
        titleTextView = (TextView)findViewById(R.id.titleModeTextView);
        //if mode is set
        int identifier = resources.getIdentifier("mode"+mode, "string", packageName);
        String modeText;
        if(mode == 0){
            //if mode is not set yet
            modeText = "";
        } else if(identifier == 0){
            //if mode that is out of our defined range in strings.xml
            modeText = "Mode does not exist in strings.xml";
        } else {
            modeText = resources.getText(identifier).toString();
        }
        newBundleData.setModeString(modeText);
        titleTextView.setText(modeText);
    }
}
