package com.android.http.volley;

import android.content.Intent;
import android.os.Message;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.core.controll.AppContextControll;
import com.android.entity.Task;
import com.android.http.HttpUtils;
import com.android.util.Logs;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by xudong on 2015/3/19.
 */
public final class VolleyHttpUtils implements HttpUtils {
    public static final String volley = "volley";
    private RequestQueue queue;
    private static VolleyHttpUtils httpUtils;
    private ObjectMapper mapper;

    private VolleyHttpUtils() {
        mapper = new ObjectMapper();
    }

    public static VolleyHttpUtils getInstance(RequestQueue queue) {
        if (httpUtils == null) {
            httpUtils = new VolleyHttpUtils();
            httpUtils.queue = queue;
        }
        return httpUtils;
    }

    /**
     * @param url   url
     * @param clazz 反序列化类型
     * @param task  任务回调器
     * @throws Exception
     */
    public String httpGetJson(String url, final Class clazz, final Task task) {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.readValue()
        http(url, Request.Method.GET, clazz, task);
        return volley;
    }


    public String httpPostJson(String url, final Class clazz, final Task task) {
        http(url, Request.Method.POST, clazz, task);
        return volley;
    }
    private  String Authorization ;
    public synchronized String http(String url, final int type, final Class clazz, final Task task) {
        //Request.Method.GET
        Logs.w("url", url);
        JSONObject json = null;
        Authorization = null;
        if (task.params != null && task.params.size() > 0) {
            json = new JSONObject(task.params);
            Authorization = (String) task.params.get("Authorization");
            task.params.remove("Authorization");
        }



        JsonObjectRequest request = new JsonObjectRequest(type, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Logs.w("onResponse", "response:" + response);
                Object obj = null;
                if (clazz != null) {
                    try {
                        obj = mapper.readValue(response.toString(), clazz);
//                        obj = JSON.parseObject(response,clazz);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    obj = response;
                }
                if (task != null) {

                    callback(task, obj);
                }
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Logs.w("onErrorResponse" , "error" );
                task.getListener().onError(task, error);
                callback(task, null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers1 = super.getHeaders();
                Map<String, String> headers = new HashMap<String, String>();
                if (headers1 != null && headers1.size() > 0)
                    headers.putAll(headers1);
                if (Authorization != null) {
                    headers.put("Authorization", Authorization + "");
                } else {
                    if (AppContextControll.app.cache.containsKey("Authorization"))
                        headers.put("Authorization", AppContextControll.app.cache.get("Authorization") + "");
                }
                Logs.w("headers", new JSONObject(headers).toString());
                return headers;
            }
        };
        Logs.w("body>>>", request.getBodyContentType());
        queue.add(request);
        return volley;
    }

    private void callback(final Task task, Object obj) {
        if (task.activity != null) {
            final Message msg = new Message();
            msg.what = task.taskId;
            msg.obj = obj;
            task.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    task.activity.freash(msg);
                }
            });
        } else if (task.handler != null) {
            Message msg = task.handler.obtainMessage();
            msg.what = task.taskId;
            msg.obj = obj;
            task.handler.sendMessage(msg);
        } else if (task.action != null) {
            Intent intent = new Intent(task.action);
            intent.putExtra("data", obj.toString());
            AppContextControll.app.sendBroadcast(intent);
        }
        task.completeTime = System.currentTimeMillis();
        task.getListener().onComplete(task);
    }

}
