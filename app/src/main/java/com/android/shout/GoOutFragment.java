package com.android.shout;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.android.shout.R.id.mapView;

public class GoOutFragment extends Fragment {

    private MapView mMapView;

    private GoogleMap map;

    public GoOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.goout_fragment, container, false);
        mMapView = rootView.findViewById(mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                Location location = ((MainActivity) getActivity()).getCurLocation();
//                Location location = null;
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    Toast.makeText(getActivity(), "please enable location service", Toast.LENGTH_LONG).show();
                }

//                location = ((MainActivity) getActivity()).getCurLocation();
                if (location == null) {
                    location = new Location("");
                    location.setLatitude(42.448795d);
                    location.setLongitude(-76.483939d);
                }
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(42.447905, -76.484293))
                        .title("First"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    // todo move to sep thread?
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        // todo check getContext() call won't fail under lollipop

                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Incident @ " + latLng.toString())); // todo is line 0 correct?
                        Intent i = new Intent(getContext(), ReportIncident.class);
                        Bundle bundle = new Bundle();
                        bundle.putDoubleArray("location", new double[]{latLng.latitude, latLng.longitude});
                        i.putExtras(bundle);
                        startActivity(i);
                    }
                });

                //add markers
                map = googleMap;
            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}