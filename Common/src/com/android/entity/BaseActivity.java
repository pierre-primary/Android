package com.android.entity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.bitmap.BitmapInterface;
import com.android.core.controll.AppContextControll;
import com.android.http.volley.cache.CFrameVolley;
import com.android.util.AcUtil;


public abstract class BaseActivity extends Activity implements BaseActivityInterface, View.OnClickListener {
    protected final String tag = this.getClass().getSimpleName();

    @Override
    protected void onStart() {
        super.onStart();
        if (AppContextControll.app.cache.containsKey("back")) {
            Integer backid = (Integer) AppContextControll.app.cache.get("back");
            View v = findViewById(backid);
            if (v != null)
                v.setOnClickListener(this);
        }
    }


    public void init(Object object) {
    }

    public void freash(Object object) {
    }


    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AcUtil.add(this);
    }

    @Override
    protected void onDestroy() {
        AcUtil.remove(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 通过view id来获取View
     *
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getViewById(int viewId) {
        return (T) this.findViewById(viewId);
    }

    /**
     * 通过view id来获取View
     *
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getViewById(View v, int viewId) {
        return (T) v.findViewById(viewId);
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            BitmapInterface bitmapInterface = (BitmapInterface) AppContextControll.app.cache.get("bitmap");
            bitmapInterface.free();

            CFrameVolley.free();
            System.gc();
        }
        super.onTrimMemory(level);

    }

    @Override
    public void onNetWorkChange(int type) {

    }

    protected void toastShortLength(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void toastLongLength(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    protected static void toast(Context ctx, String text, int LENGTH) {
        Toast.makeText(ctx, text, LENGTH).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null && "back".equals(v.getTag())) {
            finish();
            overridePendingTransition(com.android.common.R.anim.back_zoomin, com.android.common.R.anim.back_zoomout);
        }
    }

    long current = 0l;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.getClass().getName().indexOf("HomeActivity") >= 0) {
                long now = System.currentTimeMillis();
                if (now - current < 2000) {
                    finish();
                    AcUtil.finish();
                    overridePendingTransition(com.android.common.R.anim.back_zoomin, com.android.common.R.anim.back_zoomout);
                    current = 0;
                } else {
                    toastShortLength("再按一次返回键退出");
                    current = now;
                }
            } else {
                finish();
                overridePendingTransition(com.android.common.R.anim.back_zoomin, com.android.common.R.anim.back_zoomout);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void setTopTitle(Integer id) {
        TextView title = getTitleTextView();
        if (title != null)
            title.setText(id);
    }

    private TextView getTitleTextView() {
        if (AppContextControll.app.cache.containsKey("title")) {
            Integer tid = (Integer) AppContextControll.app.cache.get("title");
            View view = findViewById(tid);
            if (view != null && view instanceof TextView)
                return (TextView) view;
        }
        return null;
    }

    @Override
    public void setTopTitle(String str) {
        TextView title = getTitleTextView();
        if (title != null)
            title.setText(str);
    }

}
