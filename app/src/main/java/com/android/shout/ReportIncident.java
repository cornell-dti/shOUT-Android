package com.android.shout;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ReportIncident extends AppCompatActivity {

    EditText postTitle;
    EditText postText;

    private TextView locationText, dateText, timeText;

    private LatLng location;
    private boolean locationLocked; // todo prevent user changes...?

    private static Address getAddressForLocation(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address address = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // todo 1 may not be enough ;)
            if (addresses.size() > 0) {
                address = addresses.get(0);
            }
        } catch (IOException e) {
            // todo handle
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


        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        // getSupportActionBar().setCustomView(R.layout.actionbar_layout);
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
            startActivity(new Intent(ReportIncident.this, MainActivity.class));
        }
    }

    public void cancel(View v) {
        // startActivity(new Intent(ReportIncident.this, MainActivity.class));
        finish();
        // todo set custom animations
    }

    public void setLocation(View v) {

    }

    public void setDate(View v) {
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
                        // todo use locale
                        dateText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void setTime(View v) {
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
                        if (hourOfDay > 12) {
                            timeText.setText((hourOfDay - 12) + ":" + minute + "pm");
                        }
                        // todo use locale format and resource
                        timeText.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }
}
