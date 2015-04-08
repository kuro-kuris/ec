package com.example.edibus;

import android.app.ListActivity;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser extends ListActivity{

    //JSON node names for json received from server
    private static final String TAG_STOPS = "stops";
    private static final String TAG_STOPNAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LONG = "longitude";

    //JSON array for list of stops
    JSONArray stops = null;

    //Pair list for individual parsed stops
    List<Pair> stopList;
    Pair<String,Location> stop;

    //Custom pair class to store stopName,Location values in ordered array
    public class Pair<String,Location> {
        private final String stopName;
        private final Location stopLocation;

        public Pair(String stopName, Location stopLocation){
            this.stopName = stopName;
            this.stopLocation = stopLocation;
        }
        public String getName() {return stopName;}
        public Location getStopLocation() {return stopLocation;}
    }

    public List<Pair> parseJson(String jsonString){
        try {
            //create JSON object from string received from server
            JSONObject jsonObj = new JSONObject(jsonString);
            //move each stop object into the stops array
            stops = jsonObj.getJSONArray(TAG_STOPS);
            //loop through each stop object
            for (int i = 0; i < stops.length(); i++){
                //retrieve a stop from the array of stops
                JSONObject s = stops.getJSONObject(i);
                //get the attributes of the stop; name, latitude, longitude
                String stopName = s.getString(TAG_STOPNAME);
                Double lat = s.getDouble(TAG_LAT);
                Double lon = s.getDouble(TAG_LONG);
                //create a location object for each stop
                Location stopLocation = new Location("");
                stopLocation.setLatitude(lat);
                stopLocation.setLongitude(lon);
                //create a pair item for each stop
                stop = new Pair(stopName,stopLocation);
                //add the pair item to the list of stops
                stopList.add(stop);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stopList;
    }

}
