package org.cornelldti.shout.speakout;

import org.cornelldti.shout.R;

/**
 * Created by Evan Welsh on 4/9/18.
 */
enum FilterOption {

    ALL_REPORTS(R.string.all_reports),
    STORIES_ONLY(R.string.stories_only);

    final int mResourceId;

    FilterOption(int resourceId) {
        mResourceId = resourceId;
    }
}
