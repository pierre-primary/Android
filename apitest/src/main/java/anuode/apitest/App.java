package anuode.apitest;

import com.anuode.core.controll.AppContextControll;

/**
 * Created by xudong on 2015/3/26.
 */
public class App extends AppContextControll {

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cache.put("Authorization","Bearer 0UfZ7V4W6S80tJ0TKP7N");
    }
}
