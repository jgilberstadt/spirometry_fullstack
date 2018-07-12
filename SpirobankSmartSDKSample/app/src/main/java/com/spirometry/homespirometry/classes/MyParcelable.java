package com.spirometry.homespirometry.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MyParcelable implements Parcelable {
    private int mData;
    //private DeviceInfo discoveredDeviceInfo;
    private ArrayList DeviceInfoArray;
    private int[] surveyAnswers; // will only consist of 0 or 1s because all yes or no questions. 19 Questions
    private String[][] blowDeviceResultArray;
    private String[][] blowDeviceResultArrayPefFev1;
    private int[] questionStates;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
        out.writeList(DeviceInfoArray);
        //blowDeviceResultArray = new String[6][4];
        for (int i =0; i < 6; i++) {
            for(int j=0; j < 6 ; j++) {
                out.writeString(blowDeviceResultArray[i][j]);
            }
        }

        for (int i =0; i < 1; i++) {
            for(int j=0; j < 4 ; j++) {
                out.writeString(blowDeviceResultArrayPefFev1[i][j]);
            }
        }

        for (int i = 0; i < 19; i++) {
            out.writeInt(surveyAnswers[i]);
        }

        for (int i = 0; i < 4; i++) {
            out.writeInt(questionStates[i]);
        }

    }

    public void setDeviceInfoArray(ArrayList arr){
        DeviceInfoArray = arr;
    }

    public void setSurveyAnswers(int index, int value) { surveyAnswers[index] = value; }
    public int getSurveyAnswers(int index) { return surveyAnswers[index]; }

    public void setQuestionStates(int index, int value) { questionStates[index] = value; }
    public int getQuestionStates(int index) { return questionStates[index]; }

   public void setBlowDataArray(int index, String[] blowPoints){
       blowDeviceResultArray[index] = blowPoints;
    }

    public void setBlowDataArrayPefFev1(int indexPefFev1, String[] blowPointsPefFev1){
        blowDeviceResultArrayPefFev1[indexPefFev1] = blowPointsPefFev1;
    }

    public ArrayList getDeviceInfo(){
        return DeviceInfoArray;
    }

    public String[][] getBlowDataArray() {
        return blowDeviceResultArray;
    }

    public String[][] getBlowDataArrayPefFev1() {
        return blowDeviceResultArrayPefFev1;
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
        blowDeviceResultArray = new String[6][6];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {  //            for (int j = 0; j < 4; j++) {
                blowDeviceResultArray[i][j] = in.readString();
            }
        }

        blowDeviceResultArrayPefFev1 = new String[1][4];
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 4; j++) {  //            for (int j = 0; j < 4; j++) {
                blowDeviceResultArrayPefFev1[i][j] = in.readString();
            }
        }

        surveyAnswers = new int[19];
        for (int i = 0; i < 19; i++) {
            surveyAnswers[i] = in.readInt();
        }

        questionStates = new int[4];
        for (int i = 0; i < 4; i++) {
            questionStates[i] = in.readInt();
        }
    }

    public MyParcelable(){
        blowDeviceResultArray = new String[6][6]; // use to be 4
        for (int i = 0; i<6; i++) {
            for (int j = 0; j < 6; j++) {
                blowDeviceResultArray[i][j] = "";
            }
        }
        blowDeviceResultArrayPefFev1 = new String[1][4]; // use to be 4
        for (int i = 0; i<1; i++) {
            for (int j = 0; j < 4; j++) {
                blowDeviceResultArrayPefFev1[i][j] = "";
            }
        }

        surveyAnswers = new int[19];
        for (int i = 0; i < 19; i++) {
            surveyAnswers[i] = -1;  // default to unanswered
        }

        questionStates = new int[4];
        for (int i = 0; i < 4; i++) {
            questionStates[i] = -1;
        }
    }
}

