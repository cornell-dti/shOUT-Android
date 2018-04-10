package org.cornelldti.shout.reachout;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.cornelldti.shout.R;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by Evan Welsh on 4/2/18.
 */
class ResourceSection extends StatelessSection {

    private final List<Resource> mResourceList = new ArrayList<>();
    private final ReachOutFragment mFragment;
    private final String mSectionHeader;

    ResourceSection(ReachOutFragment fragment, String sectionHeader) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.resource_list_item)
                .headerResourceId(R.layout.resource_header)
                .build());

        mFragment = fragment;
        mSectionHeader = sectionHeader;
    }

    public void addResource(Resource resource) {
        mResourceList.add(resource);
    }

    public List<Resource> getResources() {
        return mResourceList;
    }

    @Override
    public int getContentItemsTotal() {
        return mResourceList.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ResourceItemHolder(view);
    }

    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ResourceItemHolder) {
            ResourceItemHolder itemHolder = (ResourceItemHolder) holder;

            final Resource resource = mResourceList.get(position);

            itemHolder.mTitle.setText(resource.getName());

            String description = resource.getDescription();

            if (!TextUtils.isEmpty(description)) {
                itemHolder.mDescription.setText(description);
            } else {
                itemHolder.mDescription.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> this.mFragment.showDialog(resource));
        }
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        if (holder instanceof HeaderViewHolder && this.hasHeader()) {
            ((HeaderViewHolder) holder).mTitle.setText(this.mSectionHeader);
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;

        HeaderViewHolder(View view) {
            super(view);

            mTitle = view.findViewById(R.id.resource_header_text_view);
        }
    }

    public static class ResourceItemHolder extends RecyclerView.ViewHolder {
        TextView mTitle, mDescription;

        ResourceItemHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.resource_item_title);
            mDescription = itemView.findViewById(R.id.resource_item_description);
        }
    }
}
