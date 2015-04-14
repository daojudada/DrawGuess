package com.drawguess.base;

import com.drawguess.R;
import com.drawguess.util.LogUtils;

import android.app.Application;
import android.app.Service;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

/**
 * 应用基类
 * @author Administrator
 *
 */
public class BaseApplication extends Application {

    public static boolean isDebugmode = false;
    private boolean isPrintLog = true;

    /** 静音、震动默认开关 **/
    private static boolean isSlient = false;
    private static boolean isVIBRATE = true;

    /** 新消息提醒 **/
    private static int notiSoundPoolID;
    private static SoundPool notiMediaplayer;
    private static Vibrator notiVibrator;


    private static BaseApplication instance;

    /**
     * <p>
     * 获取BaseApplication实例
     * <p>
     * 单例模式，返回唯一实例
     * 
     * @return instance
     */
    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (instance == null) {
            instance = this;
        }

        ActivitiesManager.init(getApplicationContext()); // 初始化活动管理器
        LogUtils.setLogStatus(isPrintLog); // 设置是否显示日志

        initNotification();

    }

  

    private void initNotification() {
        notiMediaplayer = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);
        notiSoundPoolID = notiMediaplayer.load(this, R.raw.crystalring, 1);
        notiVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtils.e("BaseApplication", "onLowMemory");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtils.e("BaseApplication", "onTerminate");
    }

    /* 设置声音提醒 */
    public static boolean getSoundFlag() {
        return !isSlient;
    }

    public static void setSoundFlag(boolean pIsSlient) {
        isSlient = pIsSlient;
    }

    /* 设置震动提醒 */
    public static boolean getVibrateFlag() {
        return isVIBRATE;
    }

    public static void setVibrateFlag(boolean pIsvibrate) {
        isVIBRATE = pIsvibrate;
    }

    /**
     * 新消息提醒 - 声音提醒、振动提醒
     */
    public static void playNotification() {
        if (!isSlient) {
            notiMediaplayer.play(notiSoundPoolID, 1, 1, 0, 0, 1);
        }
        if (isVIBRATE) {
            notiVibrator.vibrate(200);
        }

    }
}
