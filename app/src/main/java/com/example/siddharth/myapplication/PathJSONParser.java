package com.example.siddharth.myapplication;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Siddharth on 9/13/2017.
 */

public class PathJSONParser {
    public ArrayList<LatLng> parse(JSONObject jObject) {
        JSONArray jRoutes = null;
        ArrayList<LatLng> puntos = null;
        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            JSONObject route = jRoutes.getJSONObject(0);
            JSONObject overviewPolylines = route
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            MainActivity.points1 = encodedString;
            puntos = decodePoly(encodedString);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return puntos;
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        Log.i("Location", "String received: "+encoded);
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {				b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),(((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
