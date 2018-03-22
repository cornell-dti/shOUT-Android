package org.cornelldti.shout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.cornelldti.shout.goout.BottomSheetUpdateCallback;
import org.cornelldti.shout.speakout.ReportIncidentDialog;
import org.cornelldti.shout.util.function.BiConsumer;
import org.cornelldti.shout.util.function.Consumer;

import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

// TODO Migrate away from deprecated FusedLocationApi.

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    /* UI Elements */

    private ViewPager mViewPager;

    private BottomNavigationView mBottomNavigationView;
    private BottomSheetBehavior mBottomSheetBehavior;
    private LinearLayout mBottomSheet;
    private View mBottomSheetShadow;

    private RecyclerView mReportsView;
    private TextView mAddressTextView, mNumOfReportsTextView;
    private FloatingActionButton mFloatingActionButton;

    /* This is just used for connecting to Firebase */

    private ProgressDialog mStartDialog;

    /* Utility variable... */

    private MenuItem mPrevMenuItem;

    private FusedLocationProviderClient mLocationClient;

    /* Backend connections... */

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;

    private Consumer<Location> mLastLocationCallback;

    /* Listeners */

    private OnNavigationItemSelectedListener mOnNavItemSelectedListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.go_out:
                    mViewPager.setCurrentItem(Page.GO_OUT);
                    break;
                case R.id.speak_out:
                    mViewPager.setCurrentItem(Page.SPEAK_OUT);
                    break;
                case R.id.reach_out:
                    mViewPager.setCurrentItem(Page.REACH_OUT);
                    break;
            }
            return true;
        }
    };

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            /* Fix status bar styling on Speak Out page... */
            setStatusBarColor(position);

            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mBottomSheetShadow.setVisibility(View.INVISIBLE);

            switch (position) {
                case Page.GO_OUT:
                    setFABAction(FABAction.CURRENT_LOCATION, position);
                    break;
                case Page.REACH_OUT:

                    setFABAction(FABAction.HELP, position);
                    break;
                case Page.SPEAK_OUT:
                    setFABAction(FABAction.START_REPORT, position);
                    break;
                default:
                    mBottomSheet.setVisibility(View.INVISIBLE);

                    setFABAction(FABAction.DISABLED, position);
            }
            if (mPrevMenuItem != null) {
                mPrevMenuItem.setChecked(false);
            } else {
                /* If no menu item was previously selected, deselect EVERYTHING */
                mBottomNavigationView.getMenu().getItem(Page.GO_OUT).setChecked(false);
                mBottomNavigationView.getMenu().getItem(Page.REACH_OUT).setChecked(false);
                mBottomNavigationView.getMenu().getItem(Page.SPEAK_OUT).setChecked(false);
            }

            /* Set currently selected item... */
            mBottomNavigationView.getMenu().getItem(position).setChecked(true);
            mPrevMenuItem = mBottomNavigationView.getMenu().getItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onStart() {
        super.onStart();

        /* Retrieve the current user... */
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            /* Sign the user into firebase */
            mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "Successfully signed into Firebase anonymously.");
                } else {
                    /* if sign in fails, display a message to the user. */
                    // TODO prevent certain app features
                    // e.g. hide the FAB
                    Log.d(TAG, "Failed to sign into Firebase anonymously.");
                    Toast.makeText(MainActivity.this, "Failed to connect to shOUT.", Toast.LENGTH_SHORT).show();
                }

                /* Remove the start dialog. */
                mStartDialog.cancel();
            });
        } else {
            /* If the user is already signed in, remove the blocking dialog... */
            mStartDialog.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        /* Stop location updates when we leave the current view. */
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        /* Let the user use back to exit the bottom sheet */

        if (mBottomSheet != null && mBottomSheetBehavior != null) {
            if (mBottomSheet.getVisibility() == View.VISIBLE && mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                // TODO actually cleanup the sheet's resources and such
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /* This allows use to draw behind the status bar (for the go out view) */

        Window window = getWindow();

        if (window != null) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        /* Get the Firebase authentication service. */

        mAuth = FirebaseAuth.getInstance();

        /* Setup the dialog which runs until the user is anonymously logged in. */

        // TODO put this somewhere cleaner and use a start screen instead.
        mStartDialog = new ProgressDialog(this);
        mStartDialog.setMessage("Connecting to shOUT...");
        mStartDialog.setCancelable(false);
        mStartDialog.setInverseBackgroundForced(false);

        /* Start the dialog. */

        mStartDialog.show();

        /* Setup the location request. */

        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                // TODO I reset these values to what the comments said they should be. - Evan
                .setInterval(10000L)        // 10 seconds, in milliseconds
                .setFastestInterval(1000L);
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            mLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    result.getLastLocation();

                }
            }, getMainLooper());
        }



        /* Construct the Google mLocationClient */

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        /* Connect to the Google mLocationClient. */

        mGoogleApiClient.connect();

        /* Retrieve & store the view children. */

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mViewPager = findViewById(R.id.pager);

        mFloatingActionButton = findViewById(R.id.fab);
        mBottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetShadow = findViewById(R.id.bottom_sheet_shadow);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mReportsView = mBottomSheet.findViewById(R.id.nearby_reports_recycler_view);
        mAddressTextView = mBottomSheet.findViewById(R.id.address_quick_view);
        mNumOfReportsTextView = mBottomSheet.findViewById(R.id.number_of_reports_quick_view);

        /* Setup the pages adapter... */

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), getResources().getInteger(R.integer.number_of_tabs));
        mViewPager.setAdapter(adapter);

        /* Set the default page to "Go Out" */

        // TODO double check that this is the correct way to set the default page
        mViewPager.setCurrentItem(Page.SPEAK_OUT);
        mBottomNavigationView.getMenu().getItem(Page.SPEAK_OUT).setChecked(true);
        setStatusBarColor(Page.SPEAK_OUT);
        setFABAction(FABAction.START_REPORT, Page.SPEAK_OUT);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        /* Register page change listeners... */

        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavItemSelectedListener);
    }

    /* Google Client Connection Handling */

    @Override
    public void onConnected(Bundle bundle) {
        /* Request location permissions if not available... */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 9000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("failed", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /* Location Services */

    /**
     * Sets the current location listener for the app.
     * todo update doc
     *
     * @param callback - The listener to set.
     */
    public void getLastLocation(Consumer<Location> callback) {
        /* Request permissions if we don't have them, otherwise setup location updates... */

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.mLastLocationCallback = callback;

            // todo hm
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else if (mGoogleApiClient.isConnected()) {
            // TODO don't wait for the first location update.
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            callback.apply(lastLocation);
        } else {
            this.mLastLocationCallback = callback;
        }

    }

    /* Permissions */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) { // TODO Magic...
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                if (mGoogleApiClient.isConnected()) {
                    // duh, we just got the permissions google...
                    @SuppressLint("MissingPermission")
                    Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    mLastLocationCallback.apply(lastLocation);
                    mLastLocationCallback = null;
                }
            }
        }
    }

    /* UI Utility Functions... */

    // TODO

    /**
     * Retrieves the relevant UI elements for the bottom sheet to be updated.
     *
     * @param updater the updating function.
     */
    public void updateSheet(BottomSheetUpdateCallback updater) {
        if (updater == null) return;

        updater.update(mBottomSheet, mBottomSheetBehavior, mBottomSheetShadow, mReportsView, mAddressTextView, mNumOfReportsTextView);
    }

    /**
     * Sets the current FAB action.
     *
     * @param action the action to set
     */
    public void setFABAction(FABAction action) {
        setFABAction(action, null, Page.UNKNOWN);
    }


    /**
     * Sets the current FAB action.
     *
     * @param action     the action to set
     * @param returnPage the page styling/state to return to if the action causes the page to change
     */
    public void setFABAction(FABAction action, int returnPage) {
        setFABAction(action, null, returnPage);
    }

    /**
     * Sets the current FAB action.
     *
     * @param action     the action to set.
     * @param location   the latitude and longitude of that action (if necessary, or null)
     * @param returnPage the page styling/state to return to if the action causes the page to change
     */
    public void setFABAction(FABAction action, LatLng location, int returnPage) {
        switch (action) {
            case START_REPORT:
                mFloatingActionButton.setVisibility(View.VISIBLE);

                mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit));

                // TODO one listener, switch inside
                mFloatingActionButton.setOnClickListener(view -> {
                    this.setStatusBarColor(Page.UNKNOWN);

                    ReportIncidentDialog dialog;

                    if (location != null) {
                        dialog = ReportIncidentDialog.newInstance(location, returnPage);
                    } else {
                        dialog = ReportIncidentDialog.newInstance(returnPage);
                    }

                    FragmentManager manager = getSupportFragmentManager();

                    if (manager != null) {
                        FragmentTransaction transaction = manager.beginTransaction();

                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
                    } else {
                        // TODO display error to user and log issue
                        // this will probably never happen
                    }
                });

                break;
            case CURRENT_LOCATION:
                mFloatingActionButton.setVisibility(View.VISIBLE);

                mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_my_location));

                mFloatingActionButton.setOnClickListener(view -> {
                });

                break;
            case HELP:
                mFloatingActionButton.setVisibility(View.VISIBLE);

                mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_help));

                mFloatingActionButton.setOnClickListener(view -> {
                    // todo help
                });
                break;
            case DISABLED:
            default:
                mFloatingActionButton.setVisibility(View.GONE);
                mFloatingActionButton.setOnClickListener(view -> {
                });
                break;
        }

    }

    /**
     * Sets the status bar coloring based on the current page.
     *
     * @param page the page constant of the currently visible page.
     * @see org.cornelldti.shout.Page
     */
    public void setStatusBarColor(int page) {
        Window window = getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (page == Page.SPEAK_OUT) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                    window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                } else if (page == Page.GO_OUT) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                } else if (page == Page.REACH_OUT) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                    window.setStatusBarColor(Color.TRANSPARENT);

                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                }
            } else {
                // TODO test compatibility code (update the compat code)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (page != Page.SPEAK_OUT) {
                        window.setStatusBarColor(Color.TRANSPARENT);
                    } else {
                        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (page == Page.SPEAK_OUT) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    } else {
                        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }

                }
            }
        }
    }
}
