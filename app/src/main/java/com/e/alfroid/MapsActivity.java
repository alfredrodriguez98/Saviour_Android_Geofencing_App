package com.e.alfroid;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import Interface.IOnLoadLocationListener;



public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLoadLocationListener {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> imbl;
    private CharSequence title;
    private IOnLoadLocationListener listener;
    private DatabaseReference Coordinates;
    private Location lastLocation;
    private GeoQuery geoQuery;
   // private List imbl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener(){

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(MapsActivity.this);




                        initArea();
                        settingGeoFire();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }           //creating map_options file in menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }           //displays the menu option in map


    private void initArea() {

        Coordinates = FirebaseDatabase.getInstance()
                .getReference("IMBL coordinates")
                .child("Coordinates");

        listener = this;

        //Load from firebase


                Coordinates.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<MyLatLng> latLngList = new ArrayList<>();

                            for(DataSnapshot locationSnapShot: dataSnapshot.getChildren())
                            {
                                MyLatLng latLng=locationSnapShot.getValue(MyLatLng.class);
                                latLngList.add(latLng);

                        }

                        listener.onLoadLocationSuccess(latLngList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onLoadLocationFailed(databaseError.getMessage());

                    }
                });
                Coordinates.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Update imblc list

                        List<MyLatLng> latLngList = new ArrayList<>();

                        for(DataSnapshot locationSnapShot: dataSnapshot.getChildren())
                        {
                            MyLatLng latLng=locationSnapShot.getValue(MyLatLng.class);
                            latLngList.add(latLng);
                        }

                        listener.onLoadLocationSuccess(latLngList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

       imbl=new ArrayList<>();

       // India and Sri Lanka (Gulf of Mannar)





        imbl.add(new LatLng(09.40, 79.52));         //Kaccha Theevu
        imbl.add(new LatLng(09.39, 79.52));
        imbl.add(new LatLng(14.076228,75.211344));
        imbl.add(new LatLng(09.38, 79.52));
        imbl.add(new LatLng(09.37, 79.52));
        imbl.add(new LatLng(09.36, 79.51));
        imbl.add(new LatLng(09.35, 79.51));
        imbl.add(new LatLng(09.34, 79.51));
        imbl.add(new LatLng(09.33, 79.51));
        imbl.add(new LatLng(09.32, 79.52));
        imbl.add(new LatLng(09.31, 79.52));
        imbl.add(new LatLng(09.30, 79.52));
        imbl.add(new LatLng(09.29, 79.52));
        imbl.add(new LatLng(09.28, 79.51));
        imbl.add(new LatLng(09.27, 79.51));
        imbl.add(new LatLng(09.26, 79.51));
        imbl.add(new LatLng(09.25, 79.51));
        imbl.add(new LatLng(09.24, 79.51));
        imbl.add(new LatLng(09.23, 79.51));
        imbl.add(new LatLng(09.22, 79.51));
        imbl.add(new LatLng(09.21, 79.51));
        imbl.add(new LatLng(09.20, 79.51));
        imbl.add(new LatLng(09.19, 79.50));
        imbl.add(new LatLng(09.18, 79.51));
        imbl.add(new LatLng(09.17, 79.49));
        imbl.add(new LatLng(09.16, 79.49));
        imbl.add(new LatLng(09.15, 79.50));
        imbl.add(new LatLng(09.14, 79.50));
        imbl.add(new LatLng(09.13, 79.50));
        imbl.add(new LatLng(09.12, 79.50));
        imbl.add(new LatLng(09.11, 79.50));
        imbl.add(new LatLng(09.10, 79.50));
        imbl.add(new LatLng(09.09, 79.505));
        imbl.add(new LatLng(09.08, 79.50));
        imbl.add(new LatLng(09.07, 79.49));
        imbl.add(new LatLng(09.06, 79.49));
        imbl.add(new LatLng(09.05, 79.49));
        imbl.add(new LatLng(09.04, 79.48));
        imbl.add(new LatLng(09.03, 79.48));
        imbl.add(new LatLng(09.02, 79.48));
        imbl.add(new LatLng(09.01, 79.47));
        imbl.add(new LatLng(09.00, 79.47));
        imbl.add(new LatLng(08.99, 79.47));
        imbl.add(new LatLng(08.98, 79.47));
        imbl.add(new LatLng(08.97, 79.47));
        imbl.add(new LatLng(08.96, 79.47));
        imbl.add(new LatLng(08.95, 79.48));
        imbl.add(new LatLng(08.94, 79.48));
        imbl.add(new LatLng(08.93, 79.48));
        imbl.add(new LatLng(08.92, 79.48));
        imbl.add(new LatLng(08.91, 79.48));
        imbl.add(new LatLng(08.90, 79.47));
        imbl.add(new LatLng(08.89, 79.47));
        imbl.add(new LatLng(08.88, 79.46));
        imbl.add(new LatLng(08.86, 79.45));
        imbl.add(new LatLng(08.84, 79.44));
        imbl.add(new LatLng(08.83, 79.43));
        imbl.add(new LatLng(08.81, 79.42));
        imbl.add(new LatLng(08.80, 79.41));
        imbl.add(new LatLng(08.79, 79.40));
        imbl.add(new LatLng(08.78, 79.39));
        imbl.add(new LatLng(08.77, 79.39));
        imbl.add(new LatLng(08.76, 79.39));
        imbl.add(new LatLng(08.75, 79.38));
        imbl.add(new LatLng(08.74, 79.38));
        imbl.add(new LatLng(08.73, 79.38));
        imbl.add(new LatLng(08.72, 79.37));
        imbl.add(new LatLng(08.71, 79.37));
        imbl.add(new LatLng(08.70, 79.37));
        imbl.add(new LatLng(08.68, 79.37));
        imbl.add(new LatLng(08.67, 79.36));
        imbl.add(new LatLng(08.66, 79.36));
        imbl.add(new LatLng(08.65, 79.36));
        imbl.add(new LatLng(08.64, 79.36));
        imbl.add(new LatLng(08.63, 79.35));
        imbl.add(new LatLng(08.62, 79.35));
        imbl.add(new LatLng(08.61, 79.35));
        //Link
        //Linking kaccha theevu to Deft

        imbl.add(new LatLng(9.40,79.52));
        imbl.add(new LatLng(9.41,79.52));
        imbl.add(new LatLng(9.42,79.52));
        imbl.add(new LatLng(9.43,79.52));
        imbl.add(new LatLng(9.44,79.53));
        imbl.add(new LatLng(9.45,79.53));
        imbl.add(new LatLng(9.46,79.53));
        imbl.add(new LatLng(9.47,79.53));
        imbl.add(new LatLng(9.48,79.54));
        imbl.add(new LatLng(9.49,79.54));
        imbl.add(new LatLng(9.50,79.54));
        imbl.add(new LatLng(9.51,79.54));
        imbl.add(new LatLng(9.52,79.54));
        imbl.add(new LatLng(9.53,79.55));
        imbl.add(new LatLng(9.54,79.55));
        imbl.add(new LatLng(9.55,79.55));
        imbl.add(new LatLng(9.56,79.55));
        imbl.add(new LatLng(9.57,79.55));
        imbl.add(new LatLng(9.58,79.56));
        imbl.add(new LatLng(9.59,79.56));
        imbl.add(new LatLng(9.60,79.56));
        imbl.add(new LatLng(9.61,79.57));
        imbl.add(new LatLng(9.62,79.57));
        imbl.add(new LatLng(9.63,79.58));
        imbl.add(new LatLng(9.64,79.58));
        imbl.add(new LatLng(9.65,79.58));
        imbl.add(new LatLng(9.66,79.59));
        imbl.add(new LatLng(9.67,79.59));
        imbl.add(new LatLng(9.68,79.59));
        imbl.add(new LatLng(9.69,79.60));
        imbl.add(new LatLng(9.70,79.60));
        imbl.add(new LatLng(9.70,79.61));
        imbl.add(new LatLng(9.71,79.61));
        imbl.add(new LatLng(9.72,79.62));
        imbl.add(new LatLng(9.73,79.62));
        imbl.add(new LatLng(9.74,79.62));
        imbl.add(new LatLng(9.75,79.63));



//Bay of Bengal

        imbl.add(new LatLng(09.76, 79.63));     //Right above Deflt island
        imbl.add(new LatLng(09.76, 79.64));
        imbl.add(new LatLng(09.77, 79.65));
        imbl.add(new LatLng(09.78, 79.66));
        imbl.add(new LatLng(09.78, 79.67));
        imbl.add(new LatLng(09.79, 79.68));
        imbl.add(new LatLng(09.80, 79.69));
        imbl.add(new LatLng(09.80, 79.70));
        imbl.add(new LatLng(09.81, 79.71));
        imbl.add(new LatLng(09.82, 79.72));
        imbl.add(new LatLng(09.82, 79.73));
        imbl.add(new LatLng(09.83, 79.74));
        imbl.add(new LatLng(09.84, 79.75));
        imbl.add(new LatLng(09.84, 79.76));
        imbl.add(new LatLng(09.85, 79.77));
        imbl.add(new LatLng(09.86, 79.78));
        imbl.add(new LatLng(09.86, 79.79));
        imbl.add(new LatLng(09.87, 79.80));
        imbl.add(new LatLng(09.88, 79.81));
        imbl.add(new LatLng(09.88, 79.82));
        imbl.add(new LatLng(09.89, 79.83));
        imbl.add(new LatLng(09.90, 79.84));
        imbl.add(new LatLng(09.90, 79.85));
        imbl.add(new LatLng(09.91, 79.86));
        imbl.add(new LatLng(09.92, 79.87));
        imbl.add(new LatLng(09.92, 79.88));
        imbl.add(new LatLng(09.93, 79.89));
        imbl.add(new LatLng(09.94, 79.90));
        imbl.add(new LatLng(09.94, 79.91));
        imbl.add(new LatLng(09.95, 79.92));
        imbl.add(new LatLng(09.96, 79.93));
        imbl.add(new LatLng(09.96, 79.94));
        imbl.add(new LatLng(09.97, 79.95));
        imbl.add(new LatLng(09.98, 79.96));
        imbl.add(new LatLng(09.98, 79.97));
        imbl.add(new LatLng(09.99, 79.98));
        imbl.add(new LatLng(10.00, 79.99));
        imbl.add(new LatLng(10.00, 80.00));
        imbl.add(new LatLng(10.01, 80.01));
        imbl.add(new LatLng(10.02, 80.01));
        imbl.add(new LatLng(10.03, 80.02));
        imbl.add(new LatLng(10.04, 80.03));


        imbl.add(new LatLng(10.05, 80.03));     //Perfect upper part north SL right above Jaffna
        imbl.add(new LatLng(10.05, 80.04));
        imbl.add(new LatLng(10.06, 80.05));
        imbl.add(new LatLng(10.07, 80.06));
        imbl.add(new LatLng(10.07, 80.07));
        imbl.add(new LatLng(10.07, 80.08));
        imbl.add(new LatLng(10.08, 80.09));
        imbl.add(new LatLng(10.09, 80.10));
        imbl.add(new LatLng(10.09, 80.11));
        imbl.add(new LatLng(10.10, 80.12));
        imbl.add(new LatLng(10.10, 80.13));
        imbl.add(new LatLng(10.11, 80.14));
        imbl.add(new LatLng(10.11, 80.15));
        imbl.add(new LatLng(10.12, 80.16));
        imbl.add(new LatLng(10.12, 80.17));
        imbl.add(new LatLng(10.13, 80.18));
        imbl.add(new LatLng(10.13, 80.19));
        imbl.add(new LatLng(10.14, 80.20));
        imbl.add(new LatLng(10.14, 80.21));
        imbl.add(new LatLng(10.15, 80.22));
        imbl.add(new LatLng(10.15, 80.23));
        imbl.add(new LatLng(10.16, 80.24));
        imbl.add(new LatLng(10.16, 80.25));
        imbl.add(new LatLng(10.17, 80.26));
        imbl.add(new LatLng(10.17, 80.27));
        imbl.add(new LatLng(10.18, 80.28));
        imbl.add(new LatLng(10.18, 80.29));
        imbl.add(new LatLng(10.19, 80.30));
        imbl.add(new LatLng(10.19, 80.31));
        imbl.add(new LatLng(10.20, 80.32));
        imbl.add(new LatLng(10.20, 80.33));
        imbl.add(new LatLng(10.20, 80.34));
        imbl.add(new LatLng(10.21, 80.35));
        imbl.add(new LatLng(10.21, 80.36));
        imbl.add(new LatLng(10.22, 80.37));
        imbl.add(new LatLng(10.22, 80.38));
        imbl.add(new LatLng(10.23, 80.39));
        imbl.add(new LatLng(10.23, 80.40));
        imbl.add(new LatLng(10.24, 80.41));
        imbl.add(new LatLng(10.24, 80.42));
        imbl.add(new LatLng(10.25, 80.43));
        imbl.add(new LatLng(10.25, 80.44));
        imbl.add(new LatLng(10.26, 80.45));
        imbl.add(new LatLng(10.27, 80.45));
        imbl.add(new LatLng(10.28, 80.45));
        imbl.add(new LatLng(10.29, 80.45));
        imbl.add(new LatLng(10.30, 80.46));
        imbl.add(new LatLng(10.31, 80.46));
        imbl.add(new LatLng(10.32, 80.46));
        imbl.add(new LatLng(10.33, 80.46));
        imbl.add(new LatLng(10.34, 80.47));
        imbl.add(new LatLng(10.34, 80.48));
        imbl.add(new LatLng(10.34, 80.49));
        imbl.add(new LatLng(10.34, 80.50));
        imbl.add(new LatLng(10.35, 80.51));
        imbl.add(new LatLng(10.35, 80.52));
        imbl.add(new LatLng(10.35, 80.53));
        imbl.add(new LatLng(10.35, 80.54));
        imbl.add(new LatLng(10.35, 80.55));
        imbl.add(new LatLng(10.35, 80.56));
        imbl.add(new LatLng(10.35, 80.57));
        imbl.add(new LatLng(10.35, 80.58));
        imbl.add(new LatLng(10.35, 80.59));
        imbl.add(new LatLng(10.35, 80.60));
        imbl.add(new LatLng(10.35, 80.61));
        imbl.add(new LatLng(10.36, 80.61));
        imbl.add(new LatLng(10.36, 80.62));
        imbl.add(new LatLng(10.36, 80.63));
        imbl.add(new LatLng(10.36, 80.64));
        imbl.add(new LatLng(10.36, 80.65));
        imbl.add(new LatLng(10.36, 80.66));
        imbl.add(new LatLng(10.36, 80.67));
        imbl.add(new LatLng(10.36, 80.68));
        imbl.add(new LatLng(10.36, 80.69));
        imbl.add(new LatLng(10.36, 80.70));
        imbl.add(new LatLng(10.37, 80.71));
        imbl.add(new LatLng(10.37, 80.72));
        imbl.add(new LatLng(10.37, 80.73));
        imbl.add(new LatLng(10.37, 80.74));
        imbl.add(new LatLng(10.37, 80.75));
        imbl.add(new LatLng(10.37, 80.76));
        imbl.add(new LatLng(10.37, 80.77));
        imbl.add(new LatLng(10.37, 80.78));
        imbl.add(new LatLng(10.37, 80.79));
        imbl.add(new LatLng(10.37, 80.80));
        imbl.add(new LatLng(10.38, 80.81));
        imbl.add(new LatLng(10.38, 80.82));
        imbl.add(new LatLng(10.38, 80.83));
        imbl.add(new LatLng(10.38, 80.84));
        imbl.add(new LatLng(10.38, 80.85));
        imbl.add(new LatLng(10.38, 80.86));
        imbl.add(new LatLng(10.38, 80.87));
        imbl.add(new LatLng(10.38, 80.88));
        imbl.add(new LatLng(10.38, 80.89));
        imbl.add(new LatLng(10.38, 80.90));
        imbl.add(new LatLng(10.39, 80.91));
        imbl.add(new LatLng(10.39, 80.92));
        imbl.add(new LatLng(10.39, 80.93));
        imbl.add(new LatLng(10.39, 80.94));
        imbl.add(new LatLng(10.39, 80.95));
        imbl.add(new LatLng(10.39, 80.96));
        imbl.add(new LatLng(10.39, 80.97));
        imbl.add(new LatLng(10.39, 80.98));
        imbl.add(new LatLng(10.39, 80.99));
        imbl.add(new LatLng(10.40, 81.00));
        imbl.add(new LatLng(10.40, 81.01));
        imbl.add(new LatLng(10.41, 81.02));
        imbl.add(new LatLng(10.41, 81.02));
        imbl.add(new LatLng(10.42, 81.02));
        imbl.add(new LatLng(10.43, 81.03));
        imbl.add(new LatLng(10.43, 81.04));
        imbl.add(new LatLng(10.44, 81.05));
        imbl.add(new LatLng(10.45, 81.06));
        imbl.add(new LatLng(10.46, 81.07));
        imbl.add(new LatLng(10.47, 81.08));
        imbl.add(new LatLng(10.48, 81.09));
        imbl.add(new LatLng(10.49, 81.10));
        imbl.add(new LatLng(10.49, 81.11));
        imbl.add(new LatLng(10.50, 81.12));
        imbl.add(new LatLng(10.51, 81.13));
        imbl.add(new LatLng(10.51, 81.14));
        imbl.add(new LatLng(10.52, 81.15));
        imbl.add(new LatLng(10.53, 81.16));
        imbl.add(new LatLng(10.54, 81.17));
        imbl.add(new LatLng(10.55, 81.18));
        imbl.add(new LatLng(10.56, 81.19));
        imbl.add(new LatLng(10.57, 81.20));
        imbl.add(new LatLng(10.57, 81.21));
        imbl.add(new LatLng(10.58, 81.22));
        imbl.add(new LatLng(10.59, 81.23));
        imbl.add(new LatLng(10.60, 81.24));
        imbl.add(new LatLng(10.61, 81.25));
        imbl.add(new LatLng(10.62, 81.26));
        imbl.add(new LatLng(10.63, 81.27));
        imbl.add(new LatLng(10.64, 81.28));
        imbl.add(new LatLng(10.65, 81.29));
        imbl.add(new LatLng(10.66, 81.30));
        imbl.add(new LatLng(10.67, 81.31));
        imbl.add(new LatLng(10.68, 81.32));
        imbl.add(new LatLng(10.69, 81.33));
        imbl.add(new LatLng(10.70, 81.34));
        imbl.add(new LatLng(10.71, 81.35));
        imbl.add(new LatLng(10.72, 81.36));
        imbl.add(new LatLng(10.73, 81.37));
        imbl.add(new LatLng(10.74, 81.38));
        imbl.add(new LatLng(10.75, 81.39));
        imbl.add(new LatLng(10.76, 81.40));
        imbl.add(new LatLng(10.76, 81.41));
        imbl.add(new LatLng(10.77, 81.42));
        imbl.add(new LatLng(10.78, 81.43));
        imbl.add(new LatLng(10.79, 81.44));
        imbl.add(new LatLng(10.80, 81.45));
        imbl.add(new LatLng(10.81, 81.46));
        imbl.add(new LatLng(10.82, 81.47));
        imbl.add(new LatLng(10.83, 81.48));
        imbl.add(new LatLng(10.84, 81.49));
        imbl.add(new LatLng(10.85, 81.50));
        imbl.add(new LatLng(10.86, 81.51));
        imbl.add(new LatLng(10.86, 81.52));
        imbl.add(new LatLng(10.87, 81.52));
        imbl.add(new LatLng(10.88, 81.52));
        imbl.add(new LatLng(10.89, 81.53));
        imbl.add(new LatLng(10.90, 81.53));
        imbl.add(new LatLng(10.91, 81.54));
        imbl.add(new LatLng(10.92, 81.54));
        imbl.add(new LatLng(10.93, 81.54));
        imbl.add(new LatLng(10.94, 81.55));
        imbl.add(new LatLng(10.95, 81.55));
        imbl.add(new LatLng(10.96, 81.55));
        imbl.add(new LatLng(10.97, 81.55));
        imbl.add(new LatLng(10.98, 81.56));
        imbl.add(new LatLng(10.99, 81.56));
        imbl.add(new LatLng(11.00, 81.56));
        imbl.add(new LatLng(11.01, 81.56));
        imbl.add(new LatLng(11.02, 81.56));
        imbl.add(new LatLng(11.03, 81.57));
        imbl.add(new LatLng(11.03, 81.58));
        imbl.add(new LatLng(11.03, 81.59));
        imbl.add(new LatLng(11.03, 81.60));
        imbl.add(new LatLng(11.04, 81.61));
        imbl.add(new LatLng(11.04, 81.62));
        imbl.add(new LatLng(11.04, 81.63));
        imbl.add(new LatLng(11.04, 81.64));
        imbl.add(new LatLng(11.04, 81.65));
        imbl.add(new LatLng(11.05, 81.66));
        imbl.add(new LatLng(11.05, 81.67));
        imbl.add(new LatLng(11.05, 81.68));
        imbl.add(new LatLng(11.05, 81.69));
        imbl.add(new LatLng(11.05, 81.70));
        imbl.add(new LatLng(11.06, 81.71));
        imbl.add(new LatLng(11.06, 81.72));
        imbl.add(new LatLng(11.06, 81.73));
        imbl.add(new LatLng(11.06, 81.74));
        imbl.add(new LatLng(11.06, 81.75));
        imbl.add(new LatLng(11.07, 81.76));
        imbl.add(new LatLng(11.07, 81.77));
        imbl.add(new LatLng(11.07, 81.78));
        imbl.add(new LatLng(11.07, 81.79));
        imbl.add(new LatLng(11.07, 81.80));
        imbl.add(new LatLng(11.08, 81.81));
        imbl.add(new LatLng(11.08, 81.82));
        imbl.add(new LatLng(11.08, 81.83));
        imbl.add(new LatLng(11.08, 81.84));
        imbl.add(new LatLng(11.08, 81.85));
        imbl.add(new LatLng(11.09, 81.86));
        imbl.add(new LatLng(11.09, 81.87));
        imbl.add(new LatLng(11.09, 81.88));
        imbl.add(new LatLng(11.09, 81.89));
        imbl.add(new LatLng(11.09, 81.90));
        imbl.add(new LatLng(11.10, 81.91));
        imbl.add(new LatLng(11.10, 81.92));
        imbl.add(new LatLng(11.10, 81.93));
        imbl.add(new LatLng(11.10, 81.94));
        imbl.add(new LatLng(11.10, 81.95));
        imbl.add(new LatLng(11.11, 81.96));
        imbl.add(new LatLng(11.11, 81.97));
        imbl.add(new LatLng(11.11, 81.98));
        imbl.add(new LatLng(11.11, 81.99));
        imbl.add(new LatLng(11.11, 82.00));
        imbl.add(new LatLng(11.11, 82.01));
        imbl.add(new LatLng(11.11, 82.02));
        imbl.add(new LatLng(11.11, 82.03));
        imbl.add(new LatLng(11.11, 82.04));
        imbl.add(new LatLng(11.11, 82.05));
        imbl.add(new LatLng(11.12, 82.06));
        imbl.add(new LatLng(11.12, 82.07));
        imbl.add(new LatLng(11.12, 82.08));
        imbl.add(new LatLng(11.12, 82.09));
        imbl.add(new LatLng(11.12, 82.10));
        imbl.add(new LatLng(11.13, 82.11));
        imbl.add(new LatLng(11.13, 82.12));
        imbl.add(new LatLng(11.13, 82.13));
        imbl.add(new LatLng(11.13, 82.14));
        imbl.add(new LatLng(11.13, 82.15));
        imbl.add(new LatLng(11.14, 82.16));
        imbl.add(new LatLng(11.14, 82.17));
        imbl.add(new LatLng(11.14, 82.18));
        imbl.add(new LatLng(11.14, 82.19));
        imbl.add(new LatLng(11.14, 82.20));
        imbl.add(new LatLng(11.15, 82.21));
        imbl.add(new LatLng(11.15, 82.22));
        imbl.add(new LatLng(11.15, 82.23));
        imbl.add(new LatLng(11.15, 82.24));
        imbl.add(new LatLng(11.16, 82.24));
        imbl.add(new LatLng(11.16, 82.25));
        imbl.add(new LatLng(11.16, 82.26));
        imbl.add(new LatLng(11.16, 82.27));
        imbl.add(new LatLng(11.16, 82.28));
        imbl.add(new LatLng(11.16, 82.29));
        imbl.add(new LatLng(11.16, 82.30));
        imbl.add(new LatLng(11.17, 82.31));
        imbl.add(new LatLng(11.17, 82.32));
        imbl.add(new LatLng(11.17, 82.33));
        imbl.add(new LatLng(11.17, 82.34));
        imbl.add(new LatLng(11.17, 82.35));
        imbl.add(new LatLng(11.17, 82.36));
        imbl.add(new LatLng(11.17, 82.37));
        imbl.add(new LatLng(11.17, 82.38));
        imbl.add(new LatLng(11.17, 82.39));
        imbl.add(new LatLng(11.17, 82.40));
        imbl.add(new LatLng(11.18, 82.41));
        imbl.add(new LatLng(11.18, 82.42));
        imbl.add(new LatLng(11.18, 82.43));
        imbl.add(new LatLng(11.18, 82.44));
        imbl.add(new LatLng(11.18, 82.45));
        imbl.add(new LatLng(11.18, 82.46));
        imbl.add(new LatLng(11.18, 82.47));
        imbl.add(new LatLng(11.18, 82.48));
        imbl.add(new LatLng(11.18, 82.49));
        imbl.add(new LatLng(11.18, 82.50));
        imbl.add(new LatLng(11.19, 82.51));
        imbl.add(new LatLng(11.19, 82.52));
        imbl.add(new LatLng(11.19, 82.53));
        imbl.add(new LatLng(11.19, 82.54));
        imbl.add(new LatLng(11.19, 82.55));
        imbl.add(new LatLng(11.19, 82.56));
        imbl.add(new LatLng(11.19, 82.57));
        imbl.add(new LatLng(11.19, 82.58));
        imbl.add(new LatLng(11.19, 82.59));
        imbl.add(new LatLng(11.19, 82.60));
        imbl.add(new LatLng(11.20, 82.61));
        imbl.add(new LatLng(11.20, 82.62));
        imbl.add(new LatLng(11.20, 82.63));
        imbl.add(new LatLng(11.20, 82.64));
        imbl.add(new LatLng(11.20, 82.65));
        imbl.add(new LatLng(11.20, 82.66));
        imbl.add(new LatLng(11.20, 82.67));
        imbl.add(new LatLng(11.20, 82.68));
        imbl.add(new LatLng(11.20, 82.69));
        imbl.add(new LatLng(11.20, 82.70));
        imbl.add(new LatLng(11.21, 82.71));
        imbl.add(new LatLng(11.21, 82.72));
        imbl.add(new LatLng(11.21, 82.73));
        imbl.add(new LatLng(11.21, 82.74));
        imbl.add(new LatLng(11.21, 82.75));
        imbl.add(new LatLng(11.21, 82.76));
        imbl.add(new LatLng(11.21, 82.77));
        imbl.add(new LatLng(11.21, 82.78));
        imbl.add(new LatLng(11.21, 82.79));
        imbl.add(new LatLng(11.21, 82.80));
        imbl.add(new LatLng(11.22, 82.81));
        imbl.add(new LatLng(11.22, 82.82));
        imbl.add(new LatLng(11.22, 82.83));
        imbl.add(new LatLng(11.22, 82.84));
        imbl.add(new LatLng(11.22, 82.85));
        imbl.add(new LatLng(11.22, 82.86));
        imbl.add(new LatLng(11.22, 82.87));
        imbl.add(new LatLng(11.22, 82.88));
        imbl.add(new LatLng(11.22, 82.89));
        imbl.add(new LatLng(11.22, 82.90));
        imbl.add(new LatLng(11.22, 82.91));
        imbl.add(new LatLng(11.22, 82.92));
        imbl.add(new LatLng(11.22, 82.93));
        imbl.add(new LatLng(11.22, 82.94));
        imbl.add(new LatLng(11.22, 82.95));
        imbl.add(new LatLng(11.22, 82.96));
        imbl.add(new LatLng(11.23, 82.97));
        imbl.add(new LatLng(11.23, 82.98));
        imbl.add(new LatLng(11.23, 82.99));
        imbl.add(new LatLng(11.23, 83.00));
        imbl.add(new LatLng(11.24, 83.01));
        imbl.add(new LatLng(11.24, 83.02));
        imbl.add(new LatLng(11.24, 83.03));
        imbl.add(new LatLng(11.24, 83.04));
        imbl.add(new LatLng(11.24, 83.05));
        imbl.add(new LatLng(11.24, 83.06));
        imbl.add(new LatLng(11.24, 83.07));
        imbl.add(new LatLng(11.24, 83.08));
        imbl.add(new LatLng(11.24, 83.09));
        imbl.add(new LatLng(11.24, 83.10));
        imbl.add(new LatLng(11.25, 83.11));
        imbl.add(new LatLng(11.25, 83.12));
        imbl.add(new LatLng(11.25, 83.13));
        imbl.add(new LatLng(11.25, 83.14));
        imbl.add(new LatLng(11.25, 83.15));
        imbl.add(new LatLng(11.25, 83.16));
        imbl.add(new LatLng(11.25, 83.17));
        imbl.add(new LatLng(11.25, 83.18));
        imbl.add(new LatLng(11.25, 83.19));
        imbl.add(new LatLng(11.25, 83.20));
        imbl.add(new LatLng(11.26, 83.21));
        imbl.add(new LatLng(11.26, 83.22));         //Bay of Bengal top




        imbl.add(new LatLng( 08.61,  79.29));
        imbl.add(new LatLng( 08.59,  79.29));
        imbl.add(new LatLng( 08.58,  79.29));
        imbl.add(new LatLng( 08.57,  79.29));
        imbl.add(new LatLng( 08.56,  79.29));
        imbl.add(new LatLng( 08.55,  79.29));
        imbl.add(new LatLng( 08.54,  79.29));
        imbl.add(new LatLng( 08.53,  79.28));
        imbl.add(new LatLng( 08.52,  79.27));
        imbl.add(new LatLng( 08.51,  79.26));
        imbl.add(new LatLng( 08.50,  79.25));
        imbl.add(new LatLng( 08.49,  79.24));
        imbl.add(new LatLng( 08.48,  79.23));
        imbl.add(new LatLng( 08.47,  79.22));
        imbl.add(new LatLng( 08.46,  79.21));
        imbl.add(new LatLng( 08.45,  79.20));
        imbl.add(new LatLng( 08.44,  79.20));
        imbl.add(new LatLng( 08.43,  79.19));
        imbl.add(new LatLng( 08.42,  79.19));
        imbl.add(new LatLng( 08.41,  79.18));
        imbl.add(new LatLng( 08.40,  79.18));
        imbl.add(new LatLng( 08.39,  79.17));
        imbl.add(new LatLng( 08.38,  79.16));
        imbl.add(new LatLng( 08.37,  79.15));
        imbl.add(new LatLng( 08.36,  79.14));
        imbl.add(new LatLng( 08.35,  79.13));
        imbl.add(new LatLng( 08.34,  79.12));
        imbl.add(new LatLng( 08.33,  79.11));
        imbl.add(new LatLng( 08.32,  79.10));
        imbl.add(new LatLng( 08.31,  79.09));
        imbl.add(new LatLng( 08.30,  79.08));
        imbl.add(new LatLng( 08.29,  79.07));
        imbl.add(new LatLng( 08.28,  79.06));
        imbl.add(new LatLng( 08.27,  79.05));
        imbl.add(new LatLng( 08.26,  79.04));
        imbl.add(new LatLng( 08.25,  79.03));
        imbl.add(new LatLng( 08.24,  79.02));
        imbl.add(new LatLng( 08.23,  79.01));
        imbl.add(new LatLng( 08.22,  79.00));
        imbl.add(new LatLng( 08.21,  78.99));
        imbl.add(new LatLng( 08.20,  78.98));
        imbl.add(new LatLng( 08.19,  78.97));
        imbl.add(new LatLng( 08.18,  78.96));
        imbl.add(new LatLng( 08.17,  78.95));
        imbl.add(new LatLng( 08.16,  78.94));
        imbl.add(new LatLng( 08.15,  78.93));
        imbl.add(new LatLng( 08.14,  78.92));
        imbl.add(new LatLng( 08.13,  78.91));
        imbl.add(new LatLng( 08.12,  78.90));
        imbl.add(new LatLng( 08.11,  78.89));
        imbl.add(new LatLng( 08.10,  78.88));       //So far perfect from bottom
        imbl.add(new LatLng( 08.09,  78.87));
        imbl.add(new LatLng( 08.08,  78.86));
        imbl.add(new LatLng( 08.07,  78.85));
        imbl.add(new LatLng( 08.06,  78.84));
        imbl.add(new LatLng( 08.05,  78.83));
        imbl.add(new LatLng( 08.04,  78.82));
        imbl.add(new LatLng( 08.03,  78.81));
        imbl.add(new LatLng( 08.02,  78.80));
        imbl.add(new LatLng( 08.01,  78.79));
        imbl.add(new LatLng( 08.00,  78.78));
        imbl.add(new LatLng( 07.99,  78.77));
        imbl.add(new LatLng( 07.98,  78.76));
        imbl.add(new LatLng( 07.97,  78.75));
        imbl.add(new LatLng( 07.96,  78.74));
        imbl.add(new LatLng( 07.95,  78.73));
        imbl.add(new LatLng( 07.94,  78.72));
        imbl.add(new LatLng( 07.93,  78.71));
        imbl.add(new LatLng( 07.92,  78.70));
        imbl.add(new LatLng( 07.91,  78.69));
        imbl.add(new LatLng( 07.90,  78.68));
        imbl.add(new LatLng( 07.89,  78.67));
        imbl.add(new LatLng( 07.88,  78.66));
        imbl.add(new LatLng( 07.87,  78.65));
        imbl.add(new LatLng( 07.86,  78.64));
        imbl.add(new LatLng( 07.85,  78.63));
        imbl.add(new LatLng( 07.84,  78.62));
        imbl.add(new LatLng( 07.83,  78.61));
        imbl.add(new LatLng( 07.82,  78.60));
        imbl.add(new LatLng( 07.81,  78.59));
        imbl.add(new LatLng( 07.80,  78.58));
        imbl.add(new LatLng( 07.79,  78.57));
        imbl.add(new LatLng( 07.78,  78.56));
        imbl.add(new LatLng( 07.77,  78.55));
        imbl.add(new LatLng( 07.76,  78.54));
        imbl.add(new LatLng( 07.75,  78.53));
        imbl.add(new LatLng( 07.74,  78.52));
        imbl.add(new LatLng( 07.73,  78.51));
        imbl.add(new LatLng( 07.72,  78.50));
        imbl.add(new LatLng( 07.71,  78.49));
        imbl.add(new LatLng( 07.70,  78.48));
        imbl.add(new LatLng( 07.69,  78.47));
        imbl.add(new LatLng( 07.68,  78.46));
        imbl.add(new LatLng( 07.67,  78.45));
        imbl.add(new LatLng( 07.66,  78.44));
        imbl.add(new LatLng( 07.65,  78.43));
        imbl.add(new LatLng( 07.64,  78.42));
        imbl.add(new LatLng( 07.63,  78.41));
        imbl.add(new LatLng( 07.62,  78.40));
        imbl.add(new LatLng( 07.61,  78.39));
        imbl.add(new LatLng( 07.60,  78.38));
        imbl.add(new LatLng( 07.59,  78.37));
        imbl.add(new LatLng( 07.58,  78.36));
        imbl.add(new LatLng( 07.57,  78.35));
        imbl.add(new LatLng( 07.56,  78.34));
        imbl.add(new LatLng( 07.55,  78.33));
        imbl.add(new LatLng( 07.54,  78.32));
        imbl.add(new LatLng( 07.53,  78.31));
        imbl.add(new LatLng( 07.52,  78.30));
        imbl.add(new LatLng( 07.51,  78.29));
        imbl.add(new LatLng( 07.50,  78.28));
        imbl.add(new LatLng( 07.49,  78.27));
        imbl.add(new LatLng( 07.48,  78.26));
        imbl.add(new LatLng( 07.47,  78.25));
        imbl.add(new LatLng( 07.46,  78.24));
        imbl.add(new LatLng( 07.45,  78.23));
        imbl.add(new LatLng( 07.44,  78.22));
        imbl.add(new LatLng( 07.43,  78.21));
        imbl.add(new LatLng( 07.42,  78.20));
        imbl.add(new LatLng( 07.41,  78.19));
        imbl.add(new LatLng( 07.40,  78.18));
        imbl.add(new LatLng( 07.39,  78.18));
        imbl.add(new LatLng( 07.38,  78.18));
        imbl.add(new LatLng( 07.37,  78.17));
        imbl.add(new LatLng( 07.36,  78.17));
        imbl.add(new LatLng( 07.35,  78.17));
        imbl.add(new LatLng( 07.34,  78.16));
        imbl.add(new LatLng( 07.33,  78.16));
        imbl.add(new LatLng( 07.32,  78.16));
        imbl.add(new LatLng( 07.31,  78.15));
        imbl.add(new LatLng( 07.30,  78.15));
        imbl.add(new LatLng( 07.29,  78.15));
        imbl.add(new LatLng( 07.28,  78.14));
        imbl.add(new LatLng( 07.27,  78.14));
        imbl.add(new LatLng( 07.26,  78.14));
        imbl.add(new LatLng( 07.25,  78.13));
        imbl.add(new LatLng( 07.24,  78.13));
        imbl.add(new LatLng( 07.23,  78.13));
        imbl.add(new LatLng( 07.22,  78.13));
        imbl.add(new LatLng( 07.21,  78.13));
        imbl.add(new LatLng( 07.20,  78.13));
        imbl.add(new LatLng( 07.19,  78.12));
        imbl.add(new LatLng( 07.18,  78.12));
        imbl.add(new LatLng( 07.17,  78.12));
        imbl.add(new LatLng( 07.16,  78.12));
        imbl.add(new LatLng( 07.15,  78.12));
        imbl.add(new LatLng( 07.14,  78.12));
        imbl.add(new LatLng( 07.13,  78.12));
        imbl.add(new LatLng( 07.12,  78.12));
        imbl.add(new LatLng( 07.11,  78.12));
        imbl.add(new LatLng( 07.10,  78.12));
        imbl.add(new LatLng( 07.09,  78.12));
        imbl.add(new LatLng( 07.08,  78.12));
        imbl.add(new LatLng( 07.07,  78.12));
        imbl.add(new LatLng( 07.06,  78.12));
        imbl.add(new LatLng( 07.05,  78.12));
        imbl.add(new LatLng( 07.04,  78.12));
        imbl.add(new LatLng( 07.03,  78.12));
        imbl.add(new LatLng( 07.02,  78.12));
        imbl.add(new LatLng( 07.01,  78.12));
        imbl.add(new LatLng( 07.00,  78.12));       //So far perfect from bottom
        imbl.add(new LatLng( 06.99,  78.12));
        imbl.add(new LatLng( 06.98,  78.12));
        imbl.add(new LatLng( 06.97,  78.12));
        imbl.add(new LatLng( 06.96,  78.12));
        imbl.add(new LatLng( 06.95,  78.12));
        imbl.add(new LatLng( 06.94,  78.12));
        imbl.add(new LatLng( 06.93,  78.12));
        imbl.add(new LatLng( 06.92,  78.12));
        imbl.add(new LatLng( 06.91,  78.12));
        imbl.add(new LatLng( 06.90,  78.12));
        imbl.add(new LatLng( 06.89,  78.12));
        imbl.add(new LatLng( 06.88,  78.12));
        imbl.add(new LatLng( 06.87,  78.12));
        imbl.add(new LatLng( 06.86,  78.12));
        imbl.add(new LatLng( 06.85,  78.12));
        imbl.add(new LatLng( 06.84,  78.12));
        imbl.add(new LatLng( 06.83,  78.12));
        imbl.add(new LatLng( 06.82,  78.12));
        imbl.add(new LatLng( 06.81,  78.12));
        imbl.add(new LatLng( 06.80,  78.12));
        imbl.add(new LatLng( 06.79,  78.12));
        imbl.add(new LatLng( 06.78,  78.12));
        imbl.add(new LatLng( 06.77,  78.12));
        imbl.add(new LatLng( 06.76,  78.12));
        imbl.add(new LatLng( 06.75,  78.12));
        imbl.add(new LatLng( 06.74,  78.12));
        imbl.add(new LatLng( 06.73,  78.12));
        imbl.add(new LatLng( 06.72,  78.12));
        imbl.add(new LatLng( 06.71,  78.12));
        imbl.add(new LatLng( 06.70,  78.12));
        imbl.add(new LatLng( 06.69,  78.12));
        imbl.add(new LatLng( 06.68,  78.12));
        imbl.add(new LatLng( 06.67,  78.12));
        imbl.add(new LatLng( 06.66,  78.12));
        imbl.add(new LatLng( 06.65,  78.12));
        imbl.add(new LatLng( 06.64,  78.12));
        imbl.add(new LatLng( 06.63,  78.12));
        imbl.add(new LatLng( 06.62,  78.12));
        imbl.add(new LatLng( 06.61,  78.12));
        imbl.add(new LatLng( 06.60,  78.12));
        imbl.add(new LatLng( 06.59,  78.12));
        imbl.add(new LatLng( 06.58,  78.12));
        imbl.add(new LatLng( 06.57,  78.12));
        imbl.add(new LatLng( 06.56,  78.12));
        imbl.add(new LatLng( 06.55,  78.12));
        imbl.add(new LatLng( 06.54,  78.12));
        imbl.add(new LatLng( 06.53,  78.12));
        imbl.add(new LatLng( 06.52,  78.12));
        imbl.add(new LatLng( 06.51,  78.12));
        imbl.add(new LatLng( 06.50,  78.12));
        imbl.add(new LatLng( 06.49,  78.12));
        imbl.add(new LatLng( 06.48,  78.12));
        imbl.add(new LatLng( 06.47,  78.12));
        imbl.add(new LatLng( 06.46,  78.12));
        imbl.add(new LatLng( 06.45,  78.12));
        imbl.add(new LatLng( 06.44,  78.12));
        imbl.add(new LatLng( 06.43,  78.12));
        imbl.add(new LatLng( 06.42,  78.12));
        imbl.add(new LatLng( 06.41,  78.12));
        imbl.add(new LatLng( 06.40,  78.12));
        imbl.add(new LatLng( 06.39,  78.12));
        imbl.add(new LatLng( 06.38,  78.12));
        imbl.add(new LatLng( 06.37,  78.12));
        imbl.add(new LatLng( 06.36,  78.12));
        imbl.add(new LatLng( 06.35,  78.12));
        imbl.add(new LatLng( 06.34,  78.12));
        imbl.add(new LatLng( 06.33,  78.12));
        imbl.add(new LatLng( 06.32,  78.12));
        imbl.add(new LatLng( 06.31,  78.12));
        imbl.add(new LatLng( 06.30,  78.12));
        imbl.add(new LatLng( 06.29,  78.12));
        imbl.add(new LatLng( 06.28,  78.12));
        imbl.add(new LatLng( 06.27,  78.12));
        imbl.add(new LatLng( 06.26,  78.12));
        imbl.add(new LatLng( 06.25,  78.12));
        imbl.add(new LatLng( 06.24,  78.12));
        imbl.add(new LatLng( 06.23,  78.12));
        imbl.add(new LatLng( 06.22,  78.12));
        imbl.add(new LatLng( 06.21,  78.12));
        imbl.add(new LatLng( 06.20,  78.12));
        imbl.add(new LatLng( 06.19,  78.12));
        imbl.add(new LatLng( 06.18,  78.12));
        imbl.add(new LatLng( 06.17,  78.12));
        imbl.add(new LatLng( 06.16,  78.12));
        imbl.add(new LatLng( 06.15,  78.12));
        imbl.add(new LatLng( 06.14,  78.12));
        imbl.add(new LatLng( 06.13,  78.12));
        imbl.add(new LatLng( 06.12,  78.12));
        imbl.add(new LatLng( 06.11,  78.12));
        imbl.add(new LatLng( 06.10,  78.12));
        imbl.add(new LatLng( 06.09,  78.11));
        imbl.add(new LatLng( 06.08,  78.11));
        imbl.add(new LatLng( 06.07,  78.11));
        imbl.add(new LatLng( 06.06,  78.11));
        imbl.add(new LatLng( 06.05,  78.11));
        imbl.add(new LatLng( 06.04,  78.11));
        imbl.add(new LatLng( 06.03,  78.10));
        imbl.add(new LatLng( 06.02,  78.10));
        imbl.add(new LatLng( 06.01,  78.09));
        imbl.add(new LatLng( 06.00,  78.08));
        imbl.add(new LatLng( 05.99,  78.07));
        imbl.add(new LatLng( 05.98,  78.06));
        imbl.add(new LatLng( 05.97,  78.05));
        imbl.add(new LatLng( 05.96,  78.04));
        imbl.add(new LatLng( 05.95,  78.03));
        imbl.add(new LatLng( 05.94,  78.02));
        imbl.add(new LatLng( 05.93,  78.01));
        imbl.add(new LatLng( 05.92,  78.00));
        imbl.add(new LatLng( 05.91,  77.99));
        imbl.add(new LatLng( 05.90,  77.98));
        imbl.add(new LatLng( 05.89,  77.97));
        imbl.add(new LatLng( 05.88,  77.96));
        imbl.add(new LatLng( 05.87,  77.95));
        imbl.add(new LatLng( 05.86,  77.94));
        imbl.add(new LatLng( 05.85,  77.93));
        imbl.add(new LatLng( 05.84,  77.92));
        imbl.add(new LatLng( 05.83,  77.91));
        imbl.add(new LatLng( 05.82,  77.90));
        imbl.add(new LatLng( 05.81,  77.89));
        imbl.add(new LatLng( 05.80,  77.88));
        imbl.add(new LatLng( 05.79,  77.87));
        imbl.add(new LatLng( 05.78,  77.86));
        imbl.add(new LatLng( 05.77,  77.85));
        imbl.add(new LatLng( 05.76,  77.84));
        imbl.add(new LatLng( 05.75,  77.83));
        imbl.add(new LatLng( 05.74,  77.82));
        imbl.add(new LatLng( 05.73,  77.81));
        imbl.add(new LatLng( 05.72,  77.80));
        imbl.add(new LatLng( 05.71,  77.79));
        imbl.add(new LatLng( 05.70,  77.78));
        imbl.add(new LatLng( 05.69,  77.77));
        imbl.add(new LatLng( 05.68,  77.76));
        imbl.add(new LatLng( 05.67,  77.75));
        imbl.add(new LatLng( 05.66,  77.74));
        imbl.add(new LatLng( 05.65,  77.73));
        imbl.add(new LatLng( 05.64,  77.72));
        imbl.add(new LatLng( 05.63,  77.71));
        imbl.add(new LatLng( 05.62,  77.70));
        imbl.add(new LatLng( 05.61,  77.69));
        imbl.add(new LatLng( 05.60,  77.68));
        imbl.add(new LatLng( 05.59,  77.67));
        imbl.add(new LatLng( 05.58,  77.66));
        imbl.add(new LatLng( 05.57,  77.65));
        imbl.add(new LatLng( 05.56,  77.64));
        imbl.add(new LatLng( 05.55,  77.63));
        imbl.add(new LatLng( 05.54,  77.62));
        imbl.add(new LatLng( 05.53,  77.61));
        imbl.add(new LatLng( 05.52,  77.60));
        imbl.add(new LatLng( 05.51,  77.59));
        imbl.add(new LatLng( 05.50,  77.58));
        imbl.add(new LatLng( 05.49,  77.57));
        imbl.add(new LatLng( 05.48,  77.56));
        imbl.add(new LatLng( 05.47,  77.55));
        imbl.add(new LatLng( 05.46,  77.54));
        imbl.add(new LatLng( 05.45,  77.53));
        imbl.add(new LatLng( 05.44,  77.52));
        imbl.add(new LatLng( 05.43,  77.51));
        imbl.add(new LatLng( 05.42,  77.50));
        imbl.add(new LatLng( 05.41,  77.49));
        imbl.add(new LatLng( 05.40,  77.48));
        imbl.add(new LatLng( 05.39,  77.47));
        imbl.add(new LatLng( 05.38,  77.46));
        imbl.add(new LatLng( 05.37,  77.45));
        imbl.add(new LatLng( 05.36,  77.44));
        imbl.add(new LatLng( 05.35,  77.43));
        imbl.add(new LatLng( 05.34,  77.42));
        imbl.add(new LatLng( 05.33,  77.41));
        imbl.add(new LatLng( 05.32,  77.40));
        imbl.add(new LatLng( 05.31,  77.40));
        imbl.add(new LatLng( 05.30,  77.39));
        imbl.add(new LatLng( 05.29,  77.38));
        imbl.add(new LatLng( 05.28,  77.37));
        imbl.add(new LatLng( 05.27,  77.36));
        imbl.add(new LatLng( 05.26,  77.35));
        imbl.add(new LatLng( 05.25,  77.34));
        imbl.add(new LatLng( 05.24,  77.33));
        imbl.add(new LatLng( 05.23,  77.32));
        imbl.add(new LatLng( 05.22,  77.31));
        imbl.add(new LatLng( 05.21,  77.31));
        imbl.add(new LatLng( 05.20,  77.30));
        imbl.add(new LatLng( 05.19,  77.29));
        imbl.add(new LatLng( 05.18,  77.28));
        imbl.add(new LatLng( 05.17,  77.27));
        imbl.add(new LatLng( 05.16,  77.26));
        imbl.add(new LatLng( 05.15,  77.25));
        imbl.add(new LatLng( 05.14,  77.24));
        imbl.add(new LatLng( 05.13,  77.23));
        imbl.add(new LatLng( 05.12,  77.22));
        imbl.add(new LatLng( 05.11,  77.21));
        imbl.add(new LatLng( 05.10,  77.20));
        imbl.add(new LatLng( 05.09,  77.19));
        imbl.add(new LatLng( 05.08,  77.18));
        imbl.add(new LatLng( 05.07,  77.17));
        imbl.add(new LatLng( 05.06,  77.16));
        imbl.add(new LatLng( 05.05,  77.15));
        imbl.add(new LatLng( 05.04,  77.14));
        imbl.add(new LatLng( 05.03,  77.13));
        imbl.add(new LatLng( 05.02,  77.12));
        imbl.add(new LatLng( 05.01,  77.11));
        imbl.add(new LatLng( 05.00,  77.10));




        //Comment the below FirebaseDatabase snippet after submitting in Firebase

       FirebaseDatabase.getInstance()
                .getReference("IMBL coordinates")
                .child("Coordinates")
                .setValue(imbl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapsActivity.this, "Your location has been updated!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void addUserMarker() {
        geoFire.setLocation("You are here", new GeoLocation(lastLocation.getLatitude(),
                lastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

                if(currentUser!=null) currentUser.remove();
                currentUser=mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude()))
                        .title("You are here"));

                //After add marker

                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(currentUser.getPosition(),10.0f));       //Specifies the zoom level
            }
        });
    }

    private void settingGeoFire() {

        myLocationRef= FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire=new GeoFire(myLocationRef);

    }

    private void buildLocationCallback() {
        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if(mMap!=null){

                    lastLocation = locationResult.getLastLocation();

                    addUserMarker();
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);          //Initially 5000
        locationRequest.setFastestInterval(3000);       //Initially 3000
        locationRequest.setSmallestDisplacement(10f);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        //Add Circle for IMBL coordinates
        addCircleArea();
    }

    private void addCircleArea() {
        if(geoQuery!=null)
        {
            geoQuery.removeGeoQueryEventListener(this);
            geoQuery.removeAllListeners();
        }


        for (LatLng latLng : imbl)                    // Trying to add polyline in the same for loop
        {
            mMap.addCircle(new CircleOptions().center(latLng)
                    .radius(2500)                       //means 5000 means 5km
                    .strokeColor(android.R.color.holo_red_dark)
                    .fillColor(0x22ff0000)  //22 is transparent code  FF000000
                    .strokeWidth(7.0f));

            //Create geoQuery when user is in IMBL
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 2.5f);  //500m=0.5
            geoQuery.addGeoQueryEventListener(MapsActivity.this);
        }
    }







    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("Alfroid",String.format("%s Beware.. Entering prohibited zone",key));
    }



    @Override
    public void onKeyExited(String key) {
        sendNotification("Alfroid",String.format("%s You are safe now",key));
    }


    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotification("Alfroid",String.format("%s You are still moving in prohibited area",key));




    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, ""+error.getDetails(), Toast.LENGTH_SHORT).show();

    }

    private void sendNotification(String Alfroid, String content) {
        Toast.makeText(this, ""+content, Toast.LENGTH_SHORT).show();
        String NOTIFICATION_CHANNEL_ID="Alfroid_multiple_location";
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,"My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            //CONFIG

            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(android.R.color.holo_red_dark);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round));

        Notification notification=builder.build();
        notificationManager.notify(new Random().nextInt(),notification);




    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {
        imbl= new ArrayList<>();

        for (MyLatLng myLatLng : latLngs)
        {
            LatLng convert = new LatLng(myLatLng.getLatitude(),myLatLng.getLongitude());
            imbl.add(convert);
        }
        //After imbl gets data, we'll call Map display
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        //clear map and add again

        if(mMap!=null)
        {
            mMap.clear();
            //Add user marker
            addUserMarker();


            //Add circles of IMBL
            addCircleArea();
        }


    }

    @Override
    public void onLoadLocationFailed(String message) {
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();

    }
}
