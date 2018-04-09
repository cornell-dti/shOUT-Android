package org.cornelldti.shout.reachout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.cornelldti.shout.R;
import org.cornelldti.shout.util.LayoutUtil;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by Evan Welsh on 4/2/18.
 */
class ResourceSection extends StatelessSection {
    private List<Resource> mResourceList = new ArrayList<>();
    private final Context mContext;
    private final ReachOutFragment mFragment;

    private final String mSectionHeader;

    ResourceSection(ReachOutFragment fragment, Context context, String sectionHeader) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.resource_recycler_item)
                .headerResourceId(R.layout.resource_header)
                .build());
        this.mContext = context;
        this.mFragment = fragment;
        this.mSectionHeader = sectionHeader;
    }

    @Override
    public int getContentItemsTotal() {
        return mResourceList.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ResourcesHolder(view);
    }

    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTxtView;

        HeaderViewHolder(View view) {
            super(view);

            titleTxtView = view.findViewById(R.id.resource_header_text_view);
        }
    }

    public static class ResourcesHolder extends RecyclerView.ViewHolder {
        TextView title, description;

        ResourcesHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.resource_item_title);
            description = itemView.findViewById(R.id.resource_item_description);
        }
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ResourcesHolder itemHolder = (ResourcesHolder) holder;
        final Resource r = mResourceList.get(position);


        itemHolder.title.setText(r.getName());

        String rDescription = r.getDescription();

        if (!TextUtils.isEmpty(rDescription)) {
            itemHolder.description.setText(rDescription);
        } else {
            itemHolder.description.setVisibility(View.GONE);

            // TODO fix this in the xml (probably move to a linearlayout parent with margin/padding

            itemHolder.title.setPadding(0, 0, 0, LayoutUtil.getPixelsFromDp(mContext.getResources(), 16));
        }

        holder.itemView.setOnClickListener(v -> this.mFragment.showDialog(r));
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        if (holder instanceof HeaderViewHolder && this.hasHeader()) {
            ((HeaderViewHolder) holder).titleTxtView.setText(this.mSectionHeader);
        }
    }

    public void addResource(Resource resource) {
        mResourceList.add(resource);
    }

    public List<Resource> getResources() {
        return mResourceList;
    }
}
