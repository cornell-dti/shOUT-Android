package org.cornelldti.shout;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.cornelldti.shout.speakout.Report;
import org.cornelldti.shout.util.LayoutUtil;

import java.util.Calendar;

/**
 * Created by Evan Welsh on 3/17/18.
 */

public class ReportViewDialog extends AppCompatDialogFragment {

    private static final String TAG = "ReportView";

    private TextView timeTextView, titleTextView, bodyTextView, locationTextView;
    private MapView mapView;

    private Calendar calendar = Calendar.getInstance();
    private LatLng latLng;

    private java.text.DateFormat timeFormatter;
    private java.text.DateFormat dateFormatter;

    private int returnPage;
    private Report report;

    public ReportViewDialog() {
    }

    public static ReportViewDialog newInstance(Report report, LatLng latLng, int returnPage) {
        ReportViewDialog dialog = new ReportViewDialog();

        Bundle args = new Bundle();
        args.putDoubleArray("latLng", new double[]{latLng.latitude, latLng.longitude});
        args.putInt("returnPage", returnPage); // TODO find a better method to fix this UI issue

        args.putSerializable("report", report);

        dialog.setArguments(args);

        return dialog;
    }

    public static ReportViewDialog newInstance(Report report, int returnPage) {
        ReportViewDialog dialog = new ReportViewDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("returnPage", returnPage);
        args.putSerializable("report", report);

        dialog.setArguments(args);

        return dialog;
    }

    @SuppressLint({"ClickableViewAccessibility", "PrivateResource"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View reportDialogView = inflater.inflate(R.layout.report_view_dialog, container, false);

        AppBarLayout appBarLayout = reportDialogView.findViewById(R.id.report_view_appbar);

        /* Ensure we don't overlap with the status bar. */

        appBarLayout.setPadding(0, LayoutUtil.getStatusBarHeight(getContext()), 0, 0);

       /* Setup toolbar buttons */
        // TODO add close button

        /* Disable options menu (we don't use it) */

        setHasOptionsMenu(false);

        Context context = getContext();

        this.dateFormatter = DateFormat.getDateFormat(context);
        this.timeFormatter = DateFormat.getTimeFormat(context);

        /* Retrieve miscellaneous views... */

        titleTextView = reportDialogView.findViewById(R.id.report_view_title_text_view);
        bodyTextView = reportDialogView.findViewById(R.id.report_view_body_text_view);
        timeTextView = reportDialogView.findViewById(R.id.report_view_time_text_view);

        locationTextView = reportDialogView.findViewById(R.id.report_view_address_text_view);
        // timeTextView = reportDialogView.findViewById(R.id.report_time_spinner_text_view);
        mapView = reportDialogView.findViewById(R.id.report_view_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        /* Setup the latLng selector if a latLng was passed to the dialog... */

        if (this.report != null) {
            this.titleTextView.setText(this.report.getTitle());
            this.bodyTextView.setText(this.report.getBody());
            this.locationTextView.setText(this.report.getLocation());

            long timestamp = this.report.getTimestamp();

            String time = this.timeFormatter.format(timestamp);
            String date = this.dateFormatter.format(timestamp);

            this.calendar.setTimeInMillis(timestamp);

            Calendar calendar = Calendar.getInstance();

            boolean thisYear = calendar.get(Calendar.YEAR) == this.calendar.get(Calendar.YEAR);
            boolean today = thisYear && calendar.get(Calendar.DAY_OF_YEAR) == this.calendar.get(Calendar.DAY_OF_YEAR);
            boolean yesterday = thisYear && calendar.get(Calendar.DAY_OF_YEAR) == this.calendar.get(Calendar.DAY_OF_YEAR) + 1;

            if (today) {
                this.timeTextView.setText(getResources().getString(R.string.report_time_today, time));
            } else if (yesterday) {
                this.timeTextView.setText(getResources().getString(R.string.report_time_yesterday, time));
            } else {
                this.timeTextView.setText(getResources().getString(R.string.report_time_date, date, time));
            }
        }

        if (this.latLng != null) {
            this.mapView.getMapAsync((map) -> {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(this.latLng, 17));

                map.addMarker(new MarkerOptions().position(this.latLng));
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
            double[] loc = bundle.getDoubleArray("latLng");

            if (loc != null) {
                this.latLng = new LatLng(loc[0], loc[1]);
            }

            this.returnPage = bundle.getInt("returnPage");

            this.report = (Report) bundle.getSerializable("report");


        }

        /* Set the style of this dialog. */

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // this.statusBarColor = getActivity().getWindow().getStatusBarColor();
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
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
