package org.cornelldti.shout.speakout;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.cornelldti.shout.Page;
import org.cornelldti.shout.R;
import org.cornelldti.shout.ReportViewDialogFragment;
import org.cornelldti.shout.ShoutRealtimeDatabase;
import org.cornelldti.shout.ShoutTabFragment;
import org.cornelldti.shout.util.AndroidUtil;
import org.cornelldti.shout.util.LayoutUtil;

public class SpeakOutFragment extends ShoutTabFragment implements AdapterChangedCallback, DataLoadedCallback {

    private static final String TAG = "SpeakOutFragment";
    private static final int PROGRESS_SPINNER_CHILD = 0;
    private static final int RECYCLER_VIEW_CHILD = 1;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FilterOption mFilterOption = FilterOption.ALL_REPORTS;
    private ViewFlipper mViewFlipper;

    public SpeakOutFragment() {
    }

    @Override
    public void adapterChanged() {
        if (mViewFlipper != null) {
            mViewFlipper.setDisplayedChild(PROGRESS_SPINNER_CHILD);
        }
    }

    @Override
    public void dataLoaded() {
        if (mViewFlipper != null) {
            mViewFlipper.setDisplayedChild(RECYCLER_VIEW_CHILD);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Setup reports recycler view... */

        final View speakoutFragment = inflater.inflate(R.layout.speakout_fragment, container, false);
        final RecyclerView recyclerView = speakoutFragment.findViewById(R.id.recycler_view);
        final ProgressBar progressSpinner = speakoutFragment.findViewById(R.id.speak_out_progress_spinner);
        mViewFlipper = speakoutFragment.findViewById(R.id.speak_out_view_flipper);
        mViewFlipper.setDisplayedChild(PROGRESS_SPINNER_CHILD);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        final SpeakOutAdapter adapter = SpeakOutAdapter.construct(this, this, (eventAdapter, holder) -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ShoutRealtimeDatabase.REPORT_LOCATIONS_KEY);
            GeoFire geoFire = new GeoFire(ref);

            if (eventAdapter == null) return;

            String id = eventAdapter.getDocumentId(holder.getAdapterPosition());

            if (id != null) {
                geoFire.getLocation(id, new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (holder.report == null) return;

                        ReportViewDialogFragment dialog = ReportViewDialogFragment.newInstance(
                                holder.report,
                                new LatLng(location.latitude, location.longitude)
                        );
                        showDialog(dialog);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (holder.report == null) return;

                        ReportViewDialogFragment dialog = ReportViewDialogFragment.newInstance(holder.report);
                        showDialog(dialog);
                    }

                    private void showDialog(ReportViewDialogFragment dialog) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
                    }
                });
            } else if (holder.report != null) {
                ReportViewDialogFragment dialog = ReportViewDialogFragment.newInstance(holder.report);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
            }

        }, speakoutFragment.getContext());

        recyclerView.addOnScrollListener(adapter.listener());
        recyclerView.setAdapter(adapter);

        ViewCompat.setNestedScrollingEnabled(recyclerView, false); // enables "fast" scrolling

        // TODO decorate

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        /* Fix padding issues w/ the status bar positioning */

        Context context = AndroidUtil.getContext(container, this);

        AppBarLayout toolbar = speakoutFragment.findViewById(R.id.appbar);


        if (context != null) {
            final int statusbarSize = LayoutUtil.getStatusBarHeight(context);

            if (statusbarSize > 0) {
                toolbar.setPadding(0, statusbarSize, 0, 0);
            }
        }

        mSwipeRefreshLayout = speakoutFragment.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh items
            adapter.refreshItems(success -> mSwipeRefreshLayout.setRefreshing(false));
        });

        ImageButton filterButton = toolbar.findViewById(R.id.filter_reports_button);

        if (context != null && filterButton != null) {
            filterButton.setOnClickListener(v2 -> {
                FilterDialogFragment dialog = FilterDialogFragment.construct(context, mCurrentFilterOption -> {
                    mFilterOption = mCurrentFilterOption;
                    adapter.filter(mFilterOption);
                }, mFilterOption);

                FragmentManager manager = getFragmentManager();

                if (manager != null) {
                    dialog.show(manager, FilterDialogFragment.class.getSimpleName());
                }
            });
        } else {
            Log.e(TAG, "Unable to find filter button.");
        }

        ImageButton infoButton = toolbar.findViewById(R.id.shout_info_button);

        if (context != null && infoButton != null) {
            infoButton.setOnClickListener(v2 -> {
                AlertDialog dialog = new AlertDialog.Builder(context, R.style.DialogStyle)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.legal_tos)
                        .setPositiveButton(android.R.string.ok, (owner, which) -> owner.dismiss()).create();
                dialog.show();
            });
        } else {
            Log.e(TAG, "Unable to find info button.");
        }

        return speakoutFragment;
    }


    @Override
    public void onDisplayed(Bundle bundle) {

    }

    @Override
    public void onRemoved() {

    }


}