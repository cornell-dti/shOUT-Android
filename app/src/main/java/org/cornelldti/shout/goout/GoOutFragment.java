package org.cornelldti.shout.goout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import org.cornelldti.shout.MainActivity;
import org.cornelldti.shout.PagerAdapter;
import org.cornelldti.shout.R;
import org.cornelldti.shout.speakout.ApprovedReport;
import org.cornelldti.shout.speakout.ReportIncidentDialog;
import org.cornelldti.shout.util.AndroidUtil;
import org.cornelldti.shout.util.LayoutUtil;
import org.cornelldti.shout.util.LocationUtil;

import java.util.HashMap;
import java.util.Map;

import static org.cornelldti.shout.R.id.map_view;

/**
 * Class which handles/constructs the "Go Out" view.
 * <p>
 * Updated by Evan Welsh on 2/28/18
 */

public class GoOutFragment extends Fragment implements PlaceSelectionListener, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    private MapView mMapView;
    private GoogleMap map;

    private Map<String, MarkerClusterItem> markerClusterItems = new HashMap<>();
    private MarkerClusterManager mClusterManager;

    private GeoQuery geoQuery;
    private Marker currentLocationMarker;

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

        mMapView = rootView.findViewById(map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        Context context = AndroidUtil.getContext(rootView, this);

        if (context != null) {
            try {
                MapsInitializer.initialize(context); // TODO Was application context necessary?
            } catch (Exception e) {
                e.printStackTrace(); // TODO Log appropriately
            }
        }

        mMapView.getMapAsync(googleMap -> {
            map = googleMap;

            // moves all "map drawn" ui elements (buttons, etc)
            // 55 = margins + size of searchbar
            googleMap.setPadding(0, statusbarSize + LayoutUtil.getPixelsFromDp(getResources(), 55), 0, 0);

            googleMap.setMinZoomPreference(14.5f);

            googleMap.getUiSettings().setMyLocationButtonEnabled(false); // TODO This button interferes with the search bar
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.setLatLngBoundsForCameraTarget(LocationUtil.getIthacaBounds());

            // TODO check this...

            Context currentContext = AndroidUtil.getContext(null, this);

            if (currentContext != null && context instanceof Activity) {
                if ((ActivityCompat.checkSelfPermission(currentContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(currentContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions((Activity) currentContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                } else {
                    googleMap.setMyLocationEnabled(true);
                }
            } else {
                // TODO error
            }

            Activity activity = getActivity();

            Location location = LocationUtil.latLngToLocation(LocationUtil.CORNELL_CENTER);

            if (activity instanceof MainActivity && !((MainActivity) activity).setLocationUpdateListener(GoOutFragment.this)) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to website user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            /// Cluster Manager setup
            mClusterManager = new MarkerClusterManager(currentContext, googleMap);

            googleMap.setOnCameraIdleListener(mClusterManager);
            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);

            // TODO this may need to be moved into a separate thread.
            googleMap.setOnMapLongClickListener(latLng -> {
                ReportIncidentDialog dialog = ReportIncidentDialog.newInstance(latLng, PagerAdapter.Pages.GO_OUT);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
            });

            googleMap.setOnCameraIdleListener(() -> {
                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                LatLng a = bounds.northeast;
                LatLng b = bounds.southwest;
                float[] results = new float[4];

                Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);

                LatLng center = map.getCameraPosition().target;

                double radius = results[0] / 1000.0 + 1.0;

                geoQuery.setLocation(new GeoLocation(center.latitude, center.longitude), radius);
            });

            // Adds markers organized into clusters
            addMarkers();
        });

        return rootView;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                if (map != null) {
                    map.setMyLocationEnabled(true);
                }
            }
        }
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

    private void addMarkers() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng a = bounds.northeast;
        LatLng b = bounds.southwest;
        float[] results = new float[4];

        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);

        LatLng center = map.getCameraPosition().target;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("report_locations");

        double radius = results[0] / 1000.0 + 1.0;
        GeoFire geoFire = new GeoFire(ref);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(center.latitude, center.longitude), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                // Finds the ApprovedReport object for each marker location to access title and body!
                FirebaseDatabase.getInstance().getReference("unapproved_reports").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ApprovedReport report = dataSnapshot.getValue(ApprovedReport.class);
                        MarkerClusterItem item = new MarkerClusterItem(location.latitude, location.longitude, report.getTitle(), report.getBody());
                        markerClusterItems.put(key, item);
                        mClusterManager.addItem(item);
                        mClusterManager.cluster();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("ERROR", "Database Error");
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                MarkerClusterItem item = markerClusterItems.get(key);
                if (item != null) {
                    mClusterManager.removeItem(item);
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                // TODO display loading progress?
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO don't continuously update? Check UX.

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        } else if (map != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
        }

        if (map != null) {
            currentLocationMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mee))
                    .title("Current Location"));

        }
    }

    /**
     * Super class to extend implementation of onCameraIdle method for ClusterManager class.
     */
    class MarkerClusterManager extends ClusterManager<MarkerClusterItem> implements GoogleMap.OnCameraIdleListener {
        MarkerClusterManager(Context c, GoogleMap map) {
            super(c, map);
        }

        @Override
        public void onCameraIdle() {
            super.onCameraIdle();
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            LatLng a = bounds.northeast;
            LatLng b = bounds.southwest;
            float[] results = new float[4];

            Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);

            LatLng center = map.getCameraPosition().target;

            double radius = results[0] / 1000.0 + 1.0;

            geoQuery.setLocation(new GeoLocation(center.latitude, center.longitude), radius);

        }
    }

}