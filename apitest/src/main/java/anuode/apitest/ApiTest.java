package anuode.apitest;

import com.anuode.conf.HTTPConfig;
import com.anuode.entity.Task;
import com.anuode.http.HttpUtils;
import com.anuode.util.Logs;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import anuode.apitest.hotel.Hotel;

/**
 * Created by xudong on 2015/3/26.
 */
public class ApiTest {

    public Object TestLogin(Task task) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Hotel h = mapper.readValue(json, Hotel.class);

        Logs.w("h::: ", h.toString());

        //api/common/mobiles/15757813001/sendVerifyCode
//        String url  = "http://dev.m.wetrip.com/home/show/2";
//        String url = "http://dev.m.wetrip.com/api/wetrip/adverts/2?show_type=2";
//        String url = "http://dev.m.wetrip.com/api/common/dictionary/152";
//        String url ="http://dev.m.wetrip.com/api/wetrip/near/supplier?longitude=112.412&latitude=125.21";
//        String url = "http://dev.m.wetrip.com/api/common/near/supplier?";
//        String url = "http://dev.m.wetrip.com/api/common/suppliers/hotel";
//        String url = "http://dev.m.wetrip.com/advister/2";
        String url = "http://dev.m.wetrip.com/api/weshop/suppliers/catering";
//        String url= "http://dev.m.wetrip.com/api/common/sequences/next?type=wt_user_info&pad=10";//获取customerId
//        String url = "http://dev.m.wetrip.com/api/auth/accounts";//注册
//        String url = "http://dev.m.wetrip.com/api/auth/mobiles/15757813001/check";
//        String url = "http://dev.m.wetrip.com/api/auth/tokens";//登陆
//        String url = "http://dev.m.wetrip.com/api/auth/mobiles/15757813001/sendVerifyCode";//发送手机验证码
        HttpUtils httpUtils = (HttpUtils) App.app.cache.get("httpUtils");
        task.params.put("param", "大虾");
//        task.params.put("longitude", "100");
//        task.params.put("latitude", "200");
//        task.params.put("request_page", "1");
//        task.params.put("clientType","1");
//        task.params.put("password", "az5212117");
//        task.params.put("status", "0");
//        task.params.put("srcType", "0");
//        task.params.put("defaultLoginType", "0");
//        task.params.put("customerId", "1000000027");
        return httpUtils.httpPostJson(url, null, task);
    }

    public void regist(Task task) {

    }

    String json = "{" +
            "    \"data\": {" +
            "        \"1\": [" +
            "            {" +
            "                \"room\": \"aaaa\"," +
            "                \"type\": 1" +
            "            }," +
            "            {" +
            "                \"room\": \"aaaa\"," +
            "                \"type\": 1" +
            "            }" +
            "        ]," +
            "        \"2\": [" +
            "            {" +
            "                \"room\": \"aaaa\"," +
            "                \"type\": 1" +
            "            }," +
            "            {" +
            "                \"room\": \"aaaa\"," +
            "                \"type\": 1" +
            "            }" +
            "        ]" +
            "    }" +
            "}";
}
