package com.spirometry.homespirometry.classes;

import android.os.Parcel;
import android.os.Parcelable;



public class NewParcelable implements Parcelable {
    private String patient_id;
    private String blowDeviceResultArray;
    private String blowDeviceResultArrayPefFev1;
    private String pulseData;
    private int lowestSat;
    private int minHeartRate;
    private int maxHeartRate;
    private int timeAbnormal;
    private int timeMinRate;
    // normal range
    private float minNRange;
    private float maxNRange;
    private int symptomsExist;
    private int varianceExists;

    private int[] surveyAnswers; // will only consist of 0 or 1s because all yes or no questions. 19 Questions
    private int[] questionStates;

    public void setPatient_id(String pid) { patient_id = pid; }
    public String getPatient_id() { return patient_id; }
    public void setBlowDataArrayPefFev1(String arr){
        blowDeviceResultArrayPefFev1 = arr;
    }
    public String getBlowDataArrayPefFev1() {
        return blowDeviceResultArrayPefFev1;
    }
    public void setBlowDataArray(String arr){
        blowDeviceResultArray = arr;
    }
    public String getBlowDataArray() {
        return blowDeviceResultArray;
    }
    public void setPulseData(String pd) { pulseData = pd; }
    public String getPulseData() { return pulseData; }
    public void setSymptomsExist(int value){symptomsExist = value;}
    public int getSymptomsExist(){return symptomsExist;}
    public void setVarianceExists(int value){varianceExists = value;}
    public int getVarianceExists(){return varianceExists;}
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

    public void setSurveyAnswers(int index, int value) { surveyAnswers[index] = value; }
    public int getSurveyAnswers(int index) { return surveyAnswers[index]; }
    public void setQuestionStates(int index, int value) { questionStates[index] = value; }
    public int getQuestionStates(int index) { return questionStates[index]; }
    public int[] getSurveyAnswerArr() { return surveyAnswers; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(patient_id);
        out.writeString(blowDeviceResultArray);
        out.writeString(blowDeviceResultArrayPefFev1);
        out.writeString(pulseData);
        out.writeInt(lowestSat);
        out.writeInt(minHeartRate);
        out.writeInt(maxHeartRate);
        out.writeInt(timeAbnormal);
        out.writeInt(timeMinRate);
        out.writeFloat(minNRange);
        out.writeFloat(maxNRange);
        out.writeInt(symptomsExist);
        out.writeInt(varianceExists);
        for (int i = 0; i < 19; i++) {
            out.writeInt(surveyAnswers[i]);
        }

        for (int i = 0; i < 4; i++) {
            out.writeInt(questionStates[i]);
        }
    }

    private NewParcelable(Parcel in) {
        patient_id = in.readString();
        blowDeviceResultArray = in.readString();
        blowDeviceResultArrayPefFev1 = in.readString();
        pulseData = in.readString();
        lowestSat = in.readInt();
        minHeartRate = in.readInt();
        maxHeartRate = in.readInt();
        timeAbnormal = in.readInt();
        timeMinRate = in.readInt();
        minNRange = in.readFloat();
        maxNRange = in.readFloat();
        symptomsExist = in.readInt();
        varianceExists = in.readInt();

        surveyAnswers = new int[19];
        for (int i = 0; i < 19; i++) {
            surveyAnswers[i] = in.readInt();
        }

        questionStates = new int[4];
        for (int i = 0; i < 4; i++) {
            questionStates[i] = in.readInt();
        }
    }

    public static final Creator<NewParcelable> CREATOR = new Creator<NewParcelable>() {
        public NewParcelable createFromParcel(Parcel in) {
            return new NewParcelable(in);
        }

        public NewParcelable[] newArray(int size) {
            return new NewParcelable[size];
        }
    };

    public NewParcelable(){
        surveyAnswers = new int[19];
        for (int i = 0; i < 19; i++) {
            surveyAnswers[i] = -1;  // default to unanswered
        }

        questionStates = new int[4];
        for (int i = 0; i < 4; i++) {
            questionStates[i] = -1;
        }

    }

    public int describeContents() {
        return 0;
    }

}

