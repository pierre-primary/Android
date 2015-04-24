package com.android.http;

import com.android.entity.Task;


/**
 * Created by xudong on 2015/3/19.
 */
public interface HttpUtils {
    public String httpGetJson(String url, final Class clazz, final Task task);

    public String httpPostJson(String url, final Class clazz, final Task task);

    public String http(String url, int type, final Class clazz, final Task task);

//    public String postFile(String url,Map<String ,String> params,String fileParName,String file)throws Exception;
}
