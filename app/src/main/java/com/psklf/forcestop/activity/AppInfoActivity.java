package com.psklf.forcestop.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
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
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_appinfo);
        mTextView = (TextView) findViewById(R.id.tv_app_services);

        Intent intent = getIntent();
        AppServiceInfo appServiceInfo = intent.getParcelableExtra("info");
        ArrayList<ComponentName> serviceList = appServiceInfo.getServiceList();
        for (ComponentName name : serviceList) {
            String c = name.getClassName();
            mTextView.append(c);
            mTextView.append("\n");
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
