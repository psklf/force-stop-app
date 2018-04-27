package com.psklf.forcestop;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by zhuyuanxuan on 15/02/2017.
 * ForceStop
 */

public class AppServiceInfo implements Parcelable {
    private ArrayList<ComponentName> mServiceList;
    private ApplicationInfo mApplicationInfo;
    private String mPackageName;

    public AppServiceInfo(String mPackageName, ApplicationInfo mApplicationInfo) {
        this.mPackageName = mPackageName;
        this.mApplicationInfo = mApplicationInfo;
    }

    public ArrayList<ComponentName> getServiceList() {
        return mServiceList;
    }

    public void addService(ComponentName service) {
        mServiceList.add(service);
    }

    public void initServiceList(ComponentName service) {
        this.mServiceList = new ArrayList<>();
        this.mServiceList.add(service);
    }

    public ApplicationInfo getApplicationInfo() {
        return mApplicationInfo;
    }

    public void setApplicationInfo(ApplicationInfo applicationInfo) {
        this.mApplicationInfo = applicationInfo;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mServiceList);
        dest.writeParcelable(this.mApplicationInfo, flags);
        dest.writeString(this.mPackageName);
    }

    protected AppServiceInfo(Parcel in) {
        this.mServiceList = in.createTypedArrayList(ComponentName.CREATOR);
        this.mApplicationInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
        this.mPackageName = in.readString();
    }

    public static final Parcelable.Creator<AppServiceInfo> CREATOR = new Parcelable
            .Creator<AppServiceInfo>() {
        @Override
        public AppServiceInfo createFromParcel(Parcel source) {
            return new AppServiceInfo(source);
        }

        @Override
        public AppServiceInfo[] newArray(int size) {
            return new AppServiceInfo[size];
        }
    };
}
