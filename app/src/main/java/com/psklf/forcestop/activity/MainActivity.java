package com.psklf.forcestop.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.psklf.forcestop.AppServiceInfo;
import com.psklf.forcestop.MyRecyclerViewAdapter;
import com.psklf.forcestop.PublicConstants;
import com.psklf.forcestop.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter
        .OnRecyclerViewItemClickListener {
    private static final String TAG = "MainActivity";
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyRecyclerViewAdapter mAdapter;
    private ArrayList<AppServiceInfo> mAppServiceInfoList;
    private ExecutorService mExecutorService;
    private Handler mRecyclerViewHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide button first
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.hide();

        initSwipeRefreshLayout();

        mRecyclerViewHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PublicConstants.MSG_FORCE_STOP_APP:
                        final int position = msg.arg1;
                        stopApp(position);
                        break;
                    case PublicConstants.MSG_STOP_FINISHED:
                        final int removePosition = msg.arg1;
                        removeItem(removePosition);
                        break;
                    case PublicConstants.MSG_INIT_DATASET_FINISH:
                        initRecyclerView();
                        if (mSwipeLayout != null) {
                            mSwipeLayout.setRefreshing(false);
                        }
                        break;
                    case PublicConstants.MSG_CHECK_SERVICE_FINISH:
                        updateAdapter();
                        if (mSwipeLayout != null) {
                            mSwipeLayout.setRefreshing(false);
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        mExecutorService = Executors.newFixedThreadPool(5);

        // initiate the list after finished set refreshing status false
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                getPkgList();
                mRecyclerViewHandler.obtainMessage(PublicConstants.MSG_INIT_DATASET_FINISH)
                        .sendToTarget();
            }
        });
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

        if (id == R.id.menu_refresh) {
            mSwipeLayout.setRefreshing(true);
            refreshList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, AppServiceInfo info) {
        Intent intent = new Intent(this, AppInfoActivity.class);
        intent.putExtra("info", info);
        startActivity(intent);
    }

    private void stopApp(final int position) {
        AppServiceInfo appServiceInfo = mAppServiceInfoList.get(position);
        if (appServiceInfo == null) {
            return;
        }

        final String pkgName = appServiceInfo.getPackageName();

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (runShellCommand("am force-stop " + pkgName) != 0) {
                        Log.e(TAG, "Run command error");
                        return;
                    }

                    // use handler to notify the main thread
                    // with position info
                    Message msg = mRecyclerViewHandler.obtainMessage(PublicConstants
                            .MSG_STOP_FINISHED);
                    msg.arg1 = position;
                    msg.sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Add recycler view, this must be called in the main thread
     */
    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create a new adapter for recycler view
        mAdapter = new MyRecyclerViewAdapter(mAppServiceInfoList, mRecyclerViewHandler,
                getApplicationContext());
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Call adapter to update the source data set
     *
     * @return 1 if error.
     */
    private int updateAdapter() {
        if (mRecyclerView == null || mAdapter == null) {
            Log.i(TAG, "Recycler view null can't update");
            return 1;
        }

        mAdapter.update(mAppServiceInfoList);
        return 0;
    }

    private void initSwipeRefreshLayout() {
        mSwipeLayout = findViewById(R.id.layout_swiperefresh);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        mSwipeLayout.setRefreshing(true);
    }

    private void getPkgList() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(1000);

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
                        // Log.i(TAG, "  " + appServiceInfo.getPackageName());
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }  // end of service loop

        mRecyclerViewHandler.obtainMessage(PublicConstants.MSG_CHECK_SERVICE_FINISH).sendToTarget();
    }

    /**
     * @param command shell command
     * @return 0: all success
     * @throws Exception e
     */
    private int runShellCommand(String command) throws Exception {
        int ret = 0;
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
            Log.i(TAG, "error " + errorMsg);
            ret = 1;
        }

        // close stream
        dataOutputStream.close();
        successResult.close();
        errorResult.close();

        return ret;
    }

    private void removeItem(int removePosition) {
        // update adapter
        mAdapter.removeData(removePosition);
    }

    private void refreshList() {
        // mAdapter.removeAll();
        mAppServiceInfoList.clear();

        if (mExecutorService != null) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    getPkgList();
                }
            });

        }
    }
}
