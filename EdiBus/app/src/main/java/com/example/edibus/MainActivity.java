package com.example.edibus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.CircularProgressButton;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends ActionBarActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //response from server
    TextView responseText;
    //button with progress on click
    CircularProgressButton progressButton;
    //editable bus number
    EditText busNumberEdit;
    //location tag
    private static final String TAG = "LocationActivity";
    //location refresh intervals (ms)
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    //location requesters
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    //last known location, time, and bearing
    Location mCurrentLocation;
    String mLastUpdateTime;
    float mLastBearing = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //don't automatically focus edittext
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //textview for output from server
        responseText = (TextView) findViewById(R.id.responseText);
        //button with progress upon click
        progressButton = (CircularProgressButton) findViewById(R.id.circularProgressButton);
        //editable bus number
        busNumberEdit = (EditText) findViewById(R.id.busNumberEditText);
        //set BusNumber based on the last bus number used
        busNumberEdit.setText(getLastUsedBusNumber());
        // turn on indeterminate progress
        progressButton.setIndeterminateProgressMode(true);
        // create location listener
        createLocationRequest();
        // initialise fused location api
        createGoogleApiClient();

    }

    protected synchronized void createGoogleApiClient() {
        // create google play fused location client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //remove focus from editText when mainlayout is touched
    public void unFocusOnClick(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }



    //Checks whether we can access the network
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void onMainClick(View view) {
        //store current bus number for later use
        storeBusNumber();
        //create a new asynctask connecting to the server
        RequestTask task = new RequestTask();
        task.execute("http://178.62.4.227/authenticate/1247438");
        //refresh last known coordinates and heading
        updateLocation();
    }
    //stores bus number into shared preferences
    private void storeBusNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("BusNumber", busNumberEdit.getText().toString());
        editor.apply();
    }

    private void updateLocation() {
        Log.d(TAG, "Updating location");
        // check that fused location has some value
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            float bearing;
            // if the bearing is known, update last known bearing to current known
            if (mCurrentLocation.getBearing() != 0) {
                bearing = mCurrentLocation.getBearing();
                mLastBearing = bearing;
            } else {
                // if bearing cannot be found, use last known bearing
                bearing = mLastBearing;
            }

            responseText.append(
                    "At Time: " + mLastUpdateTime + "\n" +
                    "Latitude: " + lat + "\n" +
                    "Longitude: " + lng + "\n" +
                    "Bearing: " + bearing + "\n" +
                    "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
                    "Provider: " + mCurrentLocation.getProvider());
        } else {
            Log.d(TAG, "Location not found");
        }
    }

    private String getLastUsedBusNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //gets bus number from shared pref, with 50 value in case no other value was stored before
        String name = preferences.getString("BusNumber","50");
        return name;
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


    class RequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //starts spinning the button
            progressButton.setProgress(50);
        }


        @Override
        protected String doInBackground(String... uri) {
            String responseString = null;
            if (isNetworkAvailable()) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response;
                try {
                    response = httpclient.execute(new HttpGet(uri[0]));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        responseString = out.toString();
                        out.close();
                    } else {
                        //Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return responseString;
        }



        //Happens at the end of AsynTask
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result!=null) {
                //populate textView with result from server
                responseText.setText(result);
                //trigger success button
                progressButton.setProgress(100);
                //starts new activity sending the data received from server to it
                startNewActivity(result);
            }
            else {
                //trigger failure button
                progressButton.setProgress(-1);
            }
        }

        private void startNewActivity(String result) {
            Intent intent = new Intent(MainActivity.this,NextStopsAcitivity.class);
            intent.putExtra("data","hello");
            startActivity(intent);
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
        Log.d(TAG, "Location changed");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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
}

