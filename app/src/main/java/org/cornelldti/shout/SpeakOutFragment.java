package org.cornelldti.shout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebase = database.getReference("approved_reports");
        final View view = inflater.inflate(R.layout.speakout_fragment, container, false);
        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> titleList = new ArrayList<>(), bodyList = new ArrayList<>(), locationList = new ArrayList<>();
                List<Date> timeList = new ArrayList<>();


                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    ApprovedMessage m = d.getValue(ApprovedMessage.class);

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

}
