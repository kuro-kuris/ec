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

    //Custom pair class to store stopName,Location values in ordered array
    public static class Pair<String,Location> {
        private String stopName;
        private Location stopLocation;

        public Pair(String stopName, Location stopLocation){
            this.stopName = stopName;
            this.stopLocation = stopLocation;
        }
        public String getName() {return stopName;}
        public Location getStopLocation() {return stopLocation;}
    }

    public static List<Pair> parseJson(String jsonString){
        JSONArray stops = new JSONArray();
        List<Pair> stopList = new ArrayList<JsonParser.Pair>();
        try {
            //create JSON object from string received from server
            JSONObject jsonObj = new JSONObject(jsonString);
            //move each stop object into the stops array
            stops = jsonObj.getJSONArray(TAG_STOPS);
            //loop through each stop object
            for (int i = 0; i < stops.length(); i++){
                Pair stopInstance;
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
                stopInstance = new Pair(stopName,stopLocation);
                //add the pair item to the list of stops
                stopList.add(stopInstance);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stopList;
    }

    public static class staticStopList{
        private static List<Pair> stopList;

        public static void setList(List<Pair> aStopList){ stopList = aStopList;}
        public static List<Pair> getList() { return stopList;}
    }

}
