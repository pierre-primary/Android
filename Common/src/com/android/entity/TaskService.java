package com.android.entity;

import android.content.BroadcastReceiver;
import android.os.HandlerThread;

import com.android.core.controll.AppContextControll;
import com.android.core.controll.KennerControll;
import com.android.util.Logs;


/**
 * Created by xudong on 2015/3/18.
 */
public class TaskService extends BaseService {
    private HandlerThread handlerThread;
    private BroadcastReceiver receiver;
    private KennerControll controll;

    public TaskService() {
        super("com.android.wetrip.service.LocalService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.w("Service", "Oncreate...");
        controll = (KennerControll) AppContextControll.app.cache.get("KennerControll");
        handlerThread = new HandlerThread("handler");
        handlerThread.start();

//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        receiver = new NetWorkReceiver();
//        registerReceiver(receiver,filter);

    }

    public void doTask(Task task) {
        controll.doTask(task);
    }


    @Override
    public void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }

}
