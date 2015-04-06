package com.example.edibus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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


public class MainActivity extends ActionBarActivity {

    //response from server
    TextView responseText;
    //button with progress on click
    CircularProgressButton progressButton;
    //editable bus number
    EditText busNumberEdit;

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
    }
    //stores bus number into shared preferences
    private void storeBusNumber() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("BusNumber", busNumberEdit.getText().toString());
        editor.apply();
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
                //trigger success butten
                progressButton.setProgress(100);
                //starts new activity sending the data received from server to it
                startNewActivity(result);
            }
            else {
                //trigger failiure button
                progressButton.setProgress(-1);
            }
        }

        private void startNewActivity(String result) {
            Intent intent = new Intent(MainActivity.this,NextStopsAcitivity.class);
            intent.putExtra("data","hello");
            startActivity(intent);
        }


    }
}

