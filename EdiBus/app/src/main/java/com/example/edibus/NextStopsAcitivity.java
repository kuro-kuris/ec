package com.example.edibus;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class NextStopsAcitivity extends ActionBarActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    String busNumber;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_stops);
        //used to get all extras we sent from calling Activity
        Intent intent = getIntent();
        busNumber = intent.getStringExtra("busNumber");

        setTitle("Bus number : " + busNumber);

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
}
