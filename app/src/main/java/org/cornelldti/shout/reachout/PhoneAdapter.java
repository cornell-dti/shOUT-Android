package org.cornelldti.shout.reachout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.cornelldti.shout.R;

/**
 * Created by kaushikr on 3/21/18.
 */

public class PhoneAdapter extends BaseAdapter {
    private Context mContext;
    private Resource mResource;

    PhoneAdapter(Context context, Resource resource) {
        mContext = context;
        mResource = resource;
    }

    @Override
    public int getCount() {
        return mResource.getPhoneNumbers().size();
    }

    @Override
    public Object getItem(int position) {
        return mResource.getPhoneNumbers().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.phone_item, parent, false);
        }

        Phone currentPhone = mResource.getPhoneNumbers().get(position);

        if (currentPhone != null) {
            TextView labelTextView = listItem.findViewById(R.id.phone_item_label_text_view);
            labelTextView.setText(currentPhone.getLabel());

            TextView numberTextView = listItem.findViewById(R.id.phone_item_number_text_view);
            numberTextView.setText(currentPhone.getNumber());
        }

        return listItem;
    }


}
