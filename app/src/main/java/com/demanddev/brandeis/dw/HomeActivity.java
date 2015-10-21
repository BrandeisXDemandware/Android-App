package com.demanddev.brandeis.dw;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jingzou on 10/11/15.
 */
public class HomeActivity extends AppCompatActivity {
    private SharedPreferences mPreferences;
    private static final String TASKS_URL = "https://devweb.herokuapp.com/api/v1/tasks";

    private String beaconID;
    private String beaconRecord;
    private BeaconManager beaconManager;
    private Region region;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        beaconManager = new BeaconManager(this);

        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    beaconID = String.format("B9407F30-F5F8-466E-AFF9-25556B57FE6D:%d:%d", nearestBeacon.getMajor(), nearestBeacon.getMinor());
                }
            }
        });

    }

    private void loadTasksFromAPI(String url) {
        GetTasksTask getTasksTask = new GetTasksTask(HomeActivity.this);
        getTasksTask.setMessageLoading("Loading tasks...");
        getTasksTask.execute(url + "/" + mPreferences.getString("User", "beacontest4@example.com") + "?auth_token=" + mPreferences.getString("AuthToken", ""));
    }

    private void check() {
        System.out.println("AGAIN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Intent intent2 = new Intent(HomeActivity.this, MainActivity.class);
        startActivityForResult(intent2, 0);
        loadTasksFromAPI(TASKS_URL);
    }


    private class GetTasksTask extends com.demanddev.brandeis.dw.UrlJsonAsyncTask {
        public GetTasksTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("tasks");
                int length = jsonTasks.length();
                List<String> tasksTitles = new ArrayList<String>(length);

                for (int i = 0; i < length; i++) {
                    tasksTitles.add(jsonTasks.getJSONObject(i).getString("recommend"));
                }

                ListView tasksListView = (ListView) findViewById (R.id.tasks_list_view);
                if (tasksListView != null) {
                    tasksListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                            android.R.layout.simple_list_item_1, tasksTitles));
                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
        if (mPreferences.contains("AuthToken")) {
            loadTasksFromAPI(TASKS_URL);
            //beaconRecord = mPreferences.getString("Beacon", "");
            //System.out.println("&&&&&&&" + beaconRecord);
            //System.out.println("@@@@@" + beaconID);
            //if (!beaconRecord.equals("B9407F30-F5F8-466E-AFF9-25556B57FE6D:10053:63975")) {
                //check();
            //}
        } else {
            Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
    }


    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }
}
