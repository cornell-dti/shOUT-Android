package org.cornelldti.shout.reachout;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.ClassSnapshotParser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;
import org.cornelldti.shout.ShoutFirestore;
import org.cornelldti.shout.ShoutTabFragment;
import org.cornelldti.shout.util.LayoutUtil;
import org.cornelldti.shout.util.Util;

import java.util.HashMap;
import java.util.Map;

public class ReachOutFragment extends ShoutTabFragment {

    private static final String TAG = "ReachOutFragment";

    private static final String DEFAULT_SECTION = "Resources";

    private Map<String, ResourceSection> resourceSections = new HashMap<>();

    private RecyclerView mRecyclerView;

    private FirebaseFirestore db;

    public ReachOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reachout_fragment, container, false);
        mRecyclerView = view.findViewById(R.id.reachout_recycler_view);
        db = FirebaseFirestore.getInstance();

        Activity activity = getActivity();

        if (activity != null) {

            final int statusBarSize = LayoutUtil.getStatusBarHeight(activity);

            if (statusBarSize > 0) {
                LinearLayout toolbar = view.findViewById(R.id.reachout_appbar);
                toolbar.setPadding(0, statusBarSize, 0, 0);
            }


            queryResources();

            // DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
            // mRecyclerView.addItemDecoration(dividerItemDecoration);
        }

        return view;
    }

    private void queryResources() {
        //if (db == null) return;

        CollectionReference ref = db.collection(ShoutFirestore.RESOURCES_COLLECTION); // TODO order by position key
        final ResourcesAdapter sectionAdapter = new ResourcesAdapter();

        mRecyclerView.setAdapter(sectionAdapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        ref.get().addOnSuccessListener((documentSnapshots -> {
            for (DocumentSnapshot snapshot : documentSnapshots.getDocuments()) {
                Query query = ref.document(snapshot.getId()).collection(Resource.PHONES);

                ClassSnapshotParser<Resource> parser = new ClassSnapshotParser<>(Resource.class);
                Resource resource = parser.parseSnapshot(snapshot);

                query.get().addOnSuccessListener(phones -> {
                    for (DocumentSnapshot phoneDocument : phones.getDocuments()) {
                        resource.addPhoneNumber(phoneDocument.toObject(Phone.class));
                    }

                    // TODO technically there is an extremely small time when if the user clicks phone numbers may not be loaded...
                    // TODO figure out how to "update" the dialog.
                }).addOnFailureListener(error -> {
                    Log.d(TAG, error.getMessage());
                });

                String sectionHeader = Util.getValueOrDefault(resource.getSection(), DEFAULT_SECTION);

                ResourceSection section1 = Util.getValueOrDefault(resourceSections.get(sectionHeader), () -> new ResourceSection(this, getContext(), sectionHeader));

                section1.addResource(resource);

                if (resourceSections.put(sectionHeader, section1) == null) {
                    sectionAdapter.addSection(sectionHeader, section1);
                }

                sectionAdapter.notifyDataSetChanged();
            }
        })).addOnFailureListener((error) -> Log.e(TAG, "Could not add resource..." + error.getMessage()));

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false); // enables "fast" scrolling
    }

    void showDialog(Resource resource) {
        FragmentManager manager = getFragmentManager();

        if (manager != null) {
            ResourceInfoDialogFragment dialog = ResourceInfoDialogFragment.newInstance(resource);
            dialog.show(manager, "ResourceInfoDialogFragment");
        }
    }

    @Override
    public void onDisplayed(Bundle bundle) {

    }

    @Override
    public void onRemoved() {

    }


}
