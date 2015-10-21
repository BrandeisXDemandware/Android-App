package com.demanddev.brandeis.dw;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;
    private String item = "not found";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initial setup
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Talk to a shopping assistant!", Snackbar.LENGTH_LONG )
                        .setAction("Action", null).show();
            }
        });

        final ListView listview = (ListView) findViewById(R.id.itemListView);
        String[] values = new String[] { "Adidas", "Nike", "Cross",
                "Thom Browne", "Acne", "Apolis", "Mission Workshop", "Etsy",
                "Opening Ceremony", "Red Wing"};

        final ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, values);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);


        // beacon test
        // this can be the one in MyApplication
        beaconManager = new BeaconManager(this);

        region = new Region("ranged region",
                UUID.fromString(getString(R.string.beacon_UUID)), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    List<String> places = placesNearBeacon(nearestBeacon);
                    if (places.size()!=0 && !item.equals(places.get(0))) {
                        item = places.get(0);
                        Snackbar.make(findViewById(R.id.fab), "Recommend " + item + " !", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    Log.d("Category", "Nearest places: " + places);
                }
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }


    // We have a list of target
    private static final Map<String, List<String>> PLACES_BY_BEACONS;
    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("37580:52915", new ArrayList<String>() {{
            add("Shoe");
            add("Dress");
            add("Jeans");
        }});
        placesByBeacons.put("30230:63712", new ArrayList<String>() {{
            add("Fruit");
            add("Snack");
            add("Milk");
        }});
        placesByBeacons.put("10053:63975", new ArrayList<String>() {{
            add("Phone");
            add("Laptop");
            add("Tablet");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    // get the category name
    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }



}
