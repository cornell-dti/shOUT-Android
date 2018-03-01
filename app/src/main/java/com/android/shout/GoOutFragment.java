package com.android.shout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.shout.util.LayoutUtil;
import com.android.shout.util.LocationUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.android.shout.R.id.mapView;

/**
 * Class which handles/constructs the "Go Out" view.
 * <p>
 * Updated by Evan Welsh on 2/28/18
 */

public class GoOutFragment extends Fragment implements PlaceSelectionListener {

    private MapView mMapView;
    private GoogleMap map;

    /* PlaceSelectionListener Implementation */

    @Override
    public void onPlaceSelected(Place place) {
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
        }
    }

    @Override
    public void onError(Status status) {
        // TODO handle errrors from place selection.
    }

    /* Fragment Methods */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.goout_fragment, container, false);


        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment) getChildFragmentManager().findFragmentById(R.id.place_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setHint(getString(R.string.location_search_hint));
        autocompleteFragment.setBoundsBias(LocationUtil.getIthacaBounds());


        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0) {
            CardView view = rootView.findViewById(R.id.location_search_bar);
            ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(view.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP, statusbarSize + 8);
            constraintSet.applyTo(constraintLayout);
        }

        mMapView = rootView.findViewById(mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                map = googleMap;

                // moves all "map drawn" ui elements (buttons, etc)
                // 55 = margins + size of searchbar
                googleMap.setPadding(0, statusbarSize + LayoutUtil.getPixelsFromDp(getResources(), 55), 0, 0);

                googleMap.setMinZoomPreference(14.5f);
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(false);
                googleMap.setLatLngBoundsForCameraTarget(LocationUtil.getIthacaBounds());

                Location location = null;

                if (getActivity().checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        location = ((MainActivity) getActivity()).getCurLocation();
                    } catch (SecurityException ignored) {
                    }
                }

                if (location == null) {
                    Toast.makeText(getActivity(), "please enable location service", Toast.LENGTH_LONG).show();
                    location = LocationUtil.latLngToLocation(LocationUtil.CORNELL_CENTER);
                }

                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("You"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    // TODO this may need to be moved into a separate thread.
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                // TODO make this a string resource and use String.format
                                .title("Incident @ " + latLng.toString())); // TODO attempt to get the place name
                       /* Intent i = new Intent(getActivity(), ReportIncident.class);
                        Bundle bundle = new Bundle();
                        bundle.putDoubleArray("location", new double[]{latLng.latitude, latLng.longitude});
                        i.putExtras(bundle);
                        startActivity(i);*/

                        ReportIncidentDialog dialog = ReportIncidentDialog.newInstance(latLng);

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.add(android.R.id.content, dialog)
                                .addToBackStack(null).commit();


                    }
                });
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