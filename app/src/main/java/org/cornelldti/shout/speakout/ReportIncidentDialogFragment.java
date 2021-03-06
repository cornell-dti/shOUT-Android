package org.cornelldti.shout.speakout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import org.cornelldti.shout.MainActivity;
import org.cornelldti.shout.R;
import org.cornelldti.shout.util.AndroidUtil;
import org.cornelldti.shout.util.LayoutUtil;
import org.cornelldti.shout.util.LocationUtil;

import java.util.Calendar;

/**
 * A dialog for submitting reports.
 */
public class ReportIncidentDialogFragment extends AppCompatDialogFragment {

    private static final String TAG = "ReportIncident";
    private AutoCompleteTextView locationEdit;
    private TextView dateSelector, timeSelector;
    private EditText editReportTitle, editReportDetails;

    private Calendar calendar = Calendar.getInstance();
    private LatLng location;

    private int returnPage;

    public ReportIncidentDialogFragment() {
    }

    public static ReportIncidentDialogFragment newInstance(LatLng latLng, int returnPage) {
        ReportIncidentDialogFragment dialog = new ReportIncidentDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDoubleArray("location", new double[]{latLng.latitude, latLng.longitude});
        args.putInt("returnPage", returnPage); // TODO find a better method to fix this UI issue
        dialog.setArguments(args);

        return dialog;
    }

    public static ReportIncidentDialogFragment newInstance(int returnPage) {
        ReportIncidentDialogFragment dialog = new ReportIncidentDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("returnPage", returnPage);
        dialog.setArguments(args);

        return dialog;
    }

    /**
     * Saves the current report data to the database.
     *
     * @param listener - A listener to perform actions when the data has been written.
     */
    public void saveReport(OnCompleteListener<Void> listener) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("unapproved_reports");

        if (TextUtils.isEmpty(editReportTitle.getText())) {
            Toast.makeText(getContext(), "Make sure to fill in post title", Toast.LENGTH_LONG).show();
        } else {
            if (editReportTitle.getText() != null) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    if(locationEdit.getText().toString().isEmpty())
                    {
                        UnapprovedReport m = new UnapprovedReport(
                                editReportDetails.getText().toString(),
                                editReportTitle.getText().toString(), user.getUid(),
                                calendar.getTimeInMillis());
                        String id = FirebaseDatabase.getInstance().getReference("unapproved_reports").push().getKey();
                        database.child(id).setValue(m).addOnCompleteListener(listener);
                    }
                    else {
                        UnapprovedReport m = new UnapprovedReport(
                                editReportDetails.getText().toString(),
                                editReportTitle.getText().toString(),
                                user.getUid(),
                                locationEdit.getText().toString(),
                                location, calendar.getTimeInMillis());
                        String id = FirebaseDatabase.getInstance().getReference("unapproved_reports").push().getKey();
                        database.child(id).setValue(m).addOnCompleteListener(listener);
                    }
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
        View reportDialogView = inflater.inflate(R.layout.report_dialog, container, false);

        AppBarLayout toolbar = reportDialogView.findViewById(R.id.report_toolbar);

        /* Ensure we don't overlap with the status bar. */

        Context context = AndroidUtil.getContext(container, this);

        if (context != null) {
            toolbar.setPadding(0, LayoutUtil.getStatusBarHeight(context), 0, 0);
        }

        /* Setup toolbar buttons */

        ImageButton closeButton = reportDialogView.findViewById(R.id.report_button_close);

        closeButton.setOnClickListener(view -> {
            /* Manually hide the keyboard to ensure it doesn't stick around */
            AndroidUtil.hideSoftKeyboard(getActivity(), editReportDetails.getWindowToken());
            dismiss();
        });

        final Button saveButton = reportDialogView.findViewById(R.id.report_button_save);

        saveButton.setOnClickListener(view -> saveReport(task -> {
            AndroidUtil.hideSoftKeyboard(getActivity(), editReportDetails.getWindowToken());
            dismiss();
        }));

        /* Disable options menu (we don't use it) */

        setHasOptionsMenu(false);

        /* Retrieve miscellaneous views... */

        editReportTitle = reportDialogView.findViewById(R.id.report_title_edit_text);
        editReportDetails = reportDialogView.findViewById(R.id.report_details_edit_text);

        dateSelector = reportDialogView.findViewById(R.id.report_date_spinner_text_view);
        timeSelector = reportDialogView.findViewById(R.id.report_time_spinner_text_view);

        LinearLayout dateSelectorClickArea = reportDialogView.findViewById(R.id.report_date_spinner_click_area);
        LinearLayout timeSelectorClickArea = reportDialogView.findViewById(R.id.report_time_spinner_click_area);

        // TODO Can we remove the onKey listeners?
        // TODO Test the app with a physical keyboard to be sure.

        /* Catch all interaction with the date selector TextView */

        dateSelectorClickArea.setOnClickListener(this::onDateControlClicked);

        dateSelectorClickArea.setOnKeyListener((view, keyCode, event) -> {
            onDateControlClicked(view);
            return true;
        });

        /* Catch all interaction with the time selector TextView */

        timeSelectorClickArea.setOnClickListener(this::onTimeControlClicked);

        timeSelectorClickArea.setOnKeyListener((view, keyCode, event) -> {
            onTimeControlClicked(view);
            return true;
        });

        // TODO Should they be set to the current time?

        /* Set the selectors to the current time/date using the locale-set format */
        java.text.DateFormat format = DateFormat.getDateFormat(getActivity());
        Calendar cal = Calendar.getInstance();
        String formattedDate = format.format(cal.getTime());
        dateSelector.setText(formattedDate);

        format = DateFormat.getTimeFormat(getActivity());
        String formattedTime = format.format(cal.getTime());
        timeSelector.setText(formattedTime);

        /* Setup location editing... */

        locationEdit = reportDialogView.findViewById(R.id.report_location_edit_text);

        /* Setup location selection to autocomplete... */
        // TODO fix this

        if (context != null) {
            final GeoDataClient client = Places.getGeoDataClient(context);
            final PlaceAutocompleteAdapter adapter = new PlaceAutocompleteAdapter(context, client, LocationUtil.getIthacaBounds(), null);

            locationEdit.setAdapter(adapter);
            locationEdit.setOnItemClickListener((parent, view, position, id) -> {
                final AutocompletePrediction item = adapter.getItem(position);

                if (item != null) {
                    final String placeId = item.getPlaceId();
                    final CharSequence primaryText = item.getPrimaryText(null);

                    Log.i(TAG, "Autocomplete item selected: " + primaryText);

                    Task<PlaceBufferResponse> placeResult = client.getPlaceById(placeId);
                    placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
                } else {
                    Log.e(TAG, "No autocomplete item found at position: " + position);
                }
            });
        } else {
            // TODO Hide location field or allow user to input anything?
        }

        /* Setup the location selector if a location was passed to the dialog... */

        if (this.location != null) {
            Address address = LocationUtil.getAddressForLocation(getContext(), this.location);
            if (address != null) {
                locationEdit.setText(address.getAddressLine(0));
            } else {
                locationEdit.setText(this.location.toString());
            }
        }

        return reportDialogView;
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
            }

            this.returnPage = bundle.getInt("returnPage");
        }

        /* Set the style of this dialog. */

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);

        Activity activity = getActivity();

        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /* Handles setting the report location after place selection from the autofill list */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback = new OnCompleteListener<PlaceBufferResponse>() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                final Place place = places.get(0);

                /* Format details of the place for display and show it in a TextView. */
                location = place.getLatLng();

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
            }
        }
    };

    @Override
    public void dismiss() {
        // TODO find less hacky solution
        Activity activity = getActivity();

        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setStatusBarColor(returnPage);
        }

        super.dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        /* This ensures the dialog fills the entire screen... */
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void onDateControlClicked(View clickedView) {
        /* Get current date/time */
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        Context context = AndroidUtil.getContext(clickedView, this);

        if (context != null) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
                java.text.DateFormat format = DateFormat.getDateFormat(getActivity());
                Calendar cal = Calendar.getInstance();
                cal.clear(); // TODO is this necessary?
                cal.set(year, monthOfYear, dayOfMonth); // NOTE: these months are 0-based
                String formattedDate = format.format(cal.getTime());

                dateSelector.setText(formattedDate);
                calendar.set(year, monthOfYear, dayOfMonth);
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    }


    private void onTimeControlClicked(View clickedView) {
        /* Get current date/time */
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        Context context = AndroidUtil.getContext(clickedView, this);

        if (context != null) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                    (view, hourOfDay, minute) -> {
                        java.text.DateFormat format = DateFormat.getTimeFormat(getActivity());
                        Calendar cal = Calendar.getInstance();
                        cal.clear(); // TODO is this necessary?
                        cal.set(0, 0, 0, hourOfDay, minute); // TODO merge calendar usage between two fields?
                        String formattedDate = format.format(cal.getTime());

                        timeSelector.setText(formattedDate);
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                    }, mHour, mMinute, DateFormat.is24HourFormat(getActivity()));
            timePickerDialog.show();
        }
    }

}
