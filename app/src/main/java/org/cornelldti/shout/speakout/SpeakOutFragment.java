package org.cornelldti.shout.speakout;

import android.content.Context;
import android.os.Bundle;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.MainActivity;
import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;

public class SpeakOutFragment extends Fragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public SpeakOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Setup reports recycler view... */

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference ref = firestore.collection("reports");

        final View speakoutFragment = inflater.inflate(R.layout.speakout_fragment, container, false);
        final RecyclerView recyclerView = speakoutFragment.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        Query stories = ref.whereEqualTo("hasbody", true).orderBy("timestamp").limit(100);
        Query all = ref.orderBy("timestamp").limit(100);

        final SpeakOutAdapterV2 adapter = SpeakOutAdapterV2.construct(this, stories, all, speakoutFragment.getContext());
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

        /* Fix padding issues w/ the status bar positioning */

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0)

        {
            AppBarLayout toolbar = speakoutFragment.findViewById(R.id.appbar);

            toolbar.setPadding(0, statusbarSize, 0, 0);
        }


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

            adapter.filter(SpeakOutAdapterV2.FILTER_NONE);

        });

        stories_button.setOnClickListener(v -> {
            storiesHighlight.setVisibility(View.VISIBLE);
            buttonHighlight.setVisibility(View.INVISIBLE);

            adapter.filter(SpeakOutAdapterV2.FILTER_STORIES);
        });


        View makeBlogPost = speakoutFragment.findViewById(R.id.startReportButton);
        makeBlogPost.setOnClickListener(view -> {
            Context context = getContext();

            if (context instanceof MainActivity) {
                ((MainActivity) context).setStatusBarColor(-1); // TODO add constant for "unknown" page
            }

            ReportIncidentDialog dialog = ReportIncidentDialog.newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, dialog).addToBackStack(null).commit();
        });

        return speakoutFragment;
    }

}
