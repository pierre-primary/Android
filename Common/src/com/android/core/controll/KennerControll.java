package com.android.core.controll;

import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.android.entity.Task;
import com.android.util.Logs;

import java.lang.reflect.Method;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

//import com.alibaba.fastjson.JSON;

/**
 * <b> ����Ҫ���±�д����ִ��ģ�� </b> ��һ���汾Ҫ��дһ���ػ��̣߳�������������м��
 *
 * @author cxd
 */
public final class KennerControll {
    private TaskListener listener;
    private Context ctx;//APP

    public TaskListener getListener() {
        return listener;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    static {
        executorService = new ScheduledThreadPoolExecutor(2);
    }

    private static KennerControll kenner;


    public static KennerControll getInstance() {
        return kenner != null ? kenner : new KennerControll();
    }

    private static ExecutorService executorService;

    // public void doTaskQueueInOneThread(Queue<Task> queue) {
    // executorService.execute(new DoTask(queue));
    // }
    public void doTaskQueue(Queue<Task> queue) {
        while (!queue.isEmpty()) {
            doTask(queue.poll());
        }
    }

    public void doTask(Task task) {
        if (task.getListener() == null) {
            task.setListener(getListener());
        }
        task.startTime = System.currentTimeMillis();
        task.getListener().onStart(task);
        executorService.execute(new DoTask(task));
    }

    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    class DoTask implements Runnable {
        private Task task;
        private Queue<Task> queue;

        public DoTask(Task task) {
            this.task = task;
        }

        public DoTask(Queue<Task> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            if (queue != null) {
                while (!queue.isEmpty()) {
                    invoke(queue.poll());
                }
            } else {
                if (task != null) {
                    invoke(task);
                }
            }
        }

        private synchronized void invoke(final Task task) {
            try {
                Method method = task.clazz.getMethod(task.method,
                        task.getClass());
                final Object obj = method.invoke(task.obj == null ? task.clazz.newInstance() : task.obj, task);
                if (obj != null && "volley".equals(obj.toString()))
                    return;
                if (task.activity != null) {
                    task.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = task.taskId;
                            msg.obj = obj;
                            task.activity.freash(msg);
                        }
                    });
                    return;
                }
                if (task.handler != null) {
                    Message msg = task.handler.obtainMessage();
                    msg.what = task.taskId;
                    msg.obj = obj;
                    task.handler.sendMessage(msg);
                } else if (task.action != null && !task.action.equals("")
                        && ctx != null) {
                    Intent intent = new Intent(task.action);
                    intent.putExtra("json", obj.toString());
                    ctx.sendBroadcast(intent);
                }
                if (task.getListener() != null) {
                    task.params.put("obj", obj);
                    task.completeTime = System.currentTimeMillis();
                    task.getListener().onComplete(task);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (task.getListener() != null) {
                    task.completeTime = System.currentTimeMillis();
                    task.getListener().onError(task, e);
                }
                Logs.w("task.handler >> ", task.handler + " ");
                if (task.handler != null) {
                    Message msg = task.handler.obtainMessage();
                    msg.what = task.taskId;
                    task.handler.sendMessage(msg);
                    Logs.w("OnError >>>>>> ", task.taskId + "");
                }
            }
        }

    }

    /**
     * 任务监听器
     *
     * @author oopp1990
     */
    public interface TaskListener {
        public void onStart(Task task);

        public void onComplete(Task task);

        public void onError(Task task, Exception e);
    }

}
