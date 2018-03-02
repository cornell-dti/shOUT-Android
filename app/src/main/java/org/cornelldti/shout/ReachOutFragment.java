package org.cornelldti.shout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReachOutFragment extends Fragment {

    public ReachOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference firebase = database.getReference("resources");

        final View view = inflater.inflate(R.layout.reachout_fragment, container, false);
        final ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.expand);

        // TODO ADD CODE HERE THAT PROGRAMATICALLY ADDS RESOURCES TO DB!

        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //populate expandable list from the database
                List<String> listDataHeader = new ArrayList<String>();
                HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    listDataHeader.add(d.getKey());
                    ArrayList<String> childList = new ArrayList<String>();
                    childList.add(d.child("number").getValue().toString());
                    childList.add(d.child("website").getValue().toString());
                    childList.add(d.child("description").getValue().toString());
                    listDataChild.put(d.getKey(), childList);
                }

                ExpandableListAdapter listAdapter = new org.cornelldti.shout.ExpandableListAdapter(view.getContext(), listDataHeader, listDataChild);
                listView.setAdapter(listAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return view;
    }

}
