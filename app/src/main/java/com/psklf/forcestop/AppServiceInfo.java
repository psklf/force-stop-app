package com.psklf.forcestop;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;

import java.util.ArrayList;

/**
 * Created by zhuyuanxuan on 15/02/2017.
 * ForceStop
 */

public class AppServiceInfo {
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
}
