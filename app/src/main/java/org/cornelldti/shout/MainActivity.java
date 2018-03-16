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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.cornelldti.shout.speakout.ReportIncidentDialog;
import org.cornelldti.shout.util.functions.BiConsumer;
import org.cornelldti.shout.util.functions.Consumer;

import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

// TODO Migrate away from deprecated FusedLocationApi.

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private BottomNavigationView bottomNavigationView;
    private MenuItem prevMenuItem;
    private ProgressDialog startDialog;

    private LinearLayout bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private FloatingActionButton fab;

    private ViewPager mViewPager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Consumer<Location> mLastLocationCallback;
    private FirebaseAuth mAuth;

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
        startDialog = new ProgressDialog(this);
        startDialog.setMessage("Connecting to shOUT...");
        startDialog.setCancelable(false);
        startDialog.setInverseBackgroundForced(false);

        /* Start the dialog. */

        startDialog.show();

        /* Setup the location request. */

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                // TODO I reset these values to what the comments said they should be. - Evan
                .setInterval(10000L)        // 10 seconds, in milliseconds
                .setFastestInterval(1000L); // 1 second, in milliseconds

        /* Construct the Google client */

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        /* Connect to the Google client. */

        mGoogleApiClient.connect();

        /* Retrieve & store the view children. */

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        mViewPager = findViewById(R.id.pager);

        fab = findViewById(R.id.fab);
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        /* Setup the pages adapter... */

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), getResources().getInteger(R.integer.number_of_tabs));
        mViewPager.setAdapter(adapter);

        /* Set the default page to "Go Out" */

        // TODO double check that this is the correct way to set the default page
        mViewPager.setCurrentItem(Page.SPEAK_OUT);
        bottomNavigationView.getMenu().getItem(Page.SPEAK_OUT).setChecked(true);
        setStatusBarColor(Page.SPEAK_OUT);
        setFABAction(FABAction.START_REPORT, Page.SPEAK_OUT);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        /* Register page change listeners... */

        mViewPager.addOnPageChangeListener(new PageChangeListener());
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavItemSelectedListener);
    }


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
                startDialog.cancel();
            });
        } else {
            /* If the user is already signed in, remove the blocking dialog... */
            startDialog.cancel();
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

    private class PageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            /* Fix status bar styling on Speak Out page... */
            setStatusBarColor(position);

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

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
                    bottomSheet.setVisibility(View.INVISIBLE);

                    setFABAction(FABAction.DISABLED, position);
            }
            if (prevMenuItem != null) {
                prevMenuItem.setChecked(false);
            } else {
                /* If no menu item was previously selected, deselect EVERYTHING */
                bottomNavigationView.getMenu().getItem(Page.GO_OUT).setChecked(false);
                bottomNavigationView.getMenu().getItem(Page.REACH_OUT).setChecked(false);
                bottomNavigationView.getMenu().getItem(Page.SPEAK_OUT).setChecked(false);
            }

            /* Set currently selected item... */
            bottomNavigationView.getMenu().getItem(position).setChecked(true);
            prevMenuItem = bottomNavigationView.getMenu().getItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    // TODO

    public void updateSheet(BiConsumer<LinearLayout, BottomSheetBehavior> sheet) {
        if (sheet == null) return;

        sheet.apply(bottomSheet, bottomSheetBehavior);
    }


    public void setFABAction(FABAction action, int returnPage) {
        setFABAction(action, null, returnPage);
    }

    public void setFABAction(FABAction action, LatLng location, int returnPage) {
        switch (action) {
            case START_REPORT:

                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit));

                // TODO one listener, switch inside
                fab.setOnClickListener(view -> {
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

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.location));

                fab.setOnClickListener(view -> {
                });

                break;
            case HELP:
            case DISABLED:
                // todo
            default:

                fab.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.alert_light_frame));

                fab.setOnClickListener(view -> {
                });
                break;
        }

    }

    public void setStatusBarColor(int position) {
        Window window = getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (position == Page.SPEAK_OUT) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                    window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                } else if (position == Page.GO_OUT) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                } else if (position == Page.REACH_OUT) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                    window.setStatusBarColor(Color.TRANSPARENT);

                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                }
            } else {
                // TODO test compatibility code (update the compat code)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (position != Page.SPEAK_OUT) {
                        window.setStatusBarColor(Color.TRANSPARENT);
                    } else {
                        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (position == Page.SPEAK_OUT) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    } else {
                        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }

                }
            }
        }
    }
}
