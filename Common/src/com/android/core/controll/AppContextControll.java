package com.android.core.controll;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.bitmap.BitmapInterface;
import com.android.db.CFrameDb;
import com.android.http.volley.VolleyHttpUtils;
import com.android.http.volley.bitmap.VolleyBitmap;
import com.android.http.volley.cache.CFrameVolley;
import com.android.util.Logs;
import com.facebook.drawee.backends.volley.VolleyDraweeControllerBuilderSupplier;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author cxd
 */
public abstract class AppContextControll extends Application implements
        Thread.UncaughtExceptionHandler {
    // public static KennerControll controll;
    public Map<Object, Object> cache;
    public static AppContextControll app;

    @Override
    public abstract void uncaughtException(Thread thread, Throwable ex);

    @Override
    public void onCreate() {
        Logs.i("AppContextControll.onCreate() === > ", "......");
        super.onCreate();
        app = this;
        Thread.setDefaultUncaughtExceptionHandler(this);
        init();

    }

    private void init() {


        try {
            // TODO
            cache = new LinkedHashMap<Object, Object>();
            KennerControll kennerControll = KennerControll.getInstance();
            kennerControll.setListener(new SimpleTaskListener());
            kennerControll.setCtx(getApplicationContext());
            cache.put("KennerControll", kennerControll);

            RequestQueue queue = CFrameVolley.newRequestQueue(this);
            cache.put("RequestQueue", queue);

            VolleyHttpUtils httpUtils = VolleyHttpUtils.getInstance(queue);
            cache.put("httpUtils", httpUtils);

            CFrameDb db = CFrameDb.create(this, "wetrip", true);
            cache.put("db", db);
            //m默认是 VolleyBitmap  可以手动切换到 CBitmap
            int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 12);
            ; //1/8允许内存作为缓存
            Logs.w("允许最大缓存内存：", cacheSize / 1024 / 1024 + " M");
            VolleyBitmap bitmapInterface = new VolleyBitmap(cacheSize, this, queue);
            cache.put("bitmap", bitmapInterface);

            ImageLoader imageLoader = bitmapInterface.getLoader() ; // build yourself
            VolleyDraweeControllerBuilderSupplier mControllerBuilderSupplier
                    = new VolleyDraweeControllerBuilderSupplier(this, imageLoader);
            SimpleDraweeView.initialize(mControllerBuilderSupplier);
           cache.put("Supplier",mControllerBuilderSupplier);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLowMemory() {
        Logs.w("AppContextControll.onLowMemory() === > ", "onLowMemory:");
        BitmapInterface bitmapInterface = (BitmapInterface) cache.get("bitmap");
        bitmapInterface.free();

        CFrameVolley.free();
        System.gc();
        super.onLowMemory();
    }
}
