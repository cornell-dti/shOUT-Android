package org.cornelldti.shout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.cornelldti.shout.goout.ZoomLevel;
import org.cornelldti.shout.speakout.Report;
import org.cornelldti.shout.util.AndroidUtil;
import org.cornelldti.shout.util.LayoutUtil;

import java.util.Calendar;

/**
 * Created by Evan Welsh on 3/17/18.
 */

public class ReportViewDialogFragment extends AppCompatDialogFragment {

    private static final String TAG = "ReportView";

    private static final String REPORT_ARGUMENT = "report";
    private static final String LATLNG_ARGUMENT = "latLng";

    private Calendar mCalendar = Calendar.getInstance();
    private LatLng mLatLng;
    private Report mReport;

<<<<<<< Updated upstream
    @NonNull
    private ShowMapCallback mShowMapCallback = latLng -> {
=======
<<<<<<< HEAD
    private java.text.DateFormat timeFormatter;
    private java.text.DateFormat dateFormatter;

    private int returnPage;
    private Report report;
    private ImageButton backButton;

    private ShowMapCallback showMapCallback = latLng -> {
=======
    @NonNull
    private ShowMapCallback mShowMapCallback = latLng -> {
>>>>>>> origin/master
>>>>>>> Stashed changes
    };

    public interface ShowMapCallback {
        void showMap(LatLng latLng);
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof ShowMapCallback) {
            mShowMapCallback = (ShowMapCallback) context;
        }

        super.onAttach(context);
    }

    public static ReportViewDialogFragment newInstance(Report report, LatLng latLng) {
        ReportViewDialogFragment dialog = new ReportViewDialogFragment();

        Bundle args = new Bundle();
        args.putDoubleArray(LATLNG_ARGUMENT, new double[]{latLng.latitude, latLng.longitude});
        args.putSerializable(REPORT_ARGUMENT, report);

        dialog.setArguments(args);

        return dialog;
    }

    public static ReportViewDialogFragment newInstance(Report report) {
        ReportViewDialogFragment dialog = new ReportViewDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(REPORT_ARGUMENT, report);

        dialog.setArguments(args);

        return dialog;
    }

    @SuppressLint({"ClickableViewAccessibility", "PrivateResource"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View reportDialogView = inflater.inflate(R.layout.report_view_dialog, container, false);

        AppBarLayout appBarLayout = reportDialogView.findViewById(R.id.report_view_appbar);

        /* Ensure we don't overlap with the status bar. */

        appBarLayout.setPadding(0, LayoutUtil.getStatusBarHeight(AndroidUtil.getContext(container, this)), 0, 0);

        /* Setup toolbar buttons */

        ImageButton button = reportDialogView.findViewById(R.id.report_view_close_button);

        button.setOnClickListener(v -> {
            dismiss();
        });

        /* Disable options menu (we don't use it) */

        setHasOptionsMenu(false);

        Context context = getContext();

        java.text.DateFormat dateFormatter = DateFormat.getDateFormat(context);
        java.text.DateFormat timeFormatter = DateFormat.getTimeFormat(context);

        /* Retrieve miscellaneous views... */

<<<<<<< Updated upstream
        TextView titleTextView = reportDialogView.findViewById(R.id.report_view_title_text_view);
        TextView bodyTextView = reportDialogView.findViewById(R.id.report_view_body_text_view);
        TextView timeTextView = reportDialogView.findViewById(R.id.report_view_time_text_view);

        TextView locationTextView = reportDialogView.findViewById(R.id.report_view_address_text_view);

=======
<<<<<<< HEAD
        titleTextView = reportDialogView.findViewById(R.id.report_view_title_text_view);
        bodyTextView = reportDialogView.findViewById(R.id.report_view_body_text_view);
        timeTextView = reportDialogView.findViewById(R.id.report_view_time_text_view);
        backButton = reportDialogView.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        locationTextView = reportDialogView.findViewById(R.id.report_view_address_text_view);
        // timeTextView = reportDialogView.findViewById(R.id.report_time_spinner_text_view);
        mapView = reportDialogView.findViewById(R.id.report_view_map_view);
        if(latLng.latitude != 0 && latLng.longitude != 0) {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
        }
=======
        TextView titleTextView = reportDialogView.findViewById(R.id.report_view_title_text_view);
        TextView bodyTextView = reportDialogView.findViewById(R.id.report_view_body_text_view);
        TextView timeTextView = reportDialogView.findViewById(R.id.report_view_time_text_view);

        TextView locationTextView = reportDialogView.findViewById(R.id.report_view_address_text_view);

>>>>>>> Stashed changes
        MapView mapView = reportDialogView.findViewById(R.id.report_view_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
>>>>>>> origin/master

        /* Setup the mLatLng selector if a mLatLng was passed to the dialog... */

        if (this.mReport != null) {
            titleTextView.setText(this.mReport.getTitle());
            bodyTextView.setText(this.mReport.getBody());
            locationTextView.setText(this.mReport.getLocation());

            long timestamp = this.mReport.getTimestamp();

            String time = timeFormatter.format(timestamp);
            String date = dateFormatter.format(timestamp);

            this.mCalendar.setTimeInMillis(timestamp);

            Calendar calendar = Calendar.getInstance();

            boolean thisYear = calendar.get(Calendar.YEAR) == this.mCalendar.get(Calendar.YEAR);
            boolean today = thisYear && calendar.get(Calendar.DAY_OF_YEAR) == this.mCalendar.get(Calendar.DAY_OF_YEAR);
            boolean yesterday = thisYear && calendar.get(Calendar.DAY_OF_YEAR) == this.mCalendar.get(Calendar.DAY_OF_YEAR) + 1;

            if (today) {
                timeTextView.setText(getResources().getString(R.string.report_time_today, time));
            } else if (yesterday) {
                timeTextView.setText(getResources().getString(R.string.report_time_yesterday, time));
            } else {
                timeTextView.setText(getResources().getString(R.string.report_time_date, date, time));
            }
        }

        if (this.mLatLng != null) {
            mapView.getMapAsync((map) -> {
                map.addMarker(new MarkerOptions().position(this.mLatLng));

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, ZoomLevel.DEFAULT));

                map.setOnMapClickListener(latLng -> {
                    ReportViewDialogFragment.this.dismiss();

                    mShowMapCallback.showMap(latLng);
                });
            });

        }


        return reportDialogView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Get passed latLng from bundle */

        Bundle bundle = getArguments();

        if (bundle != null) {
            double[] loc = bundle.getDoubleArray(LATLNG_ARGUMENT);

            if (loc != null) {
                this.mLatLng = new LatLng(loc[0], loc[1]);
            }

            this.mReport = (Report) bundle.getSerializable(REPORT_ARGUMENT);
        }

        /* Set the style of this dialog. */

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);

        Activity activity = getActivity();

        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void dismiss() {
        // TODO find less hacky solution
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
}
