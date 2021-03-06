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
import android.widget.ViewFlipper;

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

    private static final int PROGRESS_SPINNER_CHILD = 0;
    private static final int RECYCLER_VIEW_CHILD = 1;

    private Map<String, ResourceSection> resourceSections = new HashMap<>();
    private FirebaseFirestore mFirestore;
    private ResourceAdapter mResourceAdapter;
    private ViewFlipper mViewFlipper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reach_out_fragment, container, false);
        RecyclerView mRecyclerView = view.findViewById(R.id.reachout_recycler_view);
        mViewFlipper = view.findViewById(R.id.reach_out_view_flipper);
        mViewFlipper.setDisplayedChild(PROGRESS_SPINNER_CHILD);

        mResourceAdapter = new ResourceAdapter();
        mRecyclerView.setAdapter(mResourceAdapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFirestore = FirebaseFirestore.getInstance();

        Context context = AndroidUtil.getContext(container, this);

        if (context != null) {

            final int statusBarSize = LayoutUtil.getStatusBarHeight(context);

            if (statusBarSize > 0) {
                LinearLayout toolbar = view.findViewById(R.id.reachout_appbar);
                toolbar.setPadding(0, statusBarSize, 0, 0);
            }

            loadResources();
        }

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false); // enables "fast" scrolling

        return view;
    }

    // TODO cleanup this mess

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

            mViewFlipper.setDisplayedChild(RECYCLER_VIEW_CHILD);
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