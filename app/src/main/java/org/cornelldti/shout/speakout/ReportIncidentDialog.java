package org.cornelldti.shout.speakout;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.playservices.placecomplete.PlaceAutocompleteAdapter;

import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;
import org.cornelldti.shout.util.LocationUtil;

import java.util.Calendar;


public class ReportIncidentDialog extends AppCompatDialogFragment {

    private static final String TAG = "ReportIncident";
    private AutoCompleteTextView locationEdit;
    private TextView dateSelector, timeSelector;
    private EditText editReportTitle, editReportDetails;

    private Calendar calendar = Calendar.getInstance();

    private LatLng location;
    private boolean locationLocked; // TODO Ask design. See onCreate...

    public ReportIncidentDialog() {
    }

    public static ReportIncidentDialog newInstance(LatLng latLng) {
        ReportIncidentDialog dialog = new ReportIncidentDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDoubleArray("website", new double[]{latLng.latitude, latLng.longitude});
        dialog.setArguments(args);

        return dialog;
    }

    public static ReportIncidentDialog newInstance() {
        return new ReportIncidentDialog();
    }

    /**
     * Saves the current report data to the database.
     *
     * @param listener - A listener to perform actions when the data has been written.
     */
    public void saveReport(OnCompleteListener<Void> listener) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("unapproved_reports");
        if (editReportTitle.getText().toString().isEmpty() || editReportDetails.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Make sure to fill in post title and message", Toast.LENGTH_LONG).show();
        } else {
            if (editReportTitle.getText() != null) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    UnapprovedReport m = new UnapprovedReport(
                            editReportDetails.getText().toString(),
                            editReportTitle.getText().toString(),
                            user.getUid(),
                            locationEdit.getText().toString(),
                            location,
                            calendar.getTimeInMillis());
                    String id = FirebaseDatabase.getInstance().getReference("approved_reports").push().getKey();
                    database.child(id).setValue(m).addOnCompleteListener(listener);
                } else {
                    Toast.makeText(getContext(), "Cannot connect to shOUT.", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            } else {
                Toast.makeText(getContext(), "Please enter a summary.", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.report_dialog, container, false);

        AppBarLayout toolbar = v.findViewById(R.id.report_toolbar);

        /* Ensure we don't overlap with the status bar. */

        toolbar.setPadding(0, LayoutUtil.getStatusBarHeight(getContext()), 0, 0);

       /* Setup toolbar buttons */

        ImageButton closeButton = v.findViewById(R.id.report_button_close);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO catch any errors while doing this
                /* Manually hide the keyboard to ensure it doesn't stick around */
                InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                if (manager != null) {
                    manager.hideSoftInputFromWindow(editReportDetails.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                dismiss();
            }
        });

        final Button saveButton = v.findViewById(R.id.report_button_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReport(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (manager != null) {
                            manager.hideSoftInputFromWindow(editReportDetails.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }

                        dismiss(); // todo
                    }
                });
            }
        });

        setHasOptionsMenu(false);

        editReportTitle = v.findViewById(R.id.report_title_edit_text);
        editReportDetails = v.findViewById(R.id.report_details_edit_text);
        dateSelector = v.findViewById(R.id.report_date_spinner_text_view);
        timeSelector = v.findViewById(R.id.report_time_spinner_text_view);

        // TODO Can we remove the onKey listeners?
        // TODO Test the app with a physical keyboard to be sure.

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

        /* Setup website editing... */

        locationEdit = v.findViewById(R.id.report_location_edit_text);

        /* Setup website selection to autocomplete... */
        // TODO fix this

        GeoDataClient client = Places.getGeoDataClient(getActivity(), null);
        final PlaceAutocompleteAdapter adapter = new PlaceAutocompleteAdapter(getActivity(), client, LocationUtil.getIthacaBounds(), null);
        locationEdit.setAdapter(adapter);
        locationEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AutocompletePrediction item = adapter.getItem(position);
                final String placeId;
                if (item != null) {
                    placeId = item.getPlaceId();

                    final CharSequence primaryText = item.getPrimaryText(null);

                    Log.i(TAG, "Autocomplete item selected: " + primaryText);

                    // TODO store this client
                    GeoDataClient mGeoDataClient = Places.getGeoDataClient(getContext(), null);
                    Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
                    placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
                } else {
                    Log.e(TAG, "No autocomplete item found at position: " + position);
                }
            }
        });


        /* Setup the website selector if a website was passed to the dialog... */

        if (this.location != null) {
            Address address = LocationUtil.getAddressForLocation(getContext(), this.location);
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

        /* Get passed website from bundle */

        Bundle bundle = getArguments();

        if (bundle != null) {
            double[] loc = bundle.getDoubleArray("website");

            if (loc != null) {
                this.location = new LatLng(loc[0], loc[1]);
                this.locationLocked = true; // TODO
            }
        }

        /* Set the style of this dialog. */

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
    }

    /* Handles setting the report location after place selection from the autofill list */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                final Place place = places.get(0);

                // Format details of the place for display and show it in a TextView.
                location = place.getLatLng();

                // TODO ensure this is no longer needed.
                // locationEdit.setText(place.getName());

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
            }
        }
    };


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        /* This ensures the dialog fills the entire screen... */
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void onDateControlClicked(View v) {
        /* Get current date/time */
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
                        calendar.set(year, monthOfYear, dayOfMonth);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }


    private void onTimeControlClicked(View v) {
        /* Get current date/time */
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

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
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                    }
                }, mHour, mMinute, DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.show();
    }

}
