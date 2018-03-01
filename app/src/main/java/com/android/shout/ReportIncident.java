package com.android.shout;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.shout.util.LocationUtil;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ReportIncident extends AppCompatActivity implements PlaceSelectionListener {
    
    private TextView locationText, dateText, timeText;
    private EditText postTitle, postText;

    private LatLng location;
    private boolean locationLocked; // TODO Ask design. See onCreate...

    private static Address getAddressForLocation(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // todo 1 may not be enough ;)
            if (addresses.size() > 0) {
                address = addresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LatLng latLng = null;

        setContentView(R.layout.activity_report_incident);
        postTitle = findViewById(R.id.postTitle);
        postText = findViewById(R.id.postText);
        locationText = findViewById(R.id.locationText);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);

        locationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Ask design whether location should be locked if "pin-opened" from map.
                ReportIncident.this.onLocationControlClicked(v);
            }
        });

        if (getIntent().hasExtra("location")) {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                double[] loc = bundle.getDoubleArray("location");

                this.location = loc != null ? new LatLng(loc[0], loc[1]) : latLng;
                this.locationLocked = true;
            }
        }

        if (this.location != null) {
            Address address = getAddressForLocation(getApplicationContext(), this.location);

            if (address != null) {
                locationText.setText(address.getAddressLine(0));
            } else {
                locationText.setText(this.location.toString());
            }
        }
    }

    public void post(View v) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("messages");
        if (postTitle.getText().toString().isEmpty() || postText.getText().toString().isEmpty()) {
            Toast.makeText(ReportIncident.this, "Make sure to fill in post title and message", Toast.LENGTH_LONG).show();
        } else {
            // todo validate getText()
            Message m = new Message(postText.getText().toString(), dateText.getText().toString(), timeText.getText().toString(), postTitle.getText().toString());
            String ID = database.push().getKey();
            m.setID(ID);
            database.child(ID).setValue(m);
            startActivity(new Intent(ReportIncident.this, SuggestionHandler.class));
        }
    }

    public void cancel(View v) {
        finish();
        // TODO set custom in/out animations
    }

    public void onLocationControlClicked(View v) {
        Intent intent = null;
        try {
            intent = new PlaceAutocomplete.IntentBuilder
                    (PlaceAutocomplete.MODE_FULLSCREEN)
                    .setBoundsBias(LocationUtil.getIthacaBounds())
                    .build(ReportIncident.this);
            startActivityForResult(intent, 1000);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Toast.makeText(ReportIncident.this, "Could not start the location search.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void onDateControlClicked(View v) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        java.text.DateFormat format = DateFormat.getDateFormat(ReportIncident.this);
                        Calendar cal = Calendar.getInstance();
                        cal.clear(); // TODO is this necessary?
                        cal.set(year, monthOfYear, dayOfMonth); // NOTE: these months are 0-based
                        String formattedDate = format.format(cal.getTime());

                        dateText.setText(formattedDate);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void onTimeControlClicked(View v) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        java.text.DateFormat format = DateFormat.getTimeFormat(ReportIncident.this);
                        Calendar cal = Calendar.getInstance();
                        cal.clear(); // TODO is this necessary?
                        cal.set(0, 0, 0, hourOfDay, minute); // TODO merge calendar usage between two fields?
                        String formattedDate = format.format(cal.getTime());

                        dateText.setText(formattedDate);
                    }
                }, mHour, mMinute, DateFormat.is24HourFormat(ReportIncident.this));
        timePickerDialog.show();
    }

    /* PlaceSelectionListener Implementation */

    @Override
    public void onPlaceSelected(Place place) {
        if (this.location != null) {
            CharSequence name = place.getName();

            if (name != null) {
                locationText.setText(name);
            } else {
                locationText.setText(place.getAddress()); // TODO is a null check necessary?
            }
        }
    }

    @Override
    public void onError(Status status) {
        // TODO handle errors.
    }
}
