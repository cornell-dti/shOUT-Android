package org.cornelldti.shout.reachout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.cornelldti.shout.R;

import java.util.List;

/**
 * Created by kaushikr on 3/21/18.
 */

public class PhoneAdapter extends ArrayAdapter<Phone> {
    private Context mContext;
    private List<Phone> mPhones;

    public PhoneAdapter(Context context, List<Phone> phones) {
        super(context, 0, phones);
        mContext = context;
        mPhones = phones;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.phone_item,parent,false);

        Phone currentPhone = mPhones.get(position);

        TextView labelAndNumber = (TextView) listItem.findViewById(R.id.labelAndNum);
        labelAndNumber.setText(currentPhone.getLabel() + ": " + currentPhone.getNumber());

        TextView description = (TextView) listItem.findViewById(R.id.description);
        description.setText(currentPhone.getDescription());

        return listItem;
    }


}
