package com.android.entity;

import android.view.View;

/**
 * Created by xudong on 2015/4/17.
 */
public interface BaseActionInterface {

    void init(Object obj);

    View getLayout();

    public <T extends View> T getViewById(View v, int viewId);

    public void startAnn();

    void onClick(View v);
}
