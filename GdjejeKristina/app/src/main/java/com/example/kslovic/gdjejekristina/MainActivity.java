package com.example.kslovic.gdjejekristina;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final int REQUEST_LOCATION_PERMISSION = 10;
    private static final int REQUEST_WRITE_STORAGE = 1;
    LocationListener mLocationListener;
    LocationManager mLocationManager;
    TextView tvLocation;
    GoogleMap mGoogleMap;
    MapFragment mMapFragment;
    SoundPool mSoundPool;
    boolean mLoaded = false;
    HashMap<Integer, Integer> mSoundMap = new HashMap<>();
    private GoogleMap.OnMapClickListener mCustomOnMapClickListener;
    Button bCamera;
    private String fileName = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setUpUi();
        this.loadSounds();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasLocationPermission() == false) {
            requestPermission();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.hasLocationPermission()) {
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
        String locationProvider = this.mLocationManager.getBestProvider(criteria, true);
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
        this.mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.mLocationListener = new SimpleLocationListener();
        this.mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fGoogleMap);
        this.mMapFragment.getMapAsync(this);
        this.mCustomOnMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions newMarkerOptions = new MarkerOptions();
                newMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.tujekristina));
                newMarkerOptions.title("My place");
                newMarkerOptions.snippet("I declare this my teritory!");
                newMarkerOptions.position(latLng);
                mGoogleMap.addMarker(newMarkerOptions);
                if (mLoaded == false) return;
                playSound(R.raw.sound);

            }
        };
        this.bCamera = (Button) this.findViewById(R.id.bCamera);
        bCamera.setOnClickListener(this);
    }

    private void loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mSoundPool = new SoundPool.Builder().setMaxStreams(10).build();
        } else {
            this.mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        this.mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d("Test", String.valueOf(sampleId));
                mLoaded = true;
            }
        });
        this.mSoundMap.put(R.raw.sound, this.mSoundPool.load(this, R.raw.sound, 1));

    }

    void playSound(int selectedSound) {
        int soundID = this.mSoundMap.get(selectedSound);
        this.mSoundPool.play(soundID, 1, 1, 1, 0, 1f);
    }

    private void updateLocationDisplay(Location location) {
        String message = "Geografska širina: " + location.getLatitude() + "\nGeografska dužina: " + location.getLongitude() + "\n";
        tvLocation.setText(message);
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> nearByAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (nearByAddresses.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    Address nearestAddress = nearByAddresses.get(0);
                    stringBuilder.append("Adresa: ").append(nearestAddress.getAddressLine(0)).append("\nGrad: ")
                            .append(nearestAddress.getLocality()).append("\nDržava: ")
                            .append(nearestAddress.getCountryName());
                    tvLocation.append(stringBuilder.toString());
                    fileName = nearestAddress.getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean hasLocationPermission() {
        String LocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission(this, LocationPermission);
        if (status == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permission", "Permission granted. User pressed allow.");
                    } else {
                        Log.d("Permission", "Permission not granted. User pressed deny.");
                        askForPermission();
                    }
                }
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createDirectory();

                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }

        }
    }

    private void askForPermission() {
        boolean shouldExplain = ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldExplain) {
            Log.d("Permission", "Permission should be explained, - don't show again not clicked.");
            this.displayDialog();
        } else {
            Log.d("Permission", "Permission not granted. User pressed deny and don't show again.");
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
                        Log.d("Permission", "Permission requested because of the explanation.");
                        requestPermission();
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        UiSettings uiSettings = this.mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        this.mGoogleMap.setOnMapClickListener(this.mCustomOnMapClickListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            } else {
                createDirectory();
            }
            String photoFile = fileName + ".jpg";
            createDirectory();
            File mediaStorageDir = createDirectory();
            File file = new File(mediaStorageDir, photoFile);
            int numImages = 0;
            while (file.exists()) {
                numImages++;
                photoFile = fileName + String.valueOf(numImages) + ".jpg";
                file = new File(mediaStorageDir, photoFile);
            }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.close();

            } catch (Exception error) {
                Toast.makeText(this, "Image could not be saved.",
                        Toast.LENGTH_LONG).show();
            }
            String filePath = "file:" + mediaStorageDir + "/" + photoFile;
            sendNotification(file, filePath);
        }
    }

    public File createDirectory() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Pictures");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
            }
        }
        return mediaStorageDir;
    }

    private class SimpleLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            updateLocationDisplay(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private void sendNotification(File file, String msgText) {

        Intent intentPicture = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
        intentPicture.setDataAndType(Uri.fromFile(file), "image/*");
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, intentPicture, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(true)
                .setContentTitle("Spremljena je nova slika")
                .setContentText(msgText)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentIntent(notificationPendingIntent)
                .setLights(Color.BLUE, 2000, 1000)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Notification notification = notificationBuilder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
        this.finish();
    }
}
