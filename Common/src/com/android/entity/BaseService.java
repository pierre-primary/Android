package com.android.entity;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class BaseService extends IntentService {
    public BaseService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Ibinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class Ibinder extends Binder implements MyBinder {
        @Override
        public Service getService() {
            return BaseService.this;
        }
    }

    public interface MyBinder {
        public Service getService();
    }

}
