package com.drawguess.base;


import java.util.Stack;

import android.content.Context;

/**
 * 活动管理类
 * @author GuoJun
 *
 */
public class ActivitiesManager {

    private static Stack<BaseActivity> queue;
    public static void addActivity(BaseActivity activity) {
        queue.add(activity);
    }

    public static void finishActivity(BaseActivity activity) {
        if (activity != null) {
            queue.remove(activity);
        }
    }

    public static void finishAllActivities() {
        for (BaseActivity activity : queue) {
            activity.finish();
        }
        queue.clear();
    }

    public static int getActivitiesNum() {
        if (!queue.isEmpty()) {
            return queue.size();
        }
        return 0;
    }

    public static BaseActivity getCurrentActivity() {
        if (!queue.isEmpty()) {
            return queue.lastElement();
        }
        return null;
    }

    public static void init(Context context) {
        queue = new Stack<BaseActivity>();
    }



}
