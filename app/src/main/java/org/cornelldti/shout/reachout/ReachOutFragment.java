package org.cornelldti.shout.reachout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;

import java.util.ArrayList;
import java.util.List;

public class ReachOutFragment extends Fragment {

    public ReachOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.reachout_fragment, container, false);
        final RecyclerView recyclerView = view.findViewById(R.id.reachout_recycler_view);

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0) {
            AppBarLayout toolbar = view.findViewById(R.id.reachout_appbar);

            toolbar.setPadding(0, statusbarSize, 0, 0);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query firebase = database.getReference("resources").orderByChild("ordering");

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> titleList = new ArrayList<>(),
                        descriptionList = new ArrayList<>(),
                        websiteList = new ArrayList<>();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Resource resource = d.getValue(Resource.class);

                    if (resource != null) {
                        descriptionList.add(resource.getDescription());
                        titleList.add(resource.getTitle());
                        websiteList.add(resource.getWebsite());
                    }
                }

                ReachOutAdapter adapter = new ReachOutAdapter(titleList, descriptionList, websiteList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }


}
