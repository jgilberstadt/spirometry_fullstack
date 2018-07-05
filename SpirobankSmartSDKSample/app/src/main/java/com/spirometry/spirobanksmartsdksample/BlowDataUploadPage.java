package com.spirometry.spirobanksmartsdksample;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.spirometry.spirobanksmartsdksample.classes.MyParcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BlowDataUploadPage extends AppCompatActivity {

    String[][] sixBlowData; //6 data storing 4 String Values; +-
   // String[][] upload2dArray; //6 data storing 4 String Values; +-
    private MyParcelable mBundleData;
    private static final String TAG = BlowActivity.class.getSimpleName();
    private String patient_id = "000000";

    TextView testingComplete;
    TextView waitComplete;
    ProgressBar completeProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blow_data_upload_page);

        mBundleData = getIntent().getParcelableExtra("bundle-data"); // we don't need to get an array, we just need to get the whole thing which is just
        sixBlowData = mBundleData.getBlowDataArray();

        Log.d(TAG, "result blow1" + sixBlowData[4][3]);
        Log.d(TAG, "result blow2" + sixBlowData[0][0]);
        Log.d(TAG, "result blow3" + sixBlowData[1][2]);
        Log.d(TAG, "result blow4" + sixBlowData[2][1]);
        Log.d(TAG, "result blow5" + sixBlowData[3][3]);

        for(int i =0; i<6; i++){
            upload_PefFev1(sixBlowData[i][0], sixBlowData[i][1], sixBlowData[i][2], sixBlowData[i][3]);
            Log.d(TAG, "sixBlowData i Value: " + i);
            Log.d(TAG, "sixBlowData" + sixBlowData[i][0]);
            Log.d(TAG, "sixBlowData" + sixBlowData[i][1]);
            Log.d(TAG, "sixBlowData" + sixBlowData[i][2]);
            Log.d(TAG, "sixBlowData" + sixBlowData[i][3]);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleIntentToTestComplete.post(runIntentToTestComplete);
            }
        }, 3000);
    }

    void upload_PefFev1(final String pef, final String fev1, final String peftime, final String evol) {
        // Tag used to cancel the request
        String tag_string_req = "req_response";
        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_PEFFEV1_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Instance Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to response url
                Map<String, String> params = new HashMap<String, String>();
                params.put("patient_id", patient_id);
                params.put("pef", pef);
                params.put("fev1", fev1);
                params.put("peftime", peftime);
                params.put("evol", evol);

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    Handler handleIntentToTestComplete = new Handler();
    Runnable runIntentToTestComplete= new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(BlowDataUploadPage.this, TestCompleteActivity.class);
            intent.putExtra("bundle-data", mBundleData);
            BlowDataUploadPage.this.startActivity(intent);
            finish();
            // tvConnecting.setText();
            //   tvConnecting.setText(success);
        }
    };
}
