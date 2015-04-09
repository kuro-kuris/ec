package com.example.edibus;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NextStopsAcitivity extends ActionBarActivity{

    String busNumber;

    //stops
    private TextView stopTextView1;
    private TextView stopTextView2;
    private TextView stopTextView3;
    //list with Stops
    List<JsonParser.Pair> parsedResponse;


    //location listeners
    //location tag
    private static final String TAG = "NextStopsActivity";
    //location refresh intervals (ms)
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    //location accuracy threshold (m)
    private static final long ACCURACY_THRESH = 25;
    //location requesters
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    //last known location, time, and bearing
    Location mCurrentLocation;
    String mLastUpdateTime;

    //geofence objects for detecting next bus stops
    ArrayList<Geofence> mGeofenceList;
    //geofence radius (m)
    private static final int GEOFENCE_RADIUS = 75;
    //geofence expiration timer (ms); currently set to never expire
    private static final int GEOFENCE_EXPIRATION = -1;
    //geofence transition set to trigger on exit
    private static final int GEOFENCE_TRANSITION_EXIT = 2;
    private GeofenceStore mGeofenceStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //If we are coming back to the app we want to force restart the app
        if ( savedInstanceState != null){
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_stops);
        //used to get all extras we sent from calling Activity
        Intent intent = getIntent();
        busNumber = intent.getStringExtra("busNumber");

        setTitle("Bus number : " + busNumber);
        mGeofenceList = new ArrayList<Geofence>();
        //retrieve static stop list
        List<JsonParser.Pair> parsedResponse;
        parsedResponse = JsonParser.staticStopList.getList();
        //instantiate first 3 stops
        stopTextView1 = (TextView) findViewById(R.id.stopTextView1);
        stopTextView2 = (TextView) findViewById(R.id.stopTextView2);
        stopTextView3 = (TextView) findViewById(R.id.stopTextView3);
        //populate stop text views
        updateStopsUI();
        createGeofences(parsedResponse);
        mGeofenceStore = new GeofenceStore(this,mGeofenceList);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //default on Up pressed behaviour
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //override back pressed to work like on up pressed
        NavUtils.navigateUpFromSameTask(this);
        //transition to use between these two activities
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //discard old list
        JsonParser.staticStopList.setToNull();
    }

    //Updates UI with the upcoming stops
    private void updateStopsUI() {
        //first stop
        if (parsedResponse.size()!=0) {
            String stopName = (String)parsedResponse.get(0).getName();
            stopTextView1.setText(stopName);
            //force announcing of this stop to alert user
            announceStopAfterTime(2500);
        }else {
            stopTextView1.setText("No more stops");
        }
        //second stop
        if (parsedResponse.size()>1) {
            String stopName = (String)parsedResponse.get(1).getName();
            stopTextView2.setText(stopName);
        }else {
            stopTextView2.setText("");
        }

        //third stop
        if (parsedResponse.size()>2) {
            String stopName = (String)parsedResponse.get(2).getName();
            stopTextView3.setText(stopName);
        }else {
            stopTextView3.setText("");
        }


    }

    private void announceStopAfterTime(long time) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                stopTextView1.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
        }, time);
    }


    public void createGeofences(List<JsonParser.Pair> stopsList){
        //take a list of custom pair (String stopName, Location stopLocation) objects
        //loop through each stop object in list
        for (int i = 0; i < stopsList.size(); i++){
            //take the current stop in the list
            JsonParser.Pair stop = stopsList.get(i);
            //retrieve the name and location of that stop
            String stopName = (String) stop.getName();
            Location stopLocation = new Location("");
            stopLocation = (Location) stop.getStopLocation();
            //build a new geofence
            mGeofenceList.add(new Geofence.Builder()
            //set the request ID as the stop name
            .setRequestId(stopName)
            //set the region of the geofence, expiration timer, and transition type
            .setCircularRegion(stopLocation.getLatitude(),
                    stopLocation.getLongitude(),
                    GEOFENCE_RADIUS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)
            .build());
        }
        Log.d(TAG, "Built geofence objects");
    }



}
