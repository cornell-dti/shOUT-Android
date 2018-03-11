package org.cornelldti.shout;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    private ViewPager mViewPager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationListener locationUpdateListener;
    private FirebaseAuth mAuth;

    /**
     * Sets the current location listener for the app.
     *
     * @param locationUpdateListener - The listener to set.
     * @return boolean - true if successfully connected, false if location permissions have to be requested or if another error was encountered.
     */
    public boolean setLocationUpdateListener(LocationListener locationUpdateListener) {
        this.locationUpdateListener = locationUpdateListener;

        /* Request permissions if we don't have them, otherwise setup location updates... */

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationUpdateListener);
                return true;
            }
        }

        return false;
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

        /* Setup the pages adapter... */

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), getResources().getInteger(R.integer.number_of_tabs));
        mViewPager.setAdapter(adapter);

        /* Set the default page to "Go Out" */

        // TODO double check that this is the correct way to set the default page
        mViewPager.setCurrentItem(PagerAdapter.Pages.SPEAK_OUT);
        bottomNavigationView.getMenu().getItem(PagerAdapter.Pages.SPEAK_OUT).setChecked(true);
        setStatusBarColor(PagerAdapter.Pages.SPEAK_OUT);

        /* Register page change listeners... */

        mViewPager.addOnPageChangeListener(new MainOnPageChangeListener());
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavItemSelectedListener);
    }


    @Override
    public void onStart() {
        super.onStart();

        /* Retrieve the current user... */
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            /* Sign the user into firebase */
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
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
                }
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
            if (locationUpdateListener != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationUpdateListener);
            }

            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        /* Request location permissions if not available... */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (locationUpdateListener != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationUpdateListener);
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
                    if (locationUpdateListener != null && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationUpdateListener);
                    }
                }
            }
        }
    }

    private OnNavigationItemSelectedListener mOnNavItemSelectedListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.go_out:
                    mViewPager.setCurrentItem(PagerAdapter.Pages.GO_OUT);
                    break;
                case R.id.speak_out:
                    mViewPager.setCurrentItem(PagerAdapter.Pages.SPEAK_OUT);
                    break;
                case R.id.reach_out:
                    mViewPager.setCurrentItem(PagerAdapter.Pages.REACH_OUT);
                    break;
            }
            return true;
        }
    };

    private class MainOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            /* Fix status bar styling on Speak Out page... */
            setStatusBarColor(position);


            if (prevMenuItem != null) {
                prevMenuItem.setChecked(false);
            } else {
                /* If no menu item was previously selected, deselect EVERYTHING */
                bottomNavigationView.getMenu().getItem(PagerAdapter.Pages.GO_OUT).setChecked(false);
                bottomNavigationView.getMenu().getItem(PagerAdapter.Pages.REACH_OUT).setChecked(false);
                bottomNavigationView.getMenu().getItem(PagerAdapter.Pages.SPEAK_OUT).setChecked(false);
            }

            /* Set currently selected item... */
            bottomNavigationView.getMenu().getItem(position).setChecked(true);
            prevMenuItem = bottomNavigationView.getMenu().getItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public void setStatusBarColor(int position) {
        Window window = getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (position != PagerAdapter.Pages.SPEAK_OUT) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.TRANSPARENT);

                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                } else {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                    window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                }
            } else {
                // TODO test compatibility code
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (position != PagerAdapter.Pages.SPEAK_OUT) {
                        window.setStatusBarColor(Color.TRANSPARENT);
                    } else {
                        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (position == PagerAdapter.Pages.SPEAK_OUT) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    } else {
                        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }

                }
            }
        }
    }
}
