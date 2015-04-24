package com.android.core.controll;

import java.util.HashMap;
import java.util.Map;


public class BeanFactoryControll {
    private static BeanFactoryControll beanFactoryControll;

    private static Map<String, Object> beans = new HashMap<String, Object>();

    private static void registBean() {

    }

    private BeanFactoryControll() {
        registBean();
    }

    public static BeanFactoryControll getInstance() {
        return beanFactoryControll == null ? new BeanFactoryControll() : beanFactoryControll;
    }

    public Map<String, Object> getBeans() {
        return beans;
    }


}
