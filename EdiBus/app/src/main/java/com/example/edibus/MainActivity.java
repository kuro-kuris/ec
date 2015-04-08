package com.example.edibus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.rey.material.widget.CheckBox;


public class MainActivity extends ActionBarActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{


    //button with progress on click
    CircularProgressButton progressButton;
    //editable bus number
    EditText busNumberEdit;
    //Text view displaying N for night busses based on time of day
    TextView nText;
    //checkBox for setting express bus
    CheckBox expressCheckBox;
    //location tag
    private static final String TAG = "LocationActivity";
    //location refresh intervals (ms)
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    //location accuracy threshold (m)
    private static final long ACCURACY_THRESH = 30;
    //location requesters
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    //last known location, time, and bearing
    Location mCurrentLocation;
    String mLastUpdateTime;
    float mLastBearing = -1;
    //backend server IP address
    private static final String API_ADDRESS = "http://178.62.140.115/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //change action bar text here, not in manifest as that would change app name as well
        setTitle(R.string.choose_bus);
        //don't automatically focus edittext
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //button with progress upon click
        progressButton = (CircularProgressButton) findViewById(R.id.circularProgressButton);
        //editable bus number
        busNumberEdit = (EditText) findViewById(R.id.busNumberEditText);
        //set BusNumber based on the last bus number used
        busNumberEdit.setText(getLastUsedBusNumber());
        // turn on indeterminate progress
        progressButton.setIndeterminateProgressMode(true);
        // set Text view for night busses
        nText = (TextView) findViewById(R.id.nTextView);
        //checkBoxFor express busses
        expressCheckBox = ( CheckBox ) findViewById( R.id.expressCheckBox );
        //attach a listener to our checkbox
        expressCheckBox.setOnCheckedChangeListener(createCheckBoxListner());
        //has to be called after check box is set, as it removes it for night busses
        setNForNightBusses();


        if (!isGPSEnabled())
            Utils.displayPromptForEnablingGPS(this);

        // create location listener
        createLocationRequest();
        // initialise fused location api
        createGoogleApiClient();



    }

    //leave N for night buses or disable it based on whether it's between 24 and 4:30 am
    private void setNForNightBusses() {
        Calendar cal = Calendar.getInstance();
        // set calendar to TODAY 04:30:00.000
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date fourAm = cal.getTime();
        //current time
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();

        //if it's more than four am disable the N
        if (now.after(fourAm)) {
            nText.setText("");

        }else {
            //remove checkbox as there are no express busses at night
            expressCheckBox.setVisibility(View.GONE);
            expressCheckBox.invalidate();
        }
    }

    //Manual listener for express checkbox, app crashes if done through XML
    private CompoundButton.OnCheckedChangeListener createCheckBoxListner() {
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set E in front of busEditText
                if (isChecked)
                    nText.setText("E");
                else
                    nText.setText("");
            }
        };
        return listener;
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

    //check whether GPS is enabled
    private boolean isGPSEnabled() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        return manager.isProviderEnabled( LocationManager.GPS_PROVIDER);
    }
    public void onMainClick(View view) {
        //store current bus number for later use
        storeBusNumber();
        //if we displayed error before
        if (progressButton.getProgress() == -1 && !isNetworkAvailable()) {
           Utils.displayPromptForEnablingInternet(this);
        }

        //create a new asynctask connecting to the server
        RequestTask task = new RequestTask();
        //compose url to send to server
        //String url = composeURL();
        //task.execute(url);

        task.execute("http://178.62.4.227/authenticate/1247438");

        //
    }
    //stores bus number into shared preferences
    private void storeBusNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("BusNumber", busNumberEdit.getText().toString());
        editor.apply();
    }

    //composes url for server call
    private String composeURL() {
        String url;
        //compose service number from N/E prefix + number
        String serviceNumber = nText.getText().toString() + busNumberEdit.getText().toString();

        //get strings of lat, long, bearing
        String lat = String.valueOf(mCurrentLocation.getLatitude());
        String lon = String.valueOf(mCurrentLocation.getLongitude());
        String bear = String.valueOf(mCurrentLocation.getBearing());
        //append server url, servicenumber, coords, bearing
        url = API_ADDRESS + "next/" + serviceNumber + "+" + lat + "+" + lon + "+" + bear;
        return url;
    }


    private void updateLocation() {
        Log.d(TAG, "Updating location");
        // check that fused location has some value
        // and that the accuracy of the location is within threshold
        if ((null != mCurrentLocation) && (mCurrentLocation.getAccuracy() <= ACCURACY_THRESH)){
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

            Log.d(TAG,
                    "At Time: " + mLastUpdateTime + "\n" +
                            "Latitude: " + lat + "\n" +
                            "Longitude: " + lng + "\n" +
                            "Bearing: " + bearing + "\n" +
                            "Accuracy: " + mCurrentLocation.getAccuracy() + "\n" +
                            "Provider: " + mCurrentLocation.getProvider());
        } else {
            Log.d(TAG, "Location not found; accuracy: "+mCurrentLocation.getAccuracy());
        }
    }

    private String getLastUsedBusNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //gets bus number from shared pref, with 50 value in case no other value was stored before
        String name = preferences.getString("BusNumber","50");
        return name;
    }



    class RequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //disable editable elements on the screen
            busNumberEdit.setEnabled(false);
            expressCheckBox.setEnabled(false);
            nText.setEnabled(false);
            //starts spinning the button
            progressButton.setProgress(50);
        }


        @Override
        protected String doInBackground(String... uri) {
            String responseString = null;
            String updatedURL;
            //do nothing until bearing gets updated
            while (mLastBearing == -1) {

            }

            //once bearing is updated, refresh URL to include current bearing
            updatedURL = composeURL();
            Log.d(TAG, "Bearing found, sending request " + updatedURL);
            /*
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
            */
            String response = "";
            if (isNetworkAvailable()){
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet(updatedURL);
                ResponseHandler<String> handler = new BasicResponseHandler();

                try {
                    response = httpclient.execute(request, handler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Received: "+response);
            }
            return response;
        }



        //Happens at the end of AsynTask
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result!=null) {
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
            //sends bus number from edittext to be displayed in action bar of next activity
            intent.putExtra("busNumber",busNumberEdit.getText().toString());
            intent.putExtra("stop1","stop1");
            intent.putExtra("stop1","stop1");
            intent.putExtra("stop1","stop1");
            startActivity(intent);
            //transition to use between these two activities
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
}

