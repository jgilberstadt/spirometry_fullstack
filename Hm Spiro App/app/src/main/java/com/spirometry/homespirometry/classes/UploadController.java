package com.spirometry.homespirometry.classes;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.spirometry.homespirometry.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UploadController {

    private final static String TAG = "UploadController";
    public static void upload_PefFev1(final String pef, final String fev1, final String peftime, final String evol) {
        // Tag used to cancel the request
        String tag_string_req = "req_response";
        final boolean success = true;
        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_PEFFEV1_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Instance Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                } catch (JSONException e) {
                    // JSON errore
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to response url
                Map<String, String> params = new HashMap<String, String>();
                params.put("patient_id", SuperActivity.newBundleData.getPatientId());
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

    public static void upload_FVC(final String pef, final String fev1, final String fvc, final String fev1_fvc, final String fev6,
                               final String fef2575) {
        // Tag used to cancel the request
        String tag_string_req = "req_response";
        boolean ans = false;
        StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_FVC_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Instance Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to response url
                Map<String, String> params = new HashMap<String, String>();
                params.put("patient_id", SuperActivity.newBundleData.getPatientId());
                params.put("pef", pef);
                params.put("fev1", fev1);
                params.put("fvc", fvc);
                params.put("fev1_fvc", fev1_fvc);
                params.put("fev6", fev6);
                params.put("fef2575", fef2575);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
