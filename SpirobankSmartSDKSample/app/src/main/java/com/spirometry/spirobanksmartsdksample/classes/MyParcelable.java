package com.spirometry.spirobanksmartsdksample.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MyParcelable implements Parcelable {
    private int mData;
    //private DeviceInfo discoveredDeviceInfo;
    private ArrayList DeviceInfoArray;
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
        out.writeList(DeviceInfoArray);
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

    public ArrayList getDeviceInfo(){
        return DeviceInfoArray;
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
    }
    public MyParcelable(){

    }
}

