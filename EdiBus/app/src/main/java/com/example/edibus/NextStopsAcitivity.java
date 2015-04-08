package com.example.edibus;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class NextStopsAcitivity extends ActionBarActivity {

    String busNumber;

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
}
