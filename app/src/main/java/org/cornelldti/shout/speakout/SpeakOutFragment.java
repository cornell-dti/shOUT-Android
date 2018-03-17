package org.cornelldti.shout.speakout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;

public class SpeakOutFragment extends Fragment {

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

        final SpeakOutAdapter adapter = SpeakOutAdapter.construct(this, speakoutFragment.getContext());
        recyclerView.setAdapter(adapter);

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


}
