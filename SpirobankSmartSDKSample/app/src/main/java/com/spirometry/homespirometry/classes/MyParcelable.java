package com.spirometry.homespirometry.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedList;

public class MyParcelable implements Parcelable {
    private int mData;
    //private DeviceInfo discoveredDeviceInfo;
    private ArrayList DeviceInfoArray;
    private int[] surveyAnswers; // will only consist of 0 or 1s because all yes or no questions. 19 Questions
    private String[][] blowDeviceResultArray;
    private String[][] blowDeviceResultArrayPefFev1;
    private int[] questionStates;
    private LinkedList pulseData;
    private int lowestSat;
    private int minHeartRate;
    private int maxHeartRate;
    private int timeAbnormal;
    private int timeMinRate;
    // normal range
    private float minNRange;
    private float maxNRange;
    private boolean symptomsExist;
    private boolean varianceExists;
    // patient id
    private String patient_id;


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
        out.writeList(DeviceInfoArray);
        out.writeList(pulseData);
        out.writeInt(lowestSat);
        out.writeInt(minHeartRate);
        out.writeInt(maxHeartRate);
        out.writeInt(timeAbnormal);
        out.writeInt(timeMinRate);

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

    public void setPulseData(LinkedList<String[]> list) { pulseData = list; }
    public LinkedList<String[]> getPulseData() { return pulseData; }

    public void setSurveyAnswers(int index, int value) { surveyAnswers[index] = value; }
    public int getSurveyAnswers(int index) { return surveyAnswers[index]; }

    public void setQuestionStates(int index, int value) { questionStates[index] = value; }
    public int getQuestionStates(int index) { return questionStates[index]; }

    public void setSymptomsExist(boolean value){symptomsExist = value;}
    public boolean getSymptomsExist(){return symptomsExist;}

    public void setVarianceExists(boolean value){varianceExists = value;}
    public boolean getVarianceExists(){return varianceExists;}

   public void setBlowDataArray(int index, String[] blowPoints){
       blowDeviceResultArray[index] = blowPoints;
    }

    public float getMaxFev1() {
        float max = 0;
        for(String[] blow: blowDeviceResultArray){
            Float fev1 = Float.parseFloat(blow[1]);
            if (fev1 > max) {
                max = fev1;
            }
        }
        return max;

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

    public int[] getSurveyAnswerArr() { return surveyAnswers; }

    public void setLowestSat(int i) { lowestSat = i; }
    public int getLowestSat() { return lowestSat; }
    public void setMinHeartrate(int i) { minHeartRate = i; }
    public int getMinHeartrate() { return minHeartRate; }
    public void setMaxHeartrate(int i) { maxHeartRate = i; }
    public int getMaxHeartrate() { return maxHeartRate; }
    public void setTimeAbnormal(int i) { timeAbnormal = i; }
    public int getTimeAbnormal() { return timeAbnormal; }
    public void setTimeMinRate(int i) { timeMinRate = i; }
    public int getTimeMinRate() { return timeMinRate; }

    public void setMinNRange(float minNR) {
        minNRange = minNR;
    }
    public void setMaxNRange(float maxNR) {
        maxNRange = maxNR;
    }
    public float getMinNRange() {
        return minNRange;
    }
    public float getMaxNRange() {
        return maxNRange;
    }
    public void setPid(String pid) {
        patient_id = pid;
    }
    public String getPid() {
        return patient_id;
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

        pulseData = new LinkedList<String[]>();
        in.readList(pulseData,null);

        lowestSat = in.readInt();
        minHeartRate = in.readInt();
        maxHeartRate = in.readInt();
        timeAbnormal = in.readInt();
        timeMinRate = in.readInt();
        symptomsExist = false;
        varianceExists = false;

        minNRange = in.readFloat();
        maxNRange = in.readFloat();

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
        pulseData = new LinkedList<String[]>();
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

