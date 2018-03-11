package org.cornelldti.shout.speakout;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.cornelldti.shout.MainActivity;
import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;

public class SpeakOutFragment extends Fragment {

    /* FAB */
    View makeBlogPost; // Find an alt. to a card view.
    SwipeRefreshLayout mSwipeRefreshLayout;

    public SpeakOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Setup reports recycler view... */

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebase = database.getReference("approved_reports");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firestore.collection("reports");

        final View view = inflater.inflate(R.layout.speakout_fragment, container, false);
        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        // Query allReports = firebase.orderByChild("timestamp").limitToFirst(100); // TODO

        com.google.firebase.firestore.Query stories = ref.whereEqualTo("hasbody", true).orderBy("timestamp").limit(100);
        com.google.firebase.firestore.Query all = ref.orderBy("timestamp").limit(100);

        // final SpeakOutAdapter adapter = SpeakOutAdapter.construct(view.getContext(), this, allReports);

        final SpeakOutAdapterV2 adapter = SpeakOutAdapterV2.construct(this, stories, all, view.getContext());
        recyclerView.setAdapter(adapter);


        // NOTES ON REFRESH
        // Essentially the way I've put refresh together is this:
        // 1) Have the firebase data (currently top 100 reports) be constantly synced
        // 2) However, the data doesn't automatically populate the recyclerview (that could get chaotic)
        // 3) Refreshing simply brings the latest local data into the view
        // 4) However, the implementation of this means that no data would be visible until the first refresh
        //      -- To get around this I initially have the data be automatically populated. I then have a delayed
        //         function that essentially checks if any data has been receive and once it has it stops auto
        //         population.

        // TODO Investigate better implementations.

        // final long loadedCheckDelay = 500L, maximumCheckDelay = 5000L;
        // final int[] iteration = new int[]{0};

        /* Handle loading... *
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            private Runnable anonThis = this;

            @Override
            public void run() {
                if (adapter.canStopLoading()) {
                    adapter.setIsLoading(false);
                } else {
                    runAgain();
                }
            }

            private void runAgain() {
                if (iteration[0]++ * loadedCheckDelay > maximumCheckDelay) {
                    return;
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.canStopLoading()) {
                            adapter.setIsLoading(false);
                        } else {
                            anonThis.run();
                        }
                    }
                }, loadedCheckDelay);
            }
        }, loadedCheckDelay);

        /* Fix padding issues w/ the status bar positioning */

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0)

        {
            AppBarLayout toolbar = view.findViewById(R.id.appbar);

            toolbar.setPadding(0, statusbarSize, 0, 0);
        }


        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()

        {
            @Override
            public void onRefresh() {
                // Refresh items
                adapter.refreshItems();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        /* Setup filtering tabs... */ // TODO Discuss UX with design

        final Button button = view.findViewById(R.id.all_reports_button);
        final Button stories_button = view.findViewById(R.id.stories_button);

        final LinearLayout buttonHighlight = view.findViewById(R.id.all_reports_highlight);
        final LinearLayout storiesHighlight = view.findViewById(R.id.stories_highlight);

        button.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                buttonHighlight.setVisibility(View.VISIBLE);
                storiesHighlight.setVisibility(View.INVISIBLE);

                adapter.filter(SpeakOutAdapterV2.FILTER_NONE);

            }
        });

        stories_button.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                storiesHighlight.setVisibility(View.VISIBLE);
                buttonHighlight.setVisibility(View.INVISIBLE);

                adapter.filter(SpeakOutAdapterV2.FILTER_STORIES);
            }
        });


        makeBlogPost = view.findViewById(R.id.startReportButton);
        makeBlogPost.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Context context = getContext();

                if (context instanceof MainActivity) {
                    ((MainActivity) context).setStatusBarColor(-1); // TODO add constant for "unknown" page
                }

                ReportIncidentDialog dialog = ReportIncidentDialog.newInstance();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
            }
        });

        return view;
    }

}
