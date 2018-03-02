package org.cornelldti.shout.speakout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.cornelldti.shout.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpeakOutFragment extends Fragment {

    /* FAB */
    View makeBlogPost; // Find an alt. to a card view.

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseDatabase database;

    DatabaseReference firebase;

    RecyclerView recyclerView;

    View view;

    public SpeakOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        firebase = database.getReference("approved_reports");
        view = inflater.inflate(R.layout.speakout_fragment, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        refreshItems();
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        final Toolbar toolbar = view.findViewById(R.id.toolbar);


        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        //   collapsingToolbar.setTitle("shOUT");

        makeBlogPost = view.findViewById(R.id.startReportButton);
        makeBlogPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  startActivity(new Intent(getActivity(), ReportIncident.class));

                ReportIncidentDialog dialog = ReportIncidentDialog.newInstance();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, dialog)
                        .addToBackStack(null).commit();
            }
        });

        return view;
    }

    void refreshItems() {
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> titleList = new ArrayList<>(), bodyList = new ArrayList<>(), locationList = new ArrayList<>();
                List<Date> timeList = new ArrayList<>();


                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    ApprovedReport m = d.getValue(ApprovedReport.class);

                    if (m != null) {
                        bodyList.add(m.getBody());
                        titleList.add(m.getTitle());
                        timeList.add(new Date(m.getTime()));
                        locationList.add(m.getLocation());
                    }
                }

                SpeakAdapter adapter = new SpeakAdapter(titleList, bodyList, timeList, locationList, view.getContext());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
