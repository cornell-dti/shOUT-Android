package org.cornelldti.shout.reachout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.ClassSnapshotParser;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;
import org.cornelldti.shout.ShoutFirestore;
import org.cornelldti.shout.ShoutTabFragment;
import org.cornelldti.shout.util.LayoutUtil;

public class ReachOutFragment extends ShoutTabFragment {

    private static final String TAG = "ReachOutFragment";

    private FirestoreRecyclerAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private FirebaseFirestore db;

    public ReachOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reachout_fragment, container, false);
        mRecyclerView = view.findViewById(R.id.reachout_recycler_view);
        db = FirebaseFirestore.getInstance();

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0) {
            AppBarLayout toolbar = view.findViewById(R.id.reachout_appbar);

            toolbar.setPadding(0, statusbarSize, 0, 0);
        }

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        queryResources();

        return view;
    }

    // REWRITE THIS METHOD SO IT ALSO QUERIES PHONE COLLECTION OF EACH RESOURCE TODO
    private void queryResources() {
        CollectionReference ref = db.collection(ShoutFirestore.RESOURCES_COLLECTION); // TODO order by position key

        FirestoreRecyclerOptions<Resource> response = new FirestoreRecyclerOptions.Builder<Resource>()
                .setQuery(ref, snapshot -> {
                    Query query = ref.document(snapshot.getId()).collection(Resource.PHONES);

                    ClassSnapshotParser<Resource> parser = new ClassSnapshotParser<>(Resource.class);
                    Resource resource = parser.parseSnapshot(snapshot);

                    query.get().addOnSuccessListener(phones -> {
                        resource.setPhoneNumbers(phones.toObjects(Phone.class));

                        // TODO technically there is an extremely small time when if the user clicks phone numbers may not be loaded...
                        // TODO figure out how to "update" the dialog.
                    }).addOnFailureListener(error -> {
                        Log.d(TAG, error.getMessage());
                    });

                    return resource;
                })
                .setLifecycleOwner(this)
                .build();

        mAdapter = new FirestoreRecyclerAdapter<Resource, ResourcesHolder>(response) {
            @Override
            public void onBindViewHolder(@NonNull ResourcesHolder holder, int position, @NonNull Resource r) {
                holder.title.setText(r.getName());

                String description = r.getDescription();

                if (!TextUtils.isEmpty(description)) {
                    holder.description.setText(description);
                } else {
                    holder.description.setVisibility(View.GONE);

                    // TODO fix this in the xml (probably move to a linearlayout parent with margin/padding

                    holder.title.setPadding(0, 0, 0, LayoutUtil.getPixelsFromDp(getResources(), 16));
                }

                holder.itemView.setOnClickListener(v -> {
                    DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                    String resId = snapshot.getId();
                    showDialog(r, resId);
                });
            }

            @NonNull
            @Override
            public ResourcesHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.resource_item, group, false);

                return new ResourcesHolder(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false); // enables "fast" scrolling

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    private void showDialog(Resource resource, String resId) {
        FragmentManager manager = getFragmentManager();

        if (manager != null) {
            ResourceInfoDialogFragment dialog = ResourceInfoDialogFragment.newInstance(resource);
            dialog.show(manager, "ResourceInfoDialogFragment:" + resId);
        }
    }

    @Override
    public void onDisplayed(Bundle bundle) {

    }

    @Override
    public void onRemoved() {

    }

    public class ResourcesHolder extends RecyclerView.ViewHolder {
        TextView title, description;

        ResourcesHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.resource_item_title);
            description = itemView.findViewById(R.id.resource_item_description);
        }
    }


}
