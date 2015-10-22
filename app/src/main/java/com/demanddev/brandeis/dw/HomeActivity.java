package com.demanddev.brandeis.dw;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by jingzou on 10/11/15.
 */
public class HomeActivity extends AppCompatActivity {
    private SharedPreferences mPreferences;
    private static final String TASKS_URL = "https://devweb.herokuapp.com/api/v1/tasks";
    private final static String LOGIN_API_ENDPOINT_URL = "https://devweb.herokuapp.com/api/v1/sessions.json";

    private String BeaconID;
    private BeaconManager beaconManager;
    private Region region;
    private String oldBeaconID = "";

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
                    BeaconID = String.format("B9407F30-F5F8-466E-AFF9-25556B57FE6D:%d:%d", nearestBeacon.getMajor(), nearestBeacon.getMinor());

                    if (!BeaconID.equals(oldBeaconID)) {
                        updateTasksFromAPI(LOGIN_API_ENDPOINT_URL);
                        oldBeaconID = BeaconID;
                        loadTasksFromAPI(TASKS_URL);
                    }
                }
            }
        });

        if (!mPreferences.contains("AuthToken")) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }else {
            loadTasksFromAPI(TASKS_URL);
        }


    }

    private void loadTasksFromAPI(String url) {
        LoadTask loadTask = new LoadTask(HomeActivity.this);
        loadTask.setMessageLoading("Loading tasks...");
        loadTask.execute(url + "/" + mPreferences.getString("User", "beacontest4@example.com") + "?auth_token=" + mPreferences.getString("AuthToken", ""));
    }

    private void updateTasksFromAPI(String url) {
        UpdateTask updateTask = new UpdateTask(HomeActivity.this);
        updateTask.setMessageLoading("Next Beacon...");
        updateTask.execute(url);
    }

    private class UpdateTask extends com.demanddev.brandeis.dw.UrlJsonAsyncTask {
        public UpdateTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    // add the user email and password to
                    // the params
                    userObj.put("beacon_id", BeaconID);
                    userObj.put("email", mPreferences.getString("User", "beacontest4@example.com"));
                    userObj.put("password", mPreferences.getString("Password", "secret12345"));
                    holder.put("user", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                    json.put("info", "Email and/or password are invalid. Retry!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

            return json;
        }
    }



    private class LoadTask extends com.demanddev.brandeis.dw.UrlJsonAsyncTask {
        public LoadTask(Context context) {
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
        if (mPreferences.contains("AuthToken")) {
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    beaconManager.startRanging(region);
                }
            });
        }else{
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivityForResult(intent, 0);
        }
    }


}
