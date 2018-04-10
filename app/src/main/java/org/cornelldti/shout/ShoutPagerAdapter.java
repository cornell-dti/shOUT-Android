package org.cornelldti.shout;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.cornelldti.shout.goout.GoOutFragment;
import org.cornelldti.shout.reachout.ReachOutFragment;
import org.cornelldti.shout.speakout.SpeakOutFragment;

import java.lang.ref.WeakReference;

/**
 * Created by melody on 1/31/2017.
 * Updated by Evan Welsh on 3/1/18
 */

public class ShoutPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "ShoutPagerAdapter";

    private WeakReference<ShoutTabFragment> goOut, speakOut, reachOut;
    private final static int NUMBER_OF_TABS = 3;

    ShoutPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public ShoutTabFragment getItem(int position) {
        switch (position) {
            case Page.GO_OUT:
            case Page.SPEAK_OUT:
            case Page.REACH_OUT:
                return getTabFragmentInstance(position);
            default:
                Log.d(TAG, "Attempted to retrieve unknown fragment from ShoutPagerAdapter");
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUMBER_OF_TABS;
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
    public void destroyItem(@NonNull View container, int position, @NonNull Object obj) {
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

    /* Fragment Reference Util Getters */

    private ShoutTabFragment getTabFragmentInstance(int page) {
        ShoutTabFragment fragment = null;

        WeakReference<ShoutTabFragment> ref = null;

        switch (page) {
            case Page.GO_OUT:
                ref = goOut;
                break;
            case Page.REACH_OUT:
                ref = reachOut;
                break;
            case Page.SPEAK_OUT:
                ref = speakOut;
                break;
            default:
        }

        if (ref != null) {
            fragment = ref.get();
        }

        if (fragment == null) {
            switch (page) {
                case Page.GO_OUT:
                    fragment = new GoOutFragment();
                    goOut = new WeakReference<>(fragment);
                    break;
                case Page.REACH_OUT:
                    fragment = new ReachOutFragment();
                    reachOut = new WeakReference<>(fragment);
                    break;
                case Page.SPEAK_OUT:
                    fragment = new SpeakOutFragment();
                    speakOut = new WeakReference<>(fragment);
                    break;
                default:
            }
        }

        return fragment;
    }
}