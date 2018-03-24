package org.cornelldti.shout;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.cornelldti.shout.goout.GoOutFragment;
import org.cornelldti.shout.reachout.ReachOutFragment;
import org.cornelldti.shout.speakout.SpeakOutFragment;
import org.cornelldti.shout.util.function.Producer;

import java.lang.ref.WeakReference;

/**
 * Created by melody on 1/31/2017.
 * Updated by Evan Welsh on 3/1/18
 */

public class PagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "PagerAdapter";

    private final int mNumOfTabs;


    PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    private WeakReference<ShoutTabFragment> goOut, speakOut, reachOut;

    private ShoutTabFragment goOut() {
        ShoutTabFragment fragment = null;

        if (goOut != null) {
            fragment = goOut.get();
        }

        if (fragment == null) {
            fragment = new GoOutFragment();
            goOut = new WeakReference<>(fragment);
        }

        return fragment;
    }

    private ShoutTabFragment speakOut() {
        ShoutTabFragment fragment = null;

        if (speakOut != null) {
            fragment = speakOut.get();
        }

        if (fragment == null) {
            fragment = new SpeakOutFragment();
            speakOut = new WeakReference<>(fragment);
        }

        return fragment;
    }

    private ShoutTabFragment reachOut() {
        ShoutTabFragment fragment = null;

        if (reachOut != null) {
            fragment = reachOut.get();
        }

        if (fragment == null) {
            fragment = new ReachOutFragment();
            reachOut = new WeakReference<>(fragment);
        }

        return fragment;
    }

    @Override
    public ShoutTabFragment getItem(int position) {
        switch (position) {
            case Page.GO_OUT:
                return goOut();
            case Page.SPEAK_OUT:
                return speakOut();
            case Page.REACH_OUT:
                return reachOut();
            default:

                Log.d(TAG, "Attempted to retrieve unknown fragment from PagerAdapter");
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public void destroyItem(ViewGroup group, int position, Object obj) {
        switch (position) {
            case Page.GO_OUT:
                goOut = null;
                break;
            case Page.SPEAK_OUT:
                speakOut = null;
                break;
            case Page.REACH_OUT:
                reachOut = null;
                break;
            default:
        }

        super.destroyItem(group, position, obj);
    }

    @Override
    public void destroyItem(View container, int position, Object obj) {
        switch (position) {
            case Page.GO_OUT:
                goOut = null;
                break;
            case Page.SPEAK_OUT:
                speakOut = null;
                break;
            case Page.REACH_OUT:
                reachOut = null;
                break;
            default:
        }

        super.destroyItem(container, position, obj);
    }
}