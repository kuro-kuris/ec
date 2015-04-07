package com.example.edibus;

import android.app.ListActivity;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonParser extends ListActivity{

    //JSON node names for json received from server
    private static final String TAG_ROUTE = "Route";
    private static final String TAG_STOPNAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LONG = "longitude";

    //JSON array for list of stops
    JSONArray stops = null;

    ArrayList<HashMap<String, Location>> stopList;

    public ArrayList<HashMap<String, Location>> parseJson(String jsonString){

        stopList = new ArrayList<HashMap<String, Location>>();

        try {
            //create JSON object from string received from server
            JSONObject jsonObj = new JSONObject(jsonString);
            //move each stop object into the stops array
            stops = jsonObj.getJSONArray(TAG_ROUTE);
            //loop through each stop object
            for (int i = 0; i < stops.length(); i++){
                //retrieve a stop from the array of stops
                JSONObject s = stops.getJSONObject(i);
                //get the attributes of the stop; name, latitude, longitude
                String stopName = s.getString(TAG_STOPNAME);
                Double lat = s.getDouble(TAG_LAT);
                Double lon = s.getDouble(TAG_LONG);
                //initialise the hashmap for a parsed stop
                HashMap<String, Location> stop = new HashMap<String, Location>();
                //create a location object for each stop
                Location stopLocation = new Location("");
                stopLocation.setLatitude(lat);
                stopLocation.setLongitude(lon);
                //add the hashmap to the arraylist
                stop.put(stopName,stopLocation);
                stopList.add(stop);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stopList;
    }

}
