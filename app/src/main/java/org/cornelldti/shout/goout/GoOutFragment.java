package org.cornelldti.shout.goout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.clustering.ClusterManager;

import org.cornelldti.shout.FABAction;
import org.cornelldti.shout.MainActivity;
import org.cornelldti.shout.Page;
import org.cornelldti.shout.R;
import org.cornelldti.shout.ShoutRealtimeDatabase;
import org.cornelldti.shout.ShoutTabFragment;
import org.cornelldti.shout.speakout.ReportIncidentDialogFragment;
import org.cornelldti.shout.util.AndroidUtil;
import org.cornelldti.shout.util.LayoutUtil;
import org.cornelldti.shout.util.LocationUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.cornelldti.shout.R.id.map_view;

/**
 * Class which handles/constructs the "Go Out" view.
 * <p>
 * Updated by Evan Welsh on 2/28/18
 */

public class GoOutFragment extends ShoutTabFragment {

    private static final String TAG = "GoOutFragment";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    private MapView mMapView;
    private GoogleMap mGoogleMap;

    private Map<String, MarkerClusterItem> markerClusterItems = new HashMap<>();
    private ClusterManager<MarkerClusterItem> mClusterManager;

    private GeoQuery geoQuery;
    private Marker clickedLocationMarker;

    private LatLng defaultLocationOverride;

    private final double QUERY_RADIUS = 0.2;

    private boolean initialMove;

    /* FRAGMENT LIFECYCLE */

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

    /* TAB VISIBILITY */

    @Override
    public void onDisplayed(Bundle bundle) {
        double[] latLngArr = bundle.getDoubleArray("latLng");

        if (latLngArr == null) return;

        if (mMapView != null && mGoogleMap != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLngArr[0], latLngArr[1]), ZoomLevel.DEFAULT));
        } else {
            defaultLocationOverride = new LatLng(latLngArr[0], latLngArr[1]);
        }
    }

    @Override
    public void onRemoved() {

    }

    /* FRAGMENT CREATION */

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
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (mGoogleMap != null) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), ZoomLevel.DEFAULT));
                }
            }

            @Override
            public void onError(Status status) {
                // TODO handle errrors from place selection.
            }
        });

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

        /* Setup the map... */

        mMapView.getMapAsync(instantiateMapHandler(context, statusbarSize));

        return rootView;
    }


    private OnMapReadyCallback instantiateMapHandler(Context context, int statusbarSize) {
        return googleMap -> {
            GoOutFragment.this.mGoogleMap = googleMap;

            // moves all "mGoogleMap drawn" ui elements (buttons, etc)
            // 55 = margins + size of searchbar
            googleMap.setPadding(0, statusbarSize + LayoutUtil.getPixelsFromDp(getResources(), 55), LayoutUtil.getPixelsFromDp(getResources(), 65), 0);

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

            LatLng defaultLocation = defaultLocationOverride != null ? defaultLocationOverride : LocationUtil.CORNELL_CENTER;

            // todo locatino setup?

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(defaultLocation)      // Sets the center of the mGoogleMap to website user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // TODO don't use last location, its imprecise

            if (activity instanceof MainActivity) {
                ((MainActivity) activity).registerLocationListener((lastLocation, force) -> {
                    if (force || (defaultLocationOverride == null && initialMove)) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), ZoomLevel.DEFAULT));
                        initialMove = false;
                    }
                });
            }

            defaultLocationOverride = null;

            /* Setup clustering manager and clusters... */
            mClusterManager = new ClusterManager<>(currentContext, googleMap);

            mClusterManager.setOnClusterItemClickListener(item -> {
                String reportId = item.getReportId();

                if (activity instanceof MainActivity) {
                    showReportsAtLocation((MainActivity) activity, item.getPosition()); // todo
                } else {
                    // TODO error toast
                }


                return false;
            });

            mClusterManager.setOnClusterClickListener(item -> {
                Collection<MarkerClusterItem> items = item.getItems();
                Iterator<MarkerClusterItem> iter = items.iterator();

                if (!iter.hasNext()) return true;

                LatLng first = iter.next().getPosition();
                double latAvg = first.latitude, longAvg = first.longitude;

                while (iter.hasNext()) {
                    LatLng pos = iter.next().getPosition();
                    latAvg = ((pos.latitude + latAvg) / 2);
                    longAvg = ((pos.longitude + longAvg) / 2);
                }

                float zoomLevel = Math.min((googleMap.getCameraPosition().zoom + 4), ZoomLevel.CLOSE_UP);

                if (activity instanceof MainActivity) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latAvg, longAvg), zoomLevel), 500, null);
                }

                return true;
            });

            googleMap.setOnCameraIdleListener(() -> {
                updateGeoQuery();
                // forward along
                mClusterManager.onCameraIdle();
            });

            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);

            /* Setup long clicking for the details view... */

            if (activity instanceof MainActivity) {
                googleMap.setOnMapLongClickListener((latLng -> {
                    showReportsByRadius((MainActivity) activity, latLng, 0.2); // todo
                }));
            } else {
                // TODO
            }

            clickedLocationMarker = googleMap.addMarker(new MarkerOptions().position(LocationUtil.CORNELL_CENTER)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            clickedLocationMarker.setVisible(false);

            updateGeoQuery();
        };
    }

    private void updateGeoQuery() {
        LatLngBounds bounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng a = bounds.northeast, b = bounds.southwest;

        float[] results = new float[4];

        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);

        LatLng center = mGoogleMap.getCameraPosition().target;

        double radius = results[0] / 1000.0 + 1.0;

        if (geoQuery == null) {
            geoQuery = geofire().queryAtLocation(new GeoLocation(center.latitude, center.longitude), radius);
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            geoQuery.setLocation(new GeoLocation(center.latitude, center.longitude), radius);
        }
    }

    private GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String reportId, GeoLocation location) {
            MarkerClusterItem item = new MarkerClusterItem(location.latitude, location.longitude, reportId);
            markerClusterItems.put(reportId, item);
            mClusterManager.addItem(item);
            mClusterManager.cluster();
        }


        @Override
        public void onKeyExited(String reportId) {
            MarkerClusterItem item = markerClusterItems.get(reportId);

            if (item != null) {
                mClusterManager.removeItem(item);
            }
        }

        @Override
        public void onKeyMoved(String reportId, GeoLocation location) {
            onKeyExited(reportId);
            onKeyEntered(reportId, location);
        }

        @Override
        public void onGeoQueryReady() {
            // TODO display loading progress?
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.d(TAG, error.getMessage());
        }
    };

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

    /* GEOFIRE UTILITY FUNCTIONS */

    private void showReportsAtLocation(MainActivity mainActivity, LatLng latLng) {
        // TODO don't do this by radius...
        showReportsByRadius(mainActivity, latLng, 0.001, false);
    }

    private void showReportsByRadius(MainActivity mainActivity, LatLng latLng, double radius) {
        showReportsByRadius(mainActivity, latLng, radius, true);
    }

    private void showReportsByRadius(MainActivity mainActivity, LatLng latLng, double radius, boolean nearby) {
        if (mainActivity != null) {
            mainActivity.updateSheet((sheet, behavior, shadow, nearbyReportsView, addressTextView, numberOfReportsTextView) -> {
                    /* Setup the recycler view for nearby reports */

                final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mainActivity);

                nearbyReportsView.setLayoutManager(mLayoutManager);

                nearbyReportsView.setAdapter(
                        GoOutRecentReportsAdapter
                                .construct(this, latLng, mainActivity, radius) // todo radius
                                .withItemCountCallback(itemCount -> {

                                    int resId = nearby ? R.plurals.number_of_reports_nearby : R.plurals.number_of_reports;
                                    Resources res = getResources();

                                    numberOfReportsTextView.setText(res.getQuantityString(resId, itemCount, itemCount));
                                })
                );

                // TODO (change within 750 feet text too)

                // TODO convert latLng to place *more correctly :p*
                addressTextView.setText(LocationUtil.getAddressForLocation(mainActivity, latLng).getAddressLine(0));

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

                            shadow.setVisibility(View.INVISIBLE);

                            clickedLocationMarker.setVisible(false);
                        }

                        if (newState != BottomSheetBehavior.STATE_HIDDEN) {
                            shadow.setVisibility(View.INVISIBLE);
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

            ReportIncidentDialogFragment dialog = ReportIncidentDialogFragment.newInstance(latLng, Page.GO_OUT);

            FragmentTransaction transaction = getFragmentManager().beginTransaction(); // todo nullpointer
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
        }
    }

    /* GEOFIRE INSTANTIATION/HANDLING */

    @Deprecated // just a little hack to make sure this variable is never utilized out of context.
    private GeoFire geoFire;

    @SuppressWarnings("deprecated")
    public GeoFire geofire() {
        if (geoFire == null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ShoutRealtimeDatabase.REPORT_LOCATIONS_KEY);
            geoFire = new GeoFire(ref);
        }

        return geoFire;
    }


}