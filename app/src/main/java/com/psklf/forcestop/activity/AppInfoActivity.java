package com.psklf.forcestop.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.psklf.forcestop.AppServiceInfo;
import com.psklf.forcestop.R;

import java.util.ArrayList;

/**
 * Created by zhuyuanxuan on 22/02/2017.
 * ForceStop
 */

public class AppInfoActivity extends Activity {
    private TextView mServices;
    private TextView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_appinfo);
        mServices = (TextView) findViewById(R.id.tv_app_services);
        mName = (TextView) findViewById(R.id.tv_app_info_name);

        PackageManager packageManager = getPackageManager();

        Intent intent = getIntent();
        AppServiceInfo appServiceInfo = intent.getParcelableExtra("info");

        // set application name
        CharSequence label = appServiceInfo.getApplicationInfo().loadLabel
                (packageManager);
        mName.setText(label);

        // show all services
        ArrayList<ComponentName> serviceList = appServiceInfo.getServiceList();
        for (ComponentName name : serviceList) {
            String c = name.getClassName();
            mServices.append(c);
            mServices.append("\n");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
