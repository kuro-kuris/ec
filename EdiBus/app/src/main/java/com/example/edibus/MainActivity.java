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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


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
    //textview displayed whilst loading
    TextView loadingMessageText;
    public final String URL = "http://178.62.140.115/api/next/";
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
    //for testing purposes skips location check before proceeding to next screen
    public final boolean testing = true;


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
        //point cursor to the end of edit text
        busNumberEdit.setSelection(busNumberEdit.getText().length());
        //set listener for enhances accessiblity
        setBusNumberEditListeners();
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
        //textview used to display messages during loading
        loadingMessageText = (TextView) findViewById(R.id.loadingMessageTextView);


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
            nText.setContentDescription("");

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
                if (isChecked) {


                    nText.setText("E");
                    nText.setContentDescription("Express bus");
                }
                else {
                    nText.setText("");
                    nText.setContentDescription("");
                }
            }
        };
        return listener;
    }
    //Overriding methods to prevent the user to move the cursor from the last position
    //Improves usability with accessibility features
    private void setBusNumberEditListeners() {
        busNumberEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                busNumberEdit.setSelection(busNumberEdit.getText().length());
            }

        });
        busNumberEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    // code to hide the soft keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(busNumberEdit.getApplicationWindowToken(), 0);
                }
                return false;
            }
        });
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
        //if the busNumberEdit is not empty
        if (!(busNumberEdit.getText().toString().equals(null) || busNumberEdit.getText().toString().equals(""))) {
            //create a new asynctask connecting to the server
            RequestTask task = new RequestTask(this);
            task.execute(URL);
        }else
            Toast.makeText(this,"Please input bus number",Toast.LENGTH_LONG).show();
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
        String name = preferences.getString("BusNumber","22");
        return name;
    }



    class RequestTask extends AsyncTask<String, Void, String> {

        Activity activity;

        public RequestTask (Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //disable editable elements on the screen
            busNumberEdit.setEnabled(false);
            expressCheckBox.setEnabled(false);
            nText.setEnabled(false);
            //starts spinning the button
            progressButton.setProgress(50);
            //set connecting message
            loadingMessageText.setText("Trying to connect, please wait");
        }


        @Override
        protected String doInBackground(String... uri) {

            //if we are tesing we don't want to wait for location
            if (testing) {
                while (mCurrentLocation==null) {
                }
                mCurrentLocation.setLatitude(55.9443865);
                mCurrentLocation.setLongitude(-3.1868032);
                mLastBearing = 100;
            }
            String updatedURL;
            //do nothing until bearing gets updated
            while (mLastBearing == -1) {
            }

            //once bearing is updated, refresh URL to include current bearing
            updatedURL = composeURL();
            Log.d(TAG, "Bearing found, sending request " + updatedURL);

            //prepare response string
            String responseString ="";
            //String response = "";

            if (isNetworkAvailable()){
                //create httpclient, and send a HttpGet request with url containing parameters

                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response;
                try {
                    response = httpclient.execute(new HttpGet(updatedURL));
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



                //initialise List of stopName,stopLocation pairs
                List<JsonParser.Pair> parsedResponse = new ArrayList<JsonParser.Pair>();
                //parse server response into list
                parsedResponse = JsonParser.parseJson(responseString);
                //transfer parsed list into static stop list
                JsonParser.staticStopList.setList(parsedResponse);
                for (int i = 0; i < parsedResponse.size() ; i++){
                    //print received stops into log for verification/testing
                    Location parsedStop = (Location) parsedResponse.get(i).getStopLocation();
                    Log.d(TAG, "parsed: "+parsedResponse.get(i).getName() + " : "+ parsedStop.getLatitude()+", "+parsedStop.getLongitude());
                }
            }
            return responseString;
        }


        //Happens at the end of AsynTask
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("response",result);
            //remove Connecting message
            loadingMessageText.setText("");

            if (!result.equals("")) {
                //trigger success button
                progressButton.setProgress(100);
                //starts new activity sending the data received from server to it
                startNewActivity(result);
            }
            else {
                //trigger failure button
                progressButton.setProgress(-1);

                if (!isNetworkAvailable()) {
                    Utils.displayPromptForEnablingInternet(activity);
                }else {
                    Toast.makeText(activity.getApplicationContext(),"Can not connect to the server.",Toast.LENGTH_LONG).show();

                }

                //if we fail let user edit fields again
                //disable editable elements on the screen
                busNumberEdit.setEnabled(true);
                expressCheckBox.setEnabled(true);
                nText.setEnabled(true);
            }
        }

        private void startNewActivity(String result) {
            Intent intent = new Intent(MainActivity.this,NextStopsAcitivity.class);
            //sends bus number from edittext to be displayed in action bar of next activity
            intent.putExtra("busNumber",nText.getText().toString() + busNumberEdit.getText().toString());
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




