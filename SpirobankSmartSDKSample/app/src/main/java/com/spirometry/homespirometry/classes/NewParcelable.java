package com.spirometry.homespirometry.classes;

import android.os.Parcel;
import android.os.Parcelable;



public class NewParcelable implements Parcelable {
    private String patient_id;

    public void setPatient_id(String pid) {
        patient_id = pid;
    }

    public String getPatient_id(String pid) {
        return patient_id;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(patient_id);
    }

    private NewParcelable(Parcel in) {
        patient_id = in.readString();
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

    }

    public int describeContents() {
        return 0;
    }

}

