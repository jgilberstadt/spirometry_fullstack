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
    private String[][] blowDeviceResultArray;
    //private int numberOfArrays;
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

    }

   /* public void setDeviceInfo(DeviceInfo d){
        discoveredDeviceInfo = d;
    }

    public DeviceInfo getDeviceInfo(){
        return discoveredDeviceInfo;
    } */


    public void setDeviceInfoArray(ArrayList arr){
        DeviceInfoArray = arr;
    }

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

     //  blowDeviceResultArray = in.readStringArray();
//       for (int i =0; i < 6; i++) {
//           in.readStringArray(blowDeviceResultArray[i]);
//       }


    }

    public MyParcelable(){
        blowDeviceResultArray = new String[6][4];
        for (int i = 0; i<6; i++) {
            for (int j = 0; j < 4; j++) {
                blowDeviceResultArray[i][j] = "";
            }
        }
    }
}

