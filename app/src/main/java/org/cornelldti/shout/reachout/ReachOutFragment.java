package org.cornelldti.shout.reachout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;

public class ReachOutFragment extends Fragment {

    private FirestoreRecyclerAdapter adapter;

    private RecyclerView recyclerView;

    private FirebaseFirestore db;

    public ReachOutFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reachout_fragment, container, false);
        recyclerView = view.findViewById(R.id.reachout_recycler_view);
        db = FirebaseFirestore.getInstance();

        final int statusbarSize = LayoutUtil.getStatusBarHeight(getActivity());

        if (statusbarSize > 0) {
            AppBarLayout toolbar = view.findViewById(R.id.reachout_appbar);

            toolbar.setPadding(0, statusbarSize, 0, 0);
        }

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        queryResources();

        return view;
    }

    // REWRITE THIS METHOD SO IT ALSO QUERIES PHONE COLLECTION OF EACH RESOURCE TODO
    private void queryResources() {
        Query query = db.collection("resources");

        FirestoreRecyclerOptions<Resource> response = new FirestoreRecyclerOptions.Builder<Resource>()
                .setQuery(query, Resource.class)
                .setLifecycleOwner(this)
                .build();
        adapter = new FirestoreRecyclerAdapter<Resource, ResourcesHolder>(response) {
            @Override
            public void onBindViewHolder(@NonNull ResourcesHolder holder, int position, @NonNull Resource r) {
                holder.title.setText(r.getName());
                holder.description.setText(r.getDescription());
                holder.itemView.setOnClickListener(v -> {
                    DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                    String resId = snapshot.getId();
                    showDialog(r, resId);
                });
            }

            @NonNull
            @Override
            public ResourcesHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.resource_item, group, false);

                return new ResourcesHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        ViewCompat.setNestedScrollingEnabled(recyclerView, false); // enables "fast" scrolling

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void showDialog(Resource resource, String resId) {
//        Query query = db.collection("resources").document(resId).collection("phones");
        // PHONE COLLECTION NULL TODO FIX
//        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//            }
//        });
//        Collection<Phone> pho = resource.getPhones();
//        Toast.makeText(getActivity(), String.valueOf(pho.size()), Toast.LENGTH_SHORT).show();

        MoreInfoResourceDialog dialog = MoreInfoResourceDialog.newInstance(resource, resId);
        dialog.show(getFragmentManager(), "");
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
