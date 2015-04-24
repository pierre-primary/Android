package com.android.entity;

import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by xudong on 2015/4/16.
 */
public interface BaseActivityInterface {
    void finish();
    void onNetWorkChange(int NetWorkType);
    void runOnUiThread(Runnable action);
    void freash(Object obj);
    void startActivity(Intent intent);
    boolean onKeyDown(int keyCode, KeyEvent event);
    void setTopTitle(String str);
    void setTopTitle(Integer id);
}
