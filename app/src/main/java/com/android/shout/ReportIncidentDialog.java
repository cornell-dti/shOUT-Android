package com.android.shout;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.shout.places.PlaceAutocompleteAdapter;
import com.android.shout.util.LayoutUtil;
import com.android.shout.util.LocationUtil;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ReportIncidentDialog extends AppCompatDialogFragment implements PlaceSelectionListener {

    private MultiAutoCompleteTextView locationEdit;
    private TextView dateSelector, timeSelector;
    private EditText editPostTitle, editPostText;

    private LatLng location;
    private boolean locationLocked; // TODO Ask design. See onCreate...

    public ReportIncidentDialog() {
    }

    public static ReportIncidentDialog newInstance(LatLng latLng) {
        ReportIncidentDialog dialog = new ReportIncidentDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDoubleArray("location", new double[]{latLng.latitude, latLng.longitude});
        dialog.setArguments(args);

        return dialog;
    }

    public static ReportIncidentDialog newInstance() {
        return new ReportIncidentDialog();
    }

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.report_incident_dialog, container, false);

        AppBarLayout toolbar = v.findViewById(R.id.dialogToolbar);

        /* Ensure we don't overlap with the status bar. */

        toolbar.setPadding(0, LayoutUtil.getStatusBarHeight(getContext()), 0, 0);

       /* Setup toolbar buttons */

        ImageButton closeButton = v.findViewById(R.id.button_close);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button saveButton = v.findViewById(R.id.button_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO save report
                dismiss();
            }
        });

        setHasOptionsMenu(false);

        editPostTitle = v.findViewById(R.id.editPostTitle);
        editPostText = v.findViewById(R.id.editPostText);

        dateSelector = v.findViewById(R.id.dateSpinner);
        timeSelector = v.findViewById(R.id.timeSpinner);

        // TODO remove onKey?

        /* Catch all interaction with the date selector TextView */

        dateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateControlClicked(v);
            }

        });

        dateSelector.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                onDateControlClicked(v);
                return true;
            }
        });

        /* Catch all interaction with the time selector TextView */

        timeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeControlClicked(v);
            }
        });

        timeSelector.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                onTimeControlClicked(v);
                return true;
            }
        });


        /* Set the selectors to the current time/date using the locale-set format */
        java.text.DateFormat format = DateFormat.getDateFormat(getActivity());
        Calendar cal = Calendar.getInstance();
        String formattedDate = format.format(cal.getTime());
        dateSelector.setText(formattedDate);
        format = DateFormat.getTimeFormat(getActivity());
        String formattedTime = format.format(cal.getTime());
        timeSelector.setText(formattedTime);

        /* Setup location editing... */

        locationEdit = v.findViewById(R.id.locationEdit);

        /* Setup location selection to autocomplete... */
        // TODO fix this

        GeoDataClient client = Places.getGeoDataClient(getActivity(), null);
        locationEdit.setAdapter(new PlaceAutocompleteAdapter(getActivity(), client, LocationUtil.getIthacaBounds(), null));

        /* Setup the location selector if a location was passed to the dialog... */

        if (this.location != null) {
            Address address = getAddressForLocation(getContext(), this.location);

            if (address != null) {
                locationEdit.setText(address.getAddressLine(0));
            } else {
                locationEdit.setText(this.location.toString());
            }
        }

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Get passed location from bundle */

        Bundle bundle = getArguments();

        if (bundle != null) {
            double[] loc = bundle.getDoubleArray("location");

            if (loc != null) {
                this.location = new LatLng(loc[0], loc[1]);
                this.locationLocked = true; // TODO
            }
        }

        /* Set the style of this dialog. */

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
    }

    // TODO utilize
    public void saveReport() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("messages");
        if (editPostTitle.getText().toString().isEmpty() || editPostText.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Make sure to fill in post title and message", Toast.LENGTH_LONG).show();
        } else {
            // todo validate getText()
            Message m = new Message(editPostText.getText().toString(), dateSelector.getText().toString(), timeSelector.getText().toString(), editPostTitle.getText().toString());
            String ID = database.push().getKey();
            m.setID(ID);
            database.child(ID).setValue(m);
            startActivity(new Intent(getContext(), SuggestionHandler.class));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Deprecated
    public void onLocationControlClicked(View v) {
        Intent intent = null;
        try {
            intent = new PlaceAutocomplete.IntentBuilder
                    (PlaceAutocomplete.MODE_FULLSCREEN)
                    .setBoundsBias(LocationUtil.getIthacaBounds())
                    .build(getActivity());
            startActivityForResult(intent, 1000);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getContext(), "Could not start the location search.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void onDateControlClicked(View v) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        java.text.DateFormat format = DateFormat.getDateFormat(getActivity());
                        Calendar cal = Calendar.getInstance();
                        cal.clear(); // TODO is this necessary?
                        cal.set(year, monthOfYear, dayOfMonth); // NOTE: these months are 0-based
                        String formattedDate = format.format(cal.getTime());

                        dateSelector.setText(formattedDate);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }


    private void onTimeControlClicked(View v) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        java.text.DateFormat format = DateFormat.getTimeFormat(getActivity());
                        Calendar cal = Calendar.getInstance();
                        cal.clear(); // TODO is this necessary?
                        cal.set(0, 0, 0, hourOfDay, minute); // TODO merge calendar usage between two fields?
                        String formattedDate = format.format(cal.getTime());

                        timeSelector.setText(formattedDate);
                    }
                }, mHour, mMinute, DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.show();
    }

    /* PlaceSelectionListener Implementation */
    // TODO currently unutilized...

    @Override
    public void onPlaceSelected(Place place) {
        if (this.location != null) {
            CharSequence name = place.getName();

            if (name != null) {
                locationEdit.setText(name);
            } else {
                locationEdit.setText(place.getAddress()); // TODO is a null check necessary?
            }
        }
    }

    @Override
    public void onError(Status status) {
        // TODO handle errors.
    }


}
