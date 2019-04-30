package com.cs3370.android.lrs_passengerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int REQUEST_LOCATION_PERMISSIONS = 0;

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 500;
    private FusedLocationProviderClient mClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    //Member variable
    private float mZoomLevel = 15;

    private String mPickUp;
    private String mDropOff;

    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPickUp = getIntent().getSerializableExtra("pickUp").toString();
        mDropOff = getIntent().getSerializableExtra("dropOff").toString();

        //create location request
        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(5000);
        //mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Create location callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location: locationResult.getLocations()) {
                    updateMap(location);
                }
            }
        };
        mClient = LocationServices.getFusedLocationProviderClient(this);

        mSubmit = (Button) findViewById(R.id.button);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRide();
            }
        });
    }

    private void submitRide() {
        String url = getResources().getString(R.string.server_addr) + "/api/create-request?client_id=" + Client.getInstance().get("id") +
                "&destination_address=" + getIntent().getSerializableExtra("dropOff").toString() + "&pick_up_address=" +
                getIntent().getSerializableExtra("pickUp").toString() + "&estimated_length=" +
                getIntent().getSerializableExtra("estimatedLength").toString() + "&time=" +
                getIntent().getSerializableExtra("pickUpTime").toString() + "&date=" +
                getIntent().getSerializableExtra("pickUpDate").toString() ;
        //ToDo move selected ride around in the lists of rides (will not be taken care of until we have access to the database
    }

    private void updateMap(Location location) {

        // Get locations
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Address puAddr = geoLocate(mPickUp);
        Address doAddr = geoLocate(mDropOff);
        LatLng pickUp = new LatLng(puAddr.getLatitude(), puAddr.getLongitude());
        LatLng dropOff = new LatLng(doAddr.getLatitude(), doAddr.getLongitude());

        // create the markers
        MarkerOptions myMarker = new MarkerOptions().title("Here you are!").position(myLatLng);
        myMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        MarkerOptions puMarker = new MarkerOptions().title("Pick up here!").position(pickUp);
        puMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        MarkerOptions doMarker = new MarkerOptions().title("Drop off here!").position(dropOff);
        doMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        // Remove previous marker
        mMap.clear();

        // Add new markers
        mMap.addMarker(myMarker);
        mMap.addMarker(puMarker);
        mMap.addMarker(doMarker);

        // Move and zoom to current location at the street level
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myLatLng, 15);
        mMap.animateCamera(update);

        // Zoom to previously saved level
        update = CameraUpdateFactory.newLatLngZoom(myLatLng, mZoomLevel);
        mMap.animateCamera(update);

        //create directions url for each
        String urlToPickup = getRequestUrl(myLatLng, pickUp);
        TaskRequestDirections taskRequestDirections1 = new TaskRequestDirections();
        taskRequestDirections1.execute(urlToPickup);
        String urlToDropoff = getRequestUrl(pickUp, dropOff);
        TaskRequestDirections taskRequestDirections2 = new TaskRequestDirections();
        taskRequestDirections2.execute(urlToDropoff);
    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        //Output format
        String output = "json";
        //api key
        String apiKey = "&key=AIzaSyA2ge3xTYUPagFuMb5cWtR2Sk5aoNMDir0";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param  + apiKey;

        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnections = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnections = (HttpURLConnection) url.openConnection();
            httpURLConnections.connect();

            //Get the response result
            inputStream = httpURLConnections.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnections.disconnect();
        }
        return responseString;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Save zoom level
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mMap.getCameraPosition();
                mZoomLevel =cameraPosition.zoom;
            }
        });

        // Handle marker click
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapsActivity.this, "Lat: " + marker.getPosition().latitude +
                        "\nLong: " + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        if (hasLocationPermission()) {
            mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private boolean hasLocationPermission() {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_LOCATION_PERMISSIONS);

            return false;
        }

        return true;
    }

    private Address geoLocate(String location) {
        String searchString = location;
        Address address = null;

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e) {
            Log.e("TAG", "geoLocate: IOException: " + e.getMessage());
        }
        if (list.size() > 0) {
            address = list.get(0);

            Log.d("Tag", "geoLocate: foundLocation: " + address.toString());
        }
        return address;
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            }catch (IOException e) {
                Log.d("exc", "exception caught");
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("what", "is s " + s);
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String...strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            }catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map
            Log.d("lists", "here it is " + lists);
            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for(List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    points.add(new LatLng(lat, lng));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions != null) {
                mMap.addPolyline(polylineOptions);
            }else {
                Log.d("hey", "is this on");
                Toast.makeText(getApplicationContext(), "Direction not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
