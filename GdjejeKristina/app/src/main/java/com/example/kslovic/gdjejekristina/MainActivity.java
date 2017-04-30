package com.example.kslovic.gdjejekristina;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 10;
    LocationListener mLocationListener;
    LocationManager mLocationManager;
    TextView tvLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setUpUi();
        this.mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.mLocationListener = new SimpleLocationListener();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(hasLocationPermission() == false){
            requestPermission();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(this.hasLocationPermission()){
            startTracking();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopTracking();
    }
    private void startTracking() {
        Log.d("Tracking", "Tracking started.");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvider = this.mLocationManager.getBestProvider(criteria,true);
        long minTime = 1000;
        float minDistance = 10;
        this.mLocationManager.requestLocationUpdates(locationProvider, minTime, minDistance, this.mLocationListener);
    }
    private void stopTracking() {
        Log.d("Tracking", "Tracking stopped.");
        this.mLocationManager.removeUpdates(this.mLocationListener);
    }
    private void setUpUi() {
        this.tvLocation = (TextView) this.findViewById(R.id.tvLocation);
    }
    private void updateLocationDisplay(Location location){
        String message =
                "Geografska širina: " + location.getLatitude() + "\nGeografska dužina: " + location.getLongitude() +"\n";
        tvLocation.setText(message);
        if(Geocoder.isPresent()){
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> nearByAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                if(nearByAddresses.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    Address nearestAddress = nearByAddresses.get(0);
                    stringBuilder.append("Adresa: ").append(nearestAddress.getAddressLine(0)).append("\nGrad: ")
                            .append(nearestAddress.getLocality()).append("\nDržava: ")
                            .append(nearestAddress.getCountryName());
                    tvLocation.append(stringBuilder.toString());
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
    private boolean hasLocationPermission(){
        String LocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission(this,LocationPermission);
        if(status == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }
    private void requestPermission(){
        String[] permissions = new String[]{ Manifest.permission.ACCESS_FINE_LOCATION };
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions, REQUEST_LOCATION_PERMISSION);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length >0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        Log.d("Permission","Permission granted. User pressed allow.");
                    }
                    else{
                        Log.d("Permission","Permission not granted. User pressed deny.");
                        askForPermission();
                    }
                }
        }
    }
    private void askForPermission(){
        boolean shouldExplain = ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(shouldExplain){
            Log.d("Permission","Permission should be explained, - don't show again not clicked.");
            this.displayDialog();
        }
        else{
            Log.d("Permission","Permission not granted. User pressed deny and don't show again.");
            tvLocation.setText("Sorry, we really need that permission");
        }
    }
    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Location permission")
                .setMessage("We display your location and need your permission")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission", "User declined and won't be asked again.");
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission","Permission requested because of the explanation.");
                        requestPermission();
                        dialog.cancel();
                    }
                })
                .show();
    }
    private class SimpleLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) { updateLocationDisplay(location); }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) { }
        @Override public void onProviderDisabled(String provider) {}
    }
}
