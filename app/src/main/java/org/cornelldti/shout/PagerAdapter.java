package org.cornelldti.shout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import org.cornelldti.shout.goout.GoOutFragment;
import org.cornelldti.shout.reachout.ReachOutFragment;
import org.cornelldti.shout.speakout.SpeakOutFragment;

/**
 * Created by melody on 1/31/2017.
 * Updated by Evan Welsh on 3/1/18
 */

public class PagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PagerAdapter";

    private final int mNumOfTabs;


    PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case Pages.GO_OUT:
                return new GoOutFragment();
            case Pages.SPEAK_OUT:
                return new SpeakOutFragment();
            case Pages.REACH_OUT:
                return new ReachOutFragment();
            default:
                Log.d(TAG, "Attempted to retrieve unknown fragment from PagerAdapter");
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public interface Pages {
        int UNKNOWN = -1;
        int SPEAK_OUT = 0;
        int GO_OUT = 1;
        int REACH_OUT = 2;
    }
}