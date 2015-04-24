package com.android.animation;

import android.app.Activity;

/**
 * Created by xudong on 2015/4/20.
 */
public class BaseAimation {

    public static void backAnn(Activity activity) {
        activity.overridePendingTransition(com.android.common.R.anim.back_zoomin, com.android.common.R.anim.back_zoomout);
    }

    public static void nextAnn(Activity activity){
        activity.overridePendingTransition(com.android.common.R.anim.next_zoomin, com.android.common.R.anim.next_zoomout);
    }
}
