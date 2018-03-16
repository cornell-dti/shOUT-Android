package org.cornelldti.shout.reachout;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.cornelldti.shout.R;

import java.util.List;


/**
 * Created by Evan Welsh on 3/1/18.
 */

public class ReachOutAdapter extends FirestoreRecyclerAdapter<Resource, ReachOutAdapter.ViewHolder> {

    private final OpenResourceCallback callback;
    private List<String> titleList, descriptionList, websiteList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, description;
        private Button website, directions;

        ViewHolder(View v) {
            super(v);

            /* Cache views */
            title = v.findViewById(R.id.resource_item_title);
            description = v.findViewById(R.id.resource_item_description);
            website = v.findViewById(R.id.websiteButton);
            directions = v.findViewById(R.id.directionsButton);
        }
    }

    private ReachOutAdapter(FirestoreRecyclerOptions<Resource> options, OpenResourceCallback callback) {
        super(options);
        this.callback = callback;
    }

    static ReachOutAdapter construct(Fragment fragment, OpenResourceCallback callback) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        Query query = database.collection("resources").orderBy("ordering");

        FirestoreRecyclerOptions<Resource> options = new FirestoreRecyclerOptions.Builder<Resource>()
                .setQuery(query, Resource.class)
                .setLifecycleOwner(fragment)
                .build();

        return new ReachOutAdapter(options, callback);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.resource_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Resource model) {
        holder.title.setText(model.getTitle());
        holder.description.setText(model.getDescription());

        final String website = model.getWebsite();

        holder.website.setOnClickListener(listener -> {
            Uri uri = Uri.parse(website);
            this.callback.openResourceUri(uri);
        });

    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

}
