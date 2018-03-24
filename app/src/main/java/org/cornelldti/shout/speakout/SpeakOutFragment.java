package org.cornelldti.shout.speakout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.cornelldti.shout.Page;
import org.cornelldti.shout.R;
import org.cornelldti.shout.ReportViewDialog;
import org.cornelldti.shout.ShoutRealtimeDatabase;
import org.cornelldti.shout.ShoutTabFragment;
import org.cornelldti.shout.util.LayoutUtil;

public class SpeakOutFragment extends ShoutTabFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public SpeakOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Setup reports recycler view... */


        final View speakoutFragment = inflater.inflate(R.layout.speakout_fragment, container, false);
        final RecyclerView recyclerView = speakoutFragment.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        final SpeakOutAdapter adapter = SpeakOutAdapter.construct(this, (eventAdapter, reportHolder) -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ShoutRealtimeDatabase.REPORT_LOCATIONS_KEY);
            GeoFire geoFire = new GeoFire(ref);

            if (eventAdapter == null) return;

            String id = eventAdapter.getId(reportHolder.getAdapterPosition());

            if (id != null) {
                geoFire.getLocation(id, new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (reportHolder.report == null) return;

                        ReportViewDialog dialog = ReportViewDialog.newInstance(
                                reportHolder.report,
                                new LatLng(location.latitude, location.longitude),
                                Page.SPEAK_OUT
                        );
                        showDialog(dialog);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (reportHolder.report == null) return;

                        ReportViewDialog dialog = ReportViewDialog.newInstance(reportHolder.report, Page.SPEAK_OUT);
                        showDialog(dialog);
                    }

                    private void showDialog(ReportViewDialog dialog) {
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
                    }
                });
            } else {
                if (reportHolder.report == null) return;

                ReportViewDialog dialog = ReportViewDialog.newInstance(reportHolder.report, Page.SPEAK_OUT);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
            }

        }, speakoutFragment.getContext());

        recyclerView.setAdapter(adapter);

        ViewCompat.setNestedScrollingEnabled(recyclerView, false); // enables "fast" scrolling

        // TODO decorate

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        /* Fix padding issues w/ the status bar positioning */

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0) {
            AppBarLayout toolbar = speakoutFragment.findViewById(R.id.appbar);

            toolbar.setPadding(0, statusbarSize, 0, 0);
        }

        // NOTES ON REFRESH
        // Essentially the way I've put refresh together is this:
        // 1) Have the firebase data (currently top 100 reports) be constantly synced
        // 2) However, the data doesn't automatically populate the recyclerview (that could get chaotic)
        // 3) Refreshing simply brings the latest local data into the view
        // 4) The initial data is handled by overrided the data loaded method of the Firestore adapter

        mSwipeRefreshLayout = speakoutFragment.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh items
            adapter.refreshItems();
            mSwipeRefreshLayout.setRefreshing(false);
        });

        /* Setup filtering tabs... */ // TODO Discuss UX with design

        final Button button = speakoutFragment.findViewById(R.id.all_reports_button);
        final Button stories_button = speakoutFragment.findViewById(R.id.stories_button);

        final LinearLayout buttonHighlight = speakoutFragment.findViewById(R.id.all_reports_highlight);
        final LinearLayout storiesHighlight = speakoutFragment.findViewById(R.id.stories_highlight);

        button.setOnClickListener(v -> {
            buttonHighlight.setVisibility(View.VISIBLE);
            storiesHighlight.setVisibility(View.INVISIBLE);

            adapter.filter(SpeakOutAdapter.FILTER_NONE);

        });

        stories_button.setOnClickListener(v -> {
            storiesHighlight.setVisibility(View.VISIBLE);
            buttonHighlight.setVisibility(View.INVISIBLE);

            adapter.filter(SpeakOutAdapter.FILTER_STORIES);
        });


        return speakoutFragment;
    }


    @Override
    public void onDisplayed(Bundle bundle) {

    }

    @Override
    public void onRemoved() {

    }
}
