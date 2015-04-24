package com.android.core.controll;

import com.android.entity.Task;
import com.android.util.Logs;

public class SimpleTaskListener implements KennerControll.TaskListener {

    @Override
    public void onStart(Task task) {
        Logs.d("onStart()  ====>", task.toString());
    }

    @Override
    public void onComplete(Task task) {
        Logs.d("onComplete() ====>", task.toString());

    }

    @Override
    public void onError(Task task, Exception e) {
        // TODO Auto-generated method stub
        Logs.e("onError() ====>", task.toString());
        Logs.e("onError() ====>", e == null ? "null" : e.getMessage() + "   ...");
    }

}
