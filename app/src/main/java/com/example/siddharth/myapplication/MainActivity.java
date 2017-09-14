package com.example.siddharth.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.RestrictionsManager.RESULT_ERROR;
import static com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = "MainActivity";
    public static String points1;
    GoogleMap mGoogleMap;
    GoogleApiClient googleApiClient;
    LocationManager manager;
    Location location;
    Boolean openLocationSetting = false;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public String title;
    boolean isLocationAvailable = true;
    //  public static String points;
    private double fromLatitude, fromLongitude, toLatitude, toLongitude;
    private ArrayList<LatLng> points = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        interNetcheck();

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.placeto_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                toLatitude = place.getLatLng().latitude;
                toLongitude = place.getLatLng().longitude;
                if (mGoogleMap != null)
                    mGoogleMap.clear();

                LatLng latLng = new LatLng(toLatitude, toLongitude);
                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("position To")
                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_place_black_24dp))));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                Log.i(TAG, "Place: " + place.getName());
            }


            //  points.clear();

            @Override
            public void onError(Status status) {

                Log.i(TAG, " Error Occurred To... " + status);
            }
        });


        final PlaceAutocompleteFragment autocompleteFragment1 = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.placeFrom_autocomplete_fragment);
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                fromLatitude = place.getLatLng().latitude;
                fromLongitude = place.getLatLng().longitude;
                if (mGoogleMap != null)
                    mGoogleMap.clear();

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(fromLatitude, fromLongitude))
                        .title("position From")
                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_place_black_24dp))));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fromLatitude, fromLongitude), 5));
                Log.i(TAG, "Place: " + place.getName());
//                    points.clear();

                points.add(new LatLng(fromLatitude, fromLongitude));
                points.add(new LatLng(toLatitude, toLongitude));

                ReadTask task = new ReadTask();
                task.execute(getMapsApiDirectionsUrl(points));
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "Error Occurred From..." + status);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                place.getLocale();
                place.getLatLng();
                Log.e("Tag", "Place: " + place.getAddress()
                        + place.getLatLng());
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(MainActivity.this);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_ERROR) {
                Status status = getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }

    }

    // check internet
    private boolean interNetcheck() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            Toast.makeText(MainActivity.this, "Please You are connect to Internet...", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // run time permission
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showSettingsAlert();
            } else {
                buildGoogleApiClient();
            }
        } else {
            checkLocationPermission();
        }
        //  mGoogleMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Your Location Here.")
                .snippet("")
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_place_black_24dp))));
    }

    //image vactore
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        buildGoogleApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (openLocationSetting) {
            if (googleApiClient == null) {
                buildGoogleApiClient();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // showDialog GPS
    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(getResources().getString(R.string.alert_gps_title));
        // Setting Dialog Message
        alertDialog.setMessage(getResources().getString(R.string.alert_gps_not_enable));
        // On pressing Settings button
        alertDialog.setPositiveButton(getResources().getString(R.string.alert_gps_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                openLocationSetting = true;
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton(getResources().getString(R.string.alert_gps_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location mlocation) {
        location = mlocation;

    }

    private String getMapsApiDirectionsUrl(ArrayList<LatLng> latlngs) {
        String origin = null;
        String destination = null;
        String waypoints1 = "waypoints=optimize:true|";
        StringBuilder waypoints2 = new StringBuilder("");

        for (int index = 0; index < latlngs.size(); index++) {
            if (index == 0) {
                origin = "origin=" + latlngs.get(index).latitude + "," + latlngs.get(index).longitude;
            } else if (index == latlngs.size() - 1) {
                destination = "destination=" + latlngs.get(index).latitude + "," + latlngs.get(index).longitude;
            }
        }

        String sensor = "sensor=true";
        String params = origin + "&" + destination + "&" + sensor + "&mode=driving";
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;

        Log.d("urls:", url);
        return url;
    }


    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

       @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, ArrayList<LatLng>> {

        @Override
        protected ArrayList<LatLng> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            ArrayList<LatLng> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> routes) {
            PolylineOptions polyLineOptions = new PolylineOptions();

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                polyLineOptions = polyLineOptions.add(new LatLng(routes.get(i).latitude, routes.get(i).longitude));
                polyLineOptions = polyLineOptions.width(12);
                polyLineOptions = polyLineOptions.zIndex(30);
                polyLineOptions = polyLineOptions.color(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
            }

            Polyline polyline = mGoogleMap.addPolyline(polyLineOptions);
            Log.d("POLYLINE ADDED", "YES");
        }
    }
}