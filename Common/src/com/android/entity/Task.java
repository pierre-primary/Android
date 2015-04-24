package com.android.entity;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.core.controll.AppContextControll;
import com.android.core.controll.KennerControll;
import com.android.core.controll.KennerControll.TaskListener;
import com.android.http.HttpUtils;
import com.android.http.base.CFrameHttpUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Task extends BaseBean {
    public int taskId = 0;
    public Class serializableClass;
    public String url;
    public Class clazz;
    public Object obj;
    public String method;
    public String action;
    public Handler handler;
    public BaseActivity activity;
    public long startTime;
    public long completeTime;
    public Map<String, Object> params = new HashMap<String, Object>();
    private KennerControll.TaskListener listener;
    private static Task task;

    public Task() {
        // TODO Auto-generated constructor stub
    }

    public static Task optTask(int taskId, String url, Class serializableClass,Handler handler) {
        if (task == null)
            task = new Task(taskId, url, serializableClass, null);
        else {
            task.taskId = taskId;
            task.url = url;
            task.serializableClass = serializableClass;
        }
        task.handler = handler;
        return task;
    }

    public Task(int taskId, String url, Class serializableClass, BaseActivity activity) {
        this.taskId = taskId;
        this.activity = activity;
        this.url = url;
        this.method = "doVolleyTask";
        this.serializableClass = serializableClass;
        this.clazz = this.getClass();
        this.obj = this;
    }

    public Task(int taskId, String method, String url, Class serializableClass, BaseActivity activity) {
        this.taskId = taskId;
        this.activity = activity;
        this.url = url;
        this.method = method;
        this.serializableClass = serializableClass;
        this.clazz = this.getClass();
        this.obj = this;
    }

    public Task(Class clazz, String method, KennerControll.TaskListener listener) {
        this.clazz = clazz;
        this.method = method;
        this.listener = listener;

    }

    public Task(Class clazz, String method, Handler handler, KennerControll.TaskListener listener) {
        this.clazz = clazz;
        this.method = method;
        this.handler = handler;
        this.listener = listener;
    }

    public Task(Integer id, Class clazz, String method, Handler handler, KennerControll.TaskListener listener) {
        this.taskId = id;
        this.clazz = clazz;
        this.method = method;
        this.handler = handler;
        this.listener = listener;
    }

    public Task(Integer id, BaseActivity activity, Class clazz, String method, Handler handler, KennerControll.TaskListener listener) {
        this.taskId = id;
        this.clazz = clazz;
        this.method = method;
        this.handler = handler;
        this.listener = listener;
        this.activity = activity;
    }

    public Task(Integer id, Class clazz, String method, Handler handler, Map<String, Object> params, KennerControll.TaskListener listener) {
        this.taskId = id;
        this.clazz = clazz;
        this.method = method;
        this.handler = handler;
        this.params = params;
        this.listener = listener;
    }

    public Task(Integer id, Handler handler, Class clazz) {
        this.taskId = id;
        this.handler = handler;
        this.clazz = clazz;
    }

    public TaskListener getListener() {
        return listener;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "taskId :" + taskId + "\nclazz:" + clazz.getName() + "\nmethod:" + method + "\nstartTime:" + startTime + "    completeTime" + completeTime + "\nhandler:"
                + (handler != null ? handler.getClass().getName() : "") + "\naction:" + (action != null ? action : "");
    }

    public String doVolleyTask(Task task) {
        HttpUtils httpUtils = (HttpUtils) AppContextControll.app.cache.get("httpUtils");
        return httpUtils.http(task.url, Request.Method.GET, task.serializableClass, task);
    }

    public String doVolleyPost(Task task) {
        HttpUtils httpUtils = (HttpUtils) AppContextControll.app.cache.get("httpUtils");
        return httpUtils.http(task.url, Request.Method.POST, task.serializableClass, task);
    }


    public Object baseTask(Task task) throws Exception {
        this.obj = this;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Iterator<String> it = task.params.keySet().iterator();
        String url = "";
        while (it.hasNext()) {
            String key = it.next();
            if (key.equals("url") || key.equals("URL")) {
                url = (String) task.params.get(key);
            } else {
                params.add(new BasicNameValuePair(key, task.params.get(key).toString()));
            }
        }
        String ret = null;
        if (params.size() == 0)
            ret = CFrameHttpUtils.getInstance().httpGet(url);
        else
            ret = CFrameHttpUtils.getInstance().httpPost(url, params);
        if (ret != null) {
            if (ret.startsWith("{") && ret.endsWith("}")) {
                if (clazz != null) {
                    return JSON.parseObject(ret, clazz);
                }
                return new JSONObject(ret);
            } else if (ret.startsWith("[") && ret.endsWith("]")) {
                if (clazz != null) {
                    return com.alibaba.fastjson.JSONArray.parseArray(ret, clazz);
                }
                return new JSONArray(ret);
            }
        }
        return ret;
    }
}
