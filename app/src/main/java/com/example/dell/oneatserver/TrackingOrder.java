package com.example.dell.oneatserver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
//import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.dell.oneatserver.Common.DirectionJSONParser;
import com.example.dell.oneatserver.Common.currentUser;
import com.example.dell.oneatserver.Remote.IGeoCoordinates;
import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.Locat
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int LOCATION_PERMISSION_REQUEST = 1001;
    private GoogleMap mMap;
    private Location mLastLocation;
    private GoogleApiClient mgoogleApiClient;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;
    private IGeoCoordinates mServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        mServices = currentUser.getGeoCodeService();
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            requestRuntimePermission();

        }
        else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
            }
        }
        displayLocation();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED    )
        {
            requestRuntimePermission();

        }else{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
            if(mLastLocation!=null){
                        double Latitute = mLastLocation.getLatitude();
                        double Longitude =mLastLocation.getLongitude();
                        LatLng yourLocation = new LatLng(Latitute,Longitude);
                        mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

                        //Add Marker for the order
                        System.out.println("Address: "+currentUser.currentrequest.getAddress());
                        drawRoute(yourLocation,currentUser.currentrequest.getAddress());

            }else{
               // Toast.makeText(this,"Couldn't get the Location",Toast.LENGTH_SHORT).show();
                Log.d("DEBUG","Couldn't get the Location");
            }
        }
    }

    private void drawRoute(final LatLng yourLocation, final String address) {
        System.out.println("Draw layout Address: "+address);
        System.out.println("mservice "+ mServices.getGeoCode(address));

        mServices.getGeoCode(address).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{
                    System.out.println("Draw layout Address inside: "+address);
                    System.out.println("Response Json body "+response.body().toString());
                    JSONObject jsonObject = new JSONObject(response.body().toString());
                    System.out.println("Json Object" +jsonObject);
                    String lat =((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").get("lat").toString();
                    String lng =((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").get("lng").toString();
                    System.out.println("lat "+ lat);
                    System.out.println("lng" + lng);

                    LatLng orderLocation = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.box);
                    System.out.println("Height "+bitmap.getHeight()+" Width"+bitmap.getWidth());
                    bitmap = currentUser.scaleBitmap(bitmap,100,100);
                    MarkerOptions marker =  new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .title("order of "+currentUser.currentrequest.getPhone()).position(orderLocation);
                    mMap.addMarker(marker);
                    System.out.println("Direction "+ yourLocation.latitude+", "+yourLocation.longitude+", "+orderLocation.latitude+", " +orderLocation.longitude);

                    System.out.println("mservice direction "+mServices.getDirections(yourLocation.latitude+","+yourLocation.longitude,orderLocation.latitude+","+orderLocation.longitude)
                    );
                   mServices.getDirections(yourLocation.latitude+","+yourLocation.longitude,
                                        orderLocation.latitude+","+orderLocation.longitude)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> responses) {
                                    System.out.println("Response body "+responses.body());


                                    new ParserTask().execute(responses.body().toString());
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },LOCATION_PERMISSION_REQUEST);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mgoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode  = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else{
                Toast.makeText(this,"This device not support",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

    }

    @Override
    public void onLocationChanged(Location location) {
            mLastLocation = location;
            displayLocation();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED    )
        {
            return;

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient,mLocationRequest,  this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mgoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mgoogleApiClient!=null){
            mgoogleApiClient.connect();
        }
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        ProgressDialog mDialog = new ProgressDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Waiting ...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>>routes = null;
            try{
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser=new DirectionJSONParser();

                routes =parser.parse(jObject);
            }
             catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points = null;
            PolylineOptions lineOptions = null;

            for(int i=0;i<lists.size();i++){
                points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String,String>> path = lists.get(i);
                for(int j =0;j<path.size();j++){

                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position= new LatLng(lat,lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            mMap.addPolyline(lineOptions);
        }
    }
}
