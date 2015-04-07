package com.example.edibus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

class Utils {
    public static void displayPromptForEnablingGPS(
            final Activity activity)
    {
        final AlertDialogWrapper.Builder builder =
                new AlertDialogWrapper.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Please enable GPS in high accuracy mode";

        builder.setMessage(message)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    public static void displayPromptForEnablingInternet(
            final Activity activity)
    {
        final AlertDialogWrapper.Builder builder =
                new AlertDialogWrapper.Builder(activity);
        final String action = Settings.ACTION_WIRELESS_SETTINGS;
        final String message = "Please connect to the Internet so we can get the next stops for you.";

        builder.setMessage(message)

                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    public static JSONObject createLocationJSON(String service, Double lat, Double lon, Float bearing){
        JSONObject locationJSON = new JSONObject();
        //create a JSON object with user's service and location info
        try {
            locationJSON.put("service",service);
            locationJSON.put("latitude",lat);
            locationJSON.put("longitude",lon);
            locationJSON.put("bearing",bearing);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return locationJSON;
    }

}