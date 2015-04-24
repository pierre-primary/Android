package com.android.util;

import android.os.Process;

import com.android.entity.BaseActivityInterface;

import java.util.ArrayList;
import java.util.List;

public class AcUtil {
    public static List<BaseActivityInterface> list = new ArrayList<BaseActivityInterface>();

    public static void add(BaseActivityInterface ac) {
        list.add(ac);
    }

    public static void remove(BaseActivityInterface ac) {
        list.remove(ac);
    }

    public static void finish() {
        for (BaseActivityInterface ac : list) {
            ac.finish();
        }
        Process.killProcess(Process.myPid());
    }

}
