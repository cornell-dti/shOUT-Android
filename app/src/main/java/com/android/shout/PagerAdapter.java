package com.android.shout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by melody on 1/31/2017.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                GoOutFragment goout = new GoOutFragment();
                return goout;
            case 1:
                SpeakOutFragment speakout = new SpeakOutFragment();
                return speakout;
            case 2:
                ReachOutFragment reachout = new ReachOutFragment();
                return reachout;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}