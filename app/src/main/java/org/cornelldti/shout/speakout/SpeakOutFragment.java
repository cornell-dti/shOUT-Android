package org.cornelldti.shout.speakout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.cornelldti.shout.MainActivity;
import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpeakOutFragment extends Fragment {

    /* FAB */
    View makeBlogPost; // Find an alt. to a card view.


    public SpeakOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Setup reports recycler view... */

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebase = database.getReference("approved_reports");

        final View view = inflater.inflate(R.layout.speakout_fragment, container, false);
        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        Query allReports = firebase.orderByChild("timestamp").limitToFirst(100); // TODO

        final SpeakOutAdapter adapter = SpeakOutAdapter.construct(view.getContext(), this, allReports);
        recyclerView.setAdapter(adapter);

        /* Fix padding issues w/ the status bar positioning */

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0) {
            AppBarLayout toolbar = view.findViewById(R.id.appbar);

            toolbar.setPadding(0, statusbarSize, 0, 0);
        }

        /* Setup filtering tabs... */ // TODO Discuss UX with design

        final Button button = view.findViewById(R.id.all_reports_button);
        final Button stories_button = view.findViewById(R.id.stories_button);

        final LinearLayout buttonHighlight = view.findViewById(R.id.all_reports_highlight);
        final LinearLayout storiesHighlight = view.findViewById(R.id.stories_highlight);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonHighlight.setVisibility(View.VISIBLE);
                storiesHighlight.setVisibility(View.INVISIBLE);

                adapter.filter(false);

            }
        });

        stories_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesHighlight.setVisibility(View.VISIBLE);
                buttonHighlight.setVisibility(View.INVISIBLE);

                adapter.filter(true);
            }
        });


        makeBlogPost = view.findViewById(R.id.startReportButton);
        makeBlogPost.setOnClickListener(new View.OnClickListener() {
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
