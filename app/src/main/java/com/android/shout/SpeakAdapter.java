package com.android.shout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by melody on 1/31/2017.
 */

public class SpeakAdapter extends RecyclerView.Adapter<SpeakAdapter.ViewHolder> {

    private ArrayList<String> titleList, bodyList, dateList, timeList;

    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, date, time;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            body = v.findViewById(R.id.body);
            date = v.findViewById(R.id.date);
            time = v.findViewById(R.id.time);
        }
    }

    SpeakAdapter(ArrayList<String> titleList, ArrayList<String> bodyList, ArrayList<String> dateList, ArrayList<String> timeList, Context context) {
        this.titleList = titleList;
        this.bodyList = bodyList;
        this.dateList = dateList;
        this.timeList = timeList;
        this.mContext = context;
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
        holder.date.setText(dateList.get(position));
        holder.time.setText(timeList.get(position));
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

}
