package com.example.edibus;

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
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NextStopsAcitivity extends ActionBarActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

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
    List<Geofence> mGeofenceList;
    //geofence radius (m)
    private static final int GEOFENCE_RADIUS = 200;
    //geofence expiration timer (ms); currently set to never expire
    private static final int GEOFENCE_EXPIRATION = -1;
    //geofence transition set to trigger on exit
    private static final int GEOFENCE_TRANSITION_EXIT = 2;


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

        //retrieve static stop list
        parsedResponse = JsonParser.staticStopList.getList();

        //instantiate first 3 stops
        stopTextView1 = (TextView) findViewById(R.id.stopTextView1);
        stopTextView2 = (TextView) findViewById(R.id.stopTextView2);
        stopTextView3 = (TextView) findViewById(R.id.stopTextView3);
        //populate stop text views
        updateStopsUI();


        // create location listener
        createLocationRequest();
        // initialise fused location api
        createGoogleApiClient();
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

    protected synchronized void createGoogleApiClient() {
        // create google play fused location client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void updateLocation() {
        Log.d(TAG, "Updating location in stop tracker");
        // check that fused location has some value
        // and that the accuracy of the location is within threshold
        if ((null != mCurrentLocation) && (mCurrentLocation.getAccuracy() <= ACCURACY_THRESH)){
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            Log.d(TAG,
                    "At Time: " + mLastUpdateTime + "\n" +
                            "Latitude: " + lat + "\n" +
                            "Longitude: " + lng + "\n" +
                            "Accuracy: " + mCurrentLocation.getAccuracy() + "\n");
        } else {
            Log.d(TAG, "Location not found");
        }
    }
    protected void createLocationRequest() {
        // create location listener with specified update intervals
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //handle suspension of fused location api connection
    }

    @Override
    public void onLocationChanged(Location location) {
        //update the last known location & timestamp when the location is changed
        Log.d(TAG, "Location changed in stop tracker");
        mCurrentLocation = location;

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //handle connection of fused location provider and begin listening to location updates
        Log.d(TAG, "Fused location API connected " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        //listen to location updates from fused location provider
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Starting location updates");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //handle connection to fused location provider failing
        Log.d(TAG, "Fused location api connection failed " + connectionResult.toString());
    }

    @Override
    public void onStart() {
        // restart fused location api client when resuming app
        super.onStart();
        Log.d(TAG, "App resumed, restarting fused location api");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        // disconnect fused location api client when app paused, to refrain from needless location updates
        super.onStop();
        Log.d(TAG, "App stopped, disconnecting");
        mGoogleApiClient.disconnect();
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
            .setExpirationDuration(GEOFENCE_EXPIRATION)
            .setTransitionTypes(GEOFENCE_TRANSITION_EXIT)
            .build());
        }
        Log.d(TAG, "Built geofence objects");
    }

    private GeofencingRequest getGeofencingRequest(){
        //build geofence watcher
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //set initial trigger, if app activated while within first geofence radius
        // ENTER, EXIT, or DWELL (triggers if user stops for specified duration within radius)
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        //add list of geofence objects
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
}
