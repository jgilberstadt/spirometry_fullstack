package com.spirometry.spirobanksmartsdksample.classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MyParcelable implements Parcelable {
    private int mData;
    //private DeviceInfo discoveredDeviceInfo;
    private ArrayList DeviceInfoArray;
    private int[] surveyAnswers; // will only consist of 0 or 1s because all yes or no questions. 19 Questions
    private String[][] blowDeviceResultArray;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
        out.writeList(DeviceInfoArray);
        //blowDeviceResultArray = new String[6][4];
        for (int i =0; i < 6; i++) {
            for(int j=0; j <4 ; j++) {
                out.writeString(blowDeviceResultArray[i][j]);
            }
        }

        for (int i = 0; i < 19; i++) {
            out.writeInt(surveyAnswers[i]);
        }

    }

    public void setDeviceInfoArray(ArrayList arr){
        DeviceInfoArray = arr;
    }

    public void setSurveyAnswers(int index, int value) { surveyAnswers[index] = value; }
    public int getSurveyAnswers(int index) { return surveyAnswers[index]; }


   public void setBlowDataArray(int index, String[] blowPoints){
        Log.d("PETER", blowPoints[0]);
       //Log.d("setBlowDataArray",  "" + blowDeviceResultArray);
       blowDeviceResultArray[index] = blowPoints;
    }


    public ArrayList getDeviceInfo(){
        return DeviceInfoArray;
    }

    public String[][] getBlowDataArray() {
        return blowDeviceResultArray;
    }

    public static final Creator<MyParcelable> CREATOR
            = new Creator<MyParcelable>() {
        public MyParcelable createFromParcel(Parcel in) {
            return new MyParcelable(in);
        }

        public MyParcelable[] newArray(int size) {
            return new MyParcelable[size];
        }
    };

    private MyParcelable(Parcel in) {
        mData = in.readInt();
        //discoveredDeviceInfo = (DeviceInfo)in.readValue(DeviceInfo.class.getClassLoader());
        DeviceInfoArray = in.readArrayList(null);
        blowDeviceResultArray = new String[6][4];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                blowDeviceResultArray[i][j] = in.readString();
            }
        }

        surveyAnswers = new int[19];
        for (int i = 0; i < 19; i++) {
            surveyAnswers[i] = in.readInt();
        }
    }

    public MyParcelable(){
        blowDeviceResultArray = new String[6][4];
        for (int i = 0; i<6; i++) {
            for (int j = 0; j < 4; j++) {
                blowDeviceResultArray[i][j] = "";
            }
        }

        surveyAnswers = new int[19];
        for (int i = 0; i < 19; i++) {
            surveyAnswers[i] = -1;  // default to unanswered
        }
    }
}

