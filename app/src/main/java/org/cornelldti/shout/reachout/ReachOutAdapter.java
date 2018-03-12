package org.cornelldti.shout.reachout;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cornelldti.shout.R;

import java.util.List;


/**
 * Created by Evan Welsh on 3/1/18.
 */

public class ReachOutAdapter extends RecyclerView.Adapter<ReachOutAdapter.ViewHolder> {

    private List<String> titleList, descriptionList, websiteList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, website;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.resource_item_title);
            description = v.findViewById(R.id.resource_item_description);
            website = v.findViewById(R.id.resource_item_website);
        }
    }

    ReachOutAdapter(List<String> titleList, List<String> descriptionList, List<String> websiteList) {
        this.titleList = titleList;
        this.descriptionList = descriptionList;
        this.websiteList = websiteList;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // todo switch away from ArrayList(s)
        holder.title.setText(titleList.get(position));
        holder.description.setText(descriptionList.get(position));
        holder.website.setText(websiteList.get(position));
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

}
