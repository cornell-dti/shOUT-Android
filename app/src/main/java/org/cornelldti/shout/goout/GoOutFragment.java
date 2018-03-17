package org.cornelldti.shout.goout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.core.util.Function;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.clustering.ClusterManager;

import org.cornelldti.shout.FABAction;
import org.cornelldti.shout.MainActivity;
import org.cornelldti.shout.Page;
import org.cornelldti.shout.R;
import org.cornelldti.shout.speakout.ApprovedReport;
import org.cornelldti.shout.speakout.ReportIncidentDialog;
import org.cornelldti.shout.util.AndroidUtil;
import org.cornelldti.shout.util.LayoutUtil;
import org.cornelldti.shout.util.LocationUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.cornelldti.shout.R.id.map_view;

/**
 * Class which handles/constructs the "Go Out" view.
 * <p>
 * Updated by Evan Welsh on 2/28/18
 */

public class GoOutFragment extends Fragment implements PlaceSelectionListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    private MapView mMapView;
    private GoogleMap mGoogleMap;

    private Map<String, MarkerClusterItem> markerClusterItems = new HashMap<>();
    private ClusterManager<MarkerClusterItem> mClusterManager;

    private GeoQuery geoQuery;
    private Marker clickedLocationMarker;

    private FusedLocationProviderClient client;

    private GeoFire geoFire;

    private final double QUERY_RADIUS = 0.2;

    /* PlaceSelectionListener Implementation */

    @Override
    public void onPlaceSelected(Place place) {
        if (mGoogleMap != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
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

        /* Fix status bar overlap issues */

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0) {
            CardView view = rootView.findViewById(R.id.location_search_bar);
            ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(view.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP, statusbarSize + LayoutUtil.getPixelsFromDp(getResources(), 8));
            constraintSet.applyTo(constraintLayout);
        }

        /* Setup the search bar... */

        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment) getChildFragmentManager().findFragmentById(R.id.place_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setHint(getString(R.string.location_search_hint));
        autocompleteFragment.setBoundsBias(LocationUtil.getIthacaBounds());

        /* Setup and cache the mGoogleMap view */

        mMapView = rootView.findViewById(map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the mGoogleMap to display immediately

        /* Initialize the mGoogleMap... */

        Context context = AndroidUtil.getContext(rootView, this);

        if (context != null) {
            try {
                MapsInitializer.initialize(context); // TODO Was application context necessary?
            } catch (Exception e) {
                e.printStackTrace(); // TODO Log appropriately
            }
        }

        /* Setup GeoFire */

        setupGeoFire();

        /* Setup the map... */

        mMapView.getMapAsync(getMapSetupFunc(context, statusbarSize)::apply);

        return rootView;
    }

    private Function<LatLng, Void> getMapLongClickFunc(MainActivity mainActivity) {
        return latLng -> {

            if (mainActivity != null) {
                mainActivity.updateSheet((sheet, behavior) -> {
                    /* Setup the recycler view for nearby reports */

                    final RecyclerView nearbyReportsView = sheet.findViewById(R.id.nearby_reports_recycler_view);
                    final TextView address = sheet.findViewById(R.id.address_quick_view);
                    final TextView numberOfReports = sheet.findViewById(R.id.number_of_reports_quick_view);
                    final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mainActivity);

                    nearbyReportsView.setLayoutManager(mLayoutManager);
                    nearbyReportsView.setAdapter(
                            GoOutRecentReportsAdapter
                                    .construct(this, latLng, mainActivity, QUERY_RADIUS) // CHANGE? TODO
                                    .withItemCountCallback(itemCount -> {
                                        numberOfReports.setText(getResources().getQuantityString(R.plurals.number_of_reports, itemCount, itemCount)); // convert to resource
                                    })
                    );

                    // TODO convert latLng to place *more correctly :p*
                    address.setText(LocationUtil.getAddressForLocation(mainActivity, latLng).getAddressLine(0));

                    /* Set the FAB Action to edit... */

                    mainActivity.setFABAction(FABAction.START_REPORT, latLng, Page.GO_OUT);

                    /* Show the "special location" marker... */

                    // TODO update or recreate?
                    clickedLocationMarker.setPosition(latLng);
                    clickedLocationMarker.setVisible(true);

                    final LinearLayout quickView = sheet.findViewById(R.id.quick_view_padding);

                    /* This handles two primary styles... */
                    /* 1) Clear all data/adapters when the bottom sheet is hidden. */
                    /* 2) Adjust the top padding to ensure the bottom sheet doesn't go behind the status bar while being animated... */

                    behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                        @Override
                        public void onStateChanged(@NonNull View bottomSheet, int newState) {
                            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                                nearbyReportsView.setAdapter(null);
                                mainActivity.setFABAction(FABAction.CURRENT_LOCATION, Page.GO_OUT);

                                behavior.setBottomSheetCallback(null);

                                clickedLocationMarker.setVisible(false);
                            }

                            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        (int) Math.ceil(LayoutUtil.getStatusBarHeight(mainActivity))
                                );

                                quickView.setLayoutParams(params);
                            }
                        }

                        @Override
                        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                            // TODO Convert to animation for performance sake

                            // Rough explanation...

                            // Essentially once the sheet is more than half way ascended to the top, begin adding padding
                            // corresponding to how close it is to the top. It gets 1/x * 2 * statusbarheight added every animation tick
                            // where x is the total number of ticks. And the reverse is applied when descending.

                            if (slideOffset > 0.5f) {
                                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        (int) Math.ceil((2 * (0.5 - (1.0 - slideOffset))) * LayoutUtil.getStatusBarHeight(mainActivity)) // + LayoutUtil.getPixelsFromDp(getResources(), 4)
                                );

                                quickView.setLayoutParams(params);
                            } else if (slideOffset >= 0f) {
                                if (quickView.getLayoutParams().height > 0) {
                                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            0
                                    );
                                    quickView.setLayoutParams(params);
                                }
                            }
                        }
                    });

                    /* Show the bottom sheet... */
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                });
            } else {
                // FALLBACK

                ReportIncidentDialog dialog = ReportIncidentDialog.newInstance(latLng, Page.GO_OUT);

                FragmentTransaction transaction = getFragmentManager().beginTransaction(); // todo nullpointer
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
            }

            return null; // (void return)
        };
    }

    private Function<GoogleMap, Void> getMapSetupFunc(Context context, int statusbarSize) {
        return googleMap -> {
            GoOutFragment.this.mGoogleMap = googleMap;

            // moves all "mGoogleMap drawn" ui elements (buttons, etc)
            // 55 = margins + size of searchbar
            googleMap.setPadding(0, statusbarSize + LayoutUtil.getPixelsFromDp(getResources(), 55), 0, LayoutUtil.getPixelsFromDp(getResources(), 55));

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

            /* Setup location updates to allow the "current location" FAB to function */

            Location centerLocation = LocationUtil.latLngToLocation(LocationUtil.CORNELL_CENTER);

            if (activity != null) {
                client = LocationServices.getFusedLocationProviderClient(activity);
                // TODO actually get location updates and allow user to go to current location and send user to current location on opening
                // client.requestLocationUpdates();
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude()))      // Sets the center of the mGoogleMap to website user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // TODO don't use last location, its imprecise

            if (activity instanceof MainActivity) {
                ((MainActivity) activity).getLastLocation((lastLocation) -> {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 14));
                });
            }

            /* Setup clustering manager and clusters... */
            mClusterManager = new ClusterManager<MarkerClusterItem>(currentContext, googleMap);

            mClusterManager.setOnClusterItemClickListener(item -> {
                if(activity instanceof MainActivity) {
                    setupLocationDetailsView((MainActivity)activity, item.getPosition(), 0);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            item.getPosition(), (float) Math.floor(googleMap
                                    .getCameraPosition().zoom + 4)), 500,
                            null);
                }
                return true;
            });

            mClusterManager.setOnClusterClickListener(item -> {
                Collection<MarkerClusterItem> items = item.getItems();
                MarkerClusterItem firstItem =  items.iterator().next();
                if(activity instanceof MainActivity) {
                    setupLocationDetailsView((MainActivity)activity, firstItem.getPosition(), QUERY_RADIUS);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            firstItem.getPosition(), (float) Math.floor(googleMap
                                    .getCameraPosition().zoom + 4)), 500,
                            null);
                }
                return true;
            });

            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);

            /* Setup long clicking for the details view... */

            if (activity instanceof MainActivity) {
                googleMap.setOnMapLongClickListener(getMapLongClickFunc((MainActivity) activity)::apply);
            } else {
                // TODO
            }

            googleMap.setOnCameraIdleListener(() -> {
                LatLngBounds bounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                LatLng a = bounds.northeast;
                LatLng b = bounds.southwest;
                float[] results = new float[4];

                Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);

                LatLng center = mGoogleMap.getCameraPosition().target;

                double radius = results[0] / 1000.0 + 1.0;

                geoQuery.setLocation(new GeoLocation(center.latitude, center.longitude), radius);
            });

            clickedLocationMarker = googleMap.addMarker(new MarkerOptions().position(LocationUtil.CORNELL_CENTER)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            clickedLocationMarker.setVisible(false);

            // Adds markers organized into clusters
            addMarkers();

            return null;

        };
    }

    private void setupGeoFire() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("report_locations");
        geoFire = new GeoFire(ref); // setup
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                if (mGoogleMap != null) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    private void setupLocationDetailsView(MainActivity activity, LatLng latLng, double radius) {
        activity.updateSheet((sheet, behavior) -> {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            LinearLayout quickView = sheet.findViewById(R.id.quick_view_padding);

            RecyclerView view = sheet.findViewById(R.id.nearby_reports_recycler_view);

            TextView address = sheet.findViewById(R.id.address_quick_view);
            TextView numberOfReports = sheet.findViewById(R.id.number_of_reports_quick_view);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
            view.setLayoutManager(mLayoutManager);

            view.setAdapter(GoOutRecentReportsAdapter.construct(this, latLng, activity, radius).withItemCountCallback(itemCount -> {
                        numberOfReports.setText(getResources().getQuantityString(R.plurals.number_of_reports, itemCount, itemCount));
                        // convert to resource
                    })
            );
            address.setText("");

            activity.setFABAction(FABAction.START_REPORT, latLng, Page.GO_OUT);

            // TODO update or recreate?
            clickedLocationMarker.setPosition(latLng);
            clickedLocationMarker.setVisible(true);

            behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        view.setAdapter(null);
                        ((MainActivity) activity).setFABAction(FABAction.CURRENT_LOCATION, Page.GO_OUT);

                        behavior.setBottomSheetCallback(null);

                        clickedLocationMarker.setVisible(false);
                    }

                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (int) Math.ceil(LayoutUtil.getStatusBarHeight(activity)) // + LayoutUtil.getPixelsFromDp(getResources(), 4)
                        );

                        quickView.setLayoutParams(params);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    if (slideOffset > 0.5f) {
                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (int) Math.ceil((2 * (0.5 - (1.0 - slideOffset))) * LayoutUtil.getStatusBarHeight(activity)) // + LayoutUtil.getPixelsFromDp(getResources(), 4)
                        );

                        quickView.setLayoutParams(params);
                    } else if (slideOffset >= 0f) {
                        if (quickView.getLayoutParams().height > 0) {

                            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    0
                            );


                            quickView.setLayoutParams(params);
                        }
                    }
                }
            });
        });
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
        LatLngBounds bounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng a = bounds.northeast;
        LatLng b = bounds.southwest;
        float[] results = new float[4];

        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);

        LatLng center = mGoogleMap.getCameraPosition().target;


        double radius = results[0] / 1000.0 + 1.0;

        if (geoFire == null) {
            setupGeoFire();
        }

        geoQuery = geoFire.queryAtLocation(new GeoLocation(center.latitude, center.longitude), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String reportId, GeoLocation location) {

                // Finds the ApprovedReport object for each marker location to access title and body!
                FirebaseFirestore.getInstance().collection("reports").document(reportId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ApprovedReport report = task.getResult().toObject(ApprovedReport.class);
                        MarkerClusterItem item = new MarkerClusterItem(location.latitude, location.longitude, reportId);
                        markerClusterItems.put(reportId, item);
                        mClusterManager.addItem(item);
                        mClusterManager.cluster();
                    } else {
                        // todo error;
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

    public GeoFire geofire() {
        if (geoFire == null) {
            setupGeoFire();
        }

        return geoFire;
    }


}