package org.cornelldti.shout.speakout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
<<<<<<< Updated upstream
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
=======
<<<<<<< HEAD
=======
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
>>>>>>> origin/master
>>>>>>> Stashed changes
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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

public class SpeakOutFragment extends ShoutTabFragment {

    private static final String TAG = "SpeakOutFragment";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FilterOption mFilterOption = FilterOption.ALL_REPORTS;

    public SpeakOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Setup reports recycler view... */

        final View speakoutFragment = inflater.inflate(R.layout.speakout_fragment, container, false);
        final RecyclerView recyclerView = speakoutFragment.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);


        final SpeakOutAdapter adapter = SpeakOutAdapter.construct(this, (eventAdapter, holder) -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ShoutRealtimeDatabase.REPORT_LOCATIONS_KEY);
            GeoFire geoFire = new GeoFire(ref);

            if (eventAdapter == null) return;

            String id = eventAdapter.getId(holder.getAdapterPosition());

            if (id != null) {
                geoFire.getLocation(id, new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (holder.report == null) return;

                        ReportViewDialogFragment dialog = ReportViewDialogFragment.newInstance(
                                holder.report,
                                new LatLng(location.latitude, location.longitude),
                                Page.SPEAK_OUT
                        );
                        showDialog(dialog);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (holder.report == null) return;

                        ReportViewDialogFragment dialog = ReportViewDialogFragment.newInstance(holder.report, Page.SPEAK_OUT);
                        showDialog(dialog);
                    }

                    private void showDialog(ReportViewDialogFragment dialog) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
                    }
                });
            } else if (holder.report != null) {
                ReportViewDialogFragment dialog = ReportViewDialogFragment.newInstance(holder.report, Page.SPEAK_OUT);
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

        ImageButton b = toolbar.findViewById(R.id.filter_reports_button);

        if (b != null) {
            b.setOnClickListener(v2 -> {
                FilterDialog dialog = FilterDialog.construct(mCurrentFilterOption -> {
                    mFilterOption = mCurrentFilterOption;
                    adapter.filter(mFilterOption);
                }, mFilterOption);

                FragmentManager manager = getFragmentManager();

                if (manager != null) {
                    dialog.show(manager, FilterDialog.class.getSimpleName());
                }
            });
        } else {
            Log.e(TAG, "Unable to find filter button.");
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
