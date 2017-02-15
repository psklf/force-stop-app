package com.psklf.forcestop;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> mServiceNamesList;
    private ArrayList<String> mPkgNameList;
    private ArrayList<AppServiceInfo> mAppServiceInfoList;

    private Handler mRecyclerViewHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerViewHandler = new Handler(Looper.getMainLooper()) {
            /**
             * Subclasses must implement this to receive messages.
             *
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PublicConstants.MSG_FORCE_STOP_APP:
                        stopIt(msg.arg1);
                        break;
                }
            }
        };

        getPkgList();

        initRecyclerView();

        // try {
        //     runShellCommand("am force-stop com.evomotion.ui");
        //     // runShellCommand("mkdir /sdcard/test001/");
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // String[] namesStr = mServiceNamesList.toArray(new String[mServiceNamesList.size()]);
        AppServiceInfo[] appServiceInfos = mAppServiceInfoList.toArray(
                new AppServiceInfo[mAppServiceInfoList.size()]);
        mAdapter = new MyRecyclerViewAdapter(appServiceInfos, mRecyclerViewHandler,
                getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void stopIt(int position) {
        AppServiceInfo appServiceInfo = mAppServiceInfoList.get(position);
        if (appServiceInfo == null) {
            return;
        }

        try {
            String pkgName = appServiceInfo.getPackageName();
            runShellCommand("am force-stop " + pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPkgList() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(1000);

        mServiceNamesList = new ArrayList<>();
        mPkgNameList = new ArrayList<>();
        mAppServiceInfoList = new ArrayList<>();

        // pkg manager
        PackageManager packageManager = getPackageManager();

        for (ActivityManager.RunningServiceInfo info : runningServiceInfos) {
            ApplicationInfo applicationInfo;
            try {
                String pkgName = info.service.getPackageName();
                applicationInfo = packageManager.getApplicationInfo(pkgName, 0);

                // judge if the system app
                boolean flag = false;

                if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    flag = true;
                } else if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    flag = true;
                }

                if (flag) {
                    // add to list
                    // judge if exist
                    boolean ifExist = false;
                    // CharSequence lable = applicationInfo.loadLabel(packageManager);

                    for (AppServiceInfo appServiceInfo : mAppServiceInfoList) {
                        if (appServiceInfo.getPackageName().equals(pkgName)) {
                            appServiceInfo.addService(info.service);
                            ifExist = true;
                            break;
                        }
                    }
                    if (!ifExist) {
                        AppServiceInfo appServiceInfo = new AppServiceInfo(pkgName,
                                applicationInfo);
                        appServiceInfo.initServiceList(info.service);
                        mAppServiceInfoList.add(appServiceInfo);
                        Log.i(TAG, "  " + appServiceInfo.getPackageName());
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void runShellCommand(String command) throws Exception {
        // get super user grant
        ProcessBuilder processBuilder = new ProcessBuilder("su");
        Process p = processBuilder.start();

        //
        DataOutputStream dataOutputStream = new DataOutputStream(p.getOutputStream());
        // write the command
        Log.i(TAG, "Run: " + command);
        dataOutputStream.write(command.getBytes());
        dataOutputStream.writeBytes("\n");
        dataOutputStream.writeBytes("exit\n");
        dataOutputStream.flush();

        // int result = process.waitFor();

        // get the result info
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        BufferedReader successResult = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
        BufferedReader errorResult = new BufferedReader(
                new InputStreamReader(p.getErrorStream()));
        String s;
        while ((s = successResult.readLine()) != null) {
            successMsg.append(s);
            Log.i(TAG, "success " + successMsg);
        }
        while ((s = errorResult.readLine()) != null) {
            errorMsg.append(s);
            Log.i(TAG, "error " + successMsg);
        }

        // close stream
        dataOutputStream.close();
        successResult.close();
        errorResult.close();
    }

}
