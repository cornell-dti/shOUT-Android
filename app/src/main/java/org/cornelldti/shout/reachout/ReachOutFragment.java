package org.cornelldti.shout.reachout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.firebase.ui.firestore.ClassSnapshotParser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;
import org.cornelldti.shout.ShoutFirestore;
import org.cornelldti.shout.ShoutTabFragment;
import org.cornelldti.shout.util.AndroidUtil;
import org.cornelldti.shout.util.LayoutUtil;
import org.cornelldti.shout.util.Util;

import java.util.HashMap;
import java.util.Map;

public class ReachOutFragment extends ShoutTabFragment {

    private static final String TAG = "ReachOutFragment";

    private static final String DEFAULT_SECTION = "Resources";

    private Map<String, ResourceSection> resourceSections = new HashMap<>();
    private FirebaseFirestore mFirestore;
    private ResourceAdapter mResourceAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reachout_fragment, container, false);
        RecyclerView mRecyclerView = view.findViewById(R.id.reachout_recycler_view);

        mResourceAdapter = new ResourceAdapter();
        mRecyclerView.setAdapter(mResourceAdapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFirestore = FirebaseFirestore.getInstance();

        Context context = AndroidUtil.getContext(container, this);

<<<<<<< Updated upstream
        if (context != null) {
=======
<<<<<<< HEAD
    private void queryResources() {
        CollectionReference ref = db.collection(ShoutFirestore.RESOURCES_COLLECTION); // TODO order by position key
=======
        if (context != null) {
>>>>>>> origin/master
>>>>>>> Stashed changes

            final int statusBarSize = LayoutUtil.getStatusBarHeight(context);

            if (statusBarSize > 0) {
                LinearLayout toolbar = view.findViewById(R.id.reachout_appbar);
                toolbar.setPadding(0, statusBarSize, 0, 0);
            }
<<<<<<< Updated upstream

            loadResources();
        }

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false); // enables "fast" scrolling

        return view;
    }
=======

            loadResources();
        }

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false); // enables "fast" scrolling

<<<<<<< HEAD

        mAdapter = new FirestoreRecyclerAdapter<Resource, ResourcesHolder>(response) {
            @Override
            public void onBindViewHolder(@NonNull ResourcesHolder holder, int position, @NonNull Resource r) {
                holder.title.setText(r.getName());
=======
        return view;
    }
>>>>>>> origin/master
>>>>>>> Stashed changes

    private void loadResources() {
        CollectionReference ref = mFirestore.collection(ShoutFirestore.RESOURCES_COLLECTION);

        ref.get().addOnSuccessListener((documentSnapshots -> {
            for (DocumentSnapshot snapshot : documentSnapshots.getDocuments()) {
                Query query = ref.document(snapshot.getId()).collection(Resource.PHONES);

                Resource resource = new ClassSnapshotParser<>(Resource.class).parseSnapshot(snapshot);

                query.get().addOnSuccessListener(phones -> {
                    for (DocumentSnapshot phoneDocument : phones.getDocuments()) {
                        resource.addPhoneNumber(phoneDocument.toObject(Phone.class));
                    }
                }).addOnFailureListener(error -> Log.d(TAG, error.getMessage()));

                String sectionHeader = Util.getValueOrDefault(resource.getSection(), DEFAULT_SECTION);

                ResourceSection section = Util.getValueOrDefault(
                        resourceSections.get(sectionHeader),
                        () -> new ResourceSection(this, sectionHeader)
                );

                if (resourceSections.put(sectionHeader, section) == null) {
                    mResourceAdapter.addSection(sectionHeader, section);
                    mResourceAdapter.notifySectionChangedToVisible(section);
                }

                section.addResource(resource);
                mResourceAdapter.notifyItemChangedInSection(section, section.getResources().size() - 1);
            }
        })).addOnFailureListener((error) -> Log.e(TAG, "Could not add resource..." + error.getMessage()));
    }

    void showDialog(Resource resource) {
        FragmentManager manager = getFragmentManager();

        if (manager != null) {
            ResourceMoreInfoDialogFragment dialog = ResourceMoreInfoDialogFragment.newInstance(resource);
            dialog.show(manager, ResourceMoreInfoDialogFragment.TAG);
        }
    }

    @Override
    public void onDisplayed(Bundle bundle) {

    }

    @Override
    public void onRemoved() {

    }


}
