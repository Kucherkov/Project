package com.example.weatherproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings uiSettings;
    private Marker mrk;
    private Geocoder gecoder;
    private List<Address> address;
    public final static String THIEF = "com.example.weatherproject.THIEF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        gecoder = new Geocoder(this);
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
        uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mrk !=null){
                    mrk.remove();
                }

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(latLng.latitude,latLng.longitude)).zoom(10).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.animateCamera(cameraUpdate);
                mrk = mMap.addMarker(new MarkerOptions()
                        .position(latLng).draggable(true));

            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                try {
                    address = gecoder.getFromLocation(mrk.getPosition().latitude,mrk.getPosition().longitude,1);
                    String city = address.get(0).getLocality();
                    mrk.setTitle(city);
                    mrk.showInfoWindow();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        mMap.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent MapAnswer = new Intent();
                        double lat = mrk.getPosition().latitude;
                        double lng = mrk.getPosition().longitude;
                        MapAnswer.putExtra(THIEF,(Double.toString(lat)+','+Double.toString(lng)+","+mrk.getTitle()));
                        setResult(RESULT_OK, MapAnswer);
                        finish();
                    }
                }
        );
    }
}