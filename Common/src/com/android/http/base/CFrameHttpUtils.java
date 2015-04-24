package com.android.http.base;

import android.os.Message;

import com.android.volley.Request;
import com.android.entity.Task;
import com.android.http.HttpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public final class CFrameHttpUtils implements HttpUtils {

    private static CFrameHttpUtils util;
    private ObjectMapper mapper;

    private CFrameHttpUtils() {
        mapper = new ObjectMapper();
    }

    public static CFrameHttpUtils getInstance() {
        return util != null ? util : new CFrameHttpUtils();
    }

    /**
     * get : 0
     *
     * @param url
     * @param clazz
     * @param task
     * @return
     */
    @Override
    public String httpGetJson(String url, Class clazz, final Task task) {
        return http(url, Request.Method.GET, clazz, task);
    }

    /**
     * post : 1
     *
     * @param url
     * @param clazz
     * @param task
     * @return
     */
    @Override
    public String httpPostJson(String url, Class clazz, Task task) {
        return http(url, Request.Method.POST, clazz, task);
    }

    public synchronized String httpGet(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity());
        }
        return null;
    }

    public synchronized String httpPost(String url, List<NameValuePair> params) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() == 200) {
            String str = EntityUtils.toString(response.getEntity());
            return str;
        }
        return null;
    }

    @Override
    public String http(String url, int type, Class clazz, final Task task) {

        try {
            String ret = "";
            if (type == Request.Method.GET) {
                ret = httpGet(url);
            } else if (type == Request.Method.POST) {
                List<NameValuePair> params = (List<NameValuePair>) task.params.get("params");
                ret = httpPost(url, params);
            }

            final Object obj;
            if (clazz != null)
                obj = mapper.readValue(ret, clazz);
            else {
                return ret;
            }
            if (task != null && task.activity != null) {
                task.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = task.taskId;
                        msg.obj = obj;
                        task.activity.freash(msg);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /*
    public synchronized String minitlPost(String url, List<NameValuePair> params) throws Exception {
		HttpPost post = new HttpPost(url);
		post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			String str = EntityUtils.toString(response.getEntity());
			return str;
		}
		return null;
	}
	*/

    public synchronized void downLoad(String url, String fileName, String path, DownLoadListener listener) throws Exception {
        System.out.println("HttpUtil.downLoad() ====> " + "url : " + url + " \n fileName :" + fileName + " \n path:" + path);
        URLConnection conn = new URL(url).openConnection();
        int length = conn.getContentLength();
        InputStream in = conn.getInputStream();
        File f = null;
        if (fileName != null)
            f = new File(path + "/" + fileName);
        else
            f = new File((path + "/" + url.substring(url.lastIndexOf("/") + 1)));
        if (!f.exists()) {
            f.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(f);

        byte[] buffer = new byte[4096];
        int count = 0;
        int seek = 0;
        int lastSeek = 0;
        if (listener != null) {
            listener.onstart();
        }
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
            if (seek == 0 && listener != null) {
                listener.onSeekChange(0);
            }
            if (listener != null) {
                seek += count;
                int a = (int) ((double) seek / length * 100);
                if (a > lastSeek) {
                    lastSeek = a;
                    listener.onSeekChange(a);
                }
            }
        }
        listener.onComplate();
        in.close();
        out.close();
    }

    /**
     * 建议
     *
     * @param url
     * @param f
     * @param params
     * @param listener
     * @return
     */
    public synchronized String postFile(String url, File f, List<NameValuePair> params, CustomMultipartEntity.ProgressListener listener) throws Exception {
        File[] files = new File[]{f};
        return postFiles(url, files, params, listener);
    }

    /**
     * 上传文件请尽量使用单个文件上传， 一个一个的上传不容易出现上传失败
     *
     * @param url
     * @param files
     * @param params
     * @param listener
     * @return
     */
    @SuppressWarnings("deprecation")
    public synchronized String postFiles(String url, File[] files, List<NameValuePair> params, CustomMultipartEntity.ProgressListener listener) throws Exception {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        CustomMultipartEntity multipartContent = new CustomMultipartEntity(listener);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                multipartContent.addPart("upload[]", new FileBody(file, ContentType.create("application/octet-stream", Consts.UTF_8)));
            }
        }
        if (params != null) {
            for (NameValuePair nameValuePair : params) {
                multipartContent.addPart(nameValuePair.getName(), new StringBody(nameValuePair.getValue(), ContentType.create("text/plain", Consts.UTF_8)));
            }
        }
        listener.setContentLength(multipartContent.getContentLength());
        httpPost.setEntity(multipartContent);
        System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
        // 发起请求 并返回请求的响应
        HttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String str = EntityUtils.toString(resEntity, "utf-8");
                return str;
            }
        }
        return null;
    }


    /**
     * 上传文件请尽量使用单个文件上传， 一个一个的上传不容易出现上传失败
     *
     * @param url
     * @param files
     * @param parName  应该为  upload[]  这个的数组意义字符串
     * @param params
     * @param listener
     * @return
     */
    @SuppressWarnings("deprecation")
    public synchronized String postFiles(String url, File[] files, String parName, List<NameValuePair> params, CustomMultipartEntity.ProgressListener listener) throws Exception {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        CustomMultipartEntity multipartContent = new CustomMultipartEntity(listener);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                multipartContent.addPart(parName, new FileBody(file, ContentType.create("application/octet-stream", Consts.UTF_8)));
            }
        }
        if (params != null) {
            for (NameValuePair nameValuePair : params) {
                multipartContent.addPart(nameValuePair.getName(), new StringBody(nameValuePair.getValue(), ContentType.create("text/plain", Consts.UTF_8)));
            }
        }
        listener.setContentLength(multipartContent.getContentLength());
        httpPost.setEntity(multipartContent);
        System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
        // 发起请求 并返回请求的响应
        HttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String str = EntityUtils.toString(resEntity, "utf-8");
                return str;
            }
        }
        return null;
    }

    /**
     * 上传文件请尽量使用单个文件上传， 一个一个的上传不容易出现上传失败
     *
     * @param url
     * @param file
     * @param params
     * @param listener
     * @param parName  服务器端接收名
     * @return
     */
    @SuppressWarnings("deprecation")
    public synchronized String postFile(String url, File file, String parName, List<NameValuePair> params, CustomMultipartEntity.ProgressListener listener) throws Exception {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        CustomMultipartEntity multipartContent = new CustomMultipartEntity(listener);
        multipartContent.addPart(parName, new FileBody(file, ContentType.create("application/octet-stream", Consts.UTF_8)));
        if (params != null) {
            for (NameValuePair nameValuePair : params) {
                multipartContent.addPart(nameValuePair.getName(), new StringBody(nameValuePair.getValue(), ContentType.create("text/plain", Consts.UTF_8)));
            }
        }
        listener.setContentLength(multipartContent.getContentLength());
        httpPost.setEntity(multipartContent);
        System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
        // 发起请求 并返回请求的响应
        HttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String str = EntityUtils.toString(resEntity, "utf-8");
                return str;
            }
        }
        return null;
    }
}
