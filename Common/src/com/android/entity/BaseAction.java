package com.android.entity;

import android.app.Activity;
import android.view.View;

/**
 * Created by xudong on 2015/4/17.
 */
public abstract class BaseAction implements BaseActionInterface,View.OnClickListener {
    protected View layout;
    protected Activity activity;

    protected BaseAction(Activity activity, View layout) {
        this.activity = activity;
        this.layout = layout;
        init(null);
    }

    public View getLayout() {
        return layout;
    }

    public void startAnn() {
    }

    public abstract void init(Object obj);

    public <T extends View> T getViewById(View v, int viewId) {
        return (T) v.findViewById(viewId);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag()!= null && "back".equals(v.getTag())){
            activity.finish();
            activity.overridePendingTransition(com.android.common.R.anim.next_zoomin, com.android.common.R.anim.back_zoomout);
        }
    }
}
