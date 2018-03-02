package org.cornelldti.shout.speakout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cornelldti.shout.R;

import java.util.Date;
import java.util.List;


/**
 * Created by melody on 1/31/2017.
 */

public class SpeakAdapter extends RecyclerView.Adapter<SpeakAdapter.ViewHolder> {

    private List<String> titleList, bodyList, locationList;
    private List<Date> dateList;

    private final java.text.DateFormat dateFormatter;
    private final java.text.DateFormat timeFormatter;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, date, time, location;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.feed_item_title);
            body = v.findViewById(R.id.feed_item_body);
            date = v.findViewById(R.id.feed_item_date);
            time = v.findViewById(R.id.feed_item_time);
            location = v.findViewById(R.id.feed_item_location);
        }
    }

    SpeakAdapter(List<String> titleList, List<String> bodyList, List<Date> dateList, List<String> locationList, Context context) {
        this.dateFormatter = DateFormat.getDateFormat(context);
        this.timeFormatter = DateFormat.getTimeFormat(context);

        this.titleList = titleList;
        this.bodyList = bodyList;
        this.dateList = dateList;
        this.locationList = locationList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.title.setText(titleList.get(position));
        holder.body.setText(bodyList.get(position));
        holder.location.setText(locationList.get(position));
        holder.date.setText(dateFormatter.format(dateList.get(position)));
        holder.time.setText(timeFormatter.format(dateList.get(position)));
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

}
