package com.drawguess.util;

/**
 * 计时进程类
 * @author GuoJun
 *
 */
public abstract class TimerUtils {
    private int mCount = 0;
    private boolean mExitFlag = false;
    private int mSleepTime = 1000; // 1s
    private Thread mThread = null;
    private int mTimeOutCount = 1;

    public TimerUtils() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mExitFlag) {
                    mCount++;
                    if (mCount < mTimeOutCount) {
                        doTimerCheckWork();
                        try {
                            Thread.sleep(mSleepTime);
                        }
                        catch (InterruptedException e) {
                            exit();
                        }
                    }
                    else {
                        doTimeOutWork();
                        exit();
                    }
                }
            }
        });
    }

    /**
     * 
     * @return 时间次数
     */
    public int getCount(){
    	return mCount;
    }
    
    public abstract void doTimeOutWork();

    /**
     * Do not process UI work in this.
     */
    public abstract void doTimerCheckWork();

    public void exit() {
        mExitFlag = true;
        mThread = null;
    }

    /**
     * start
     * @param times 
     * @param sleepTime
     */
    public void start(int timeOutCount, int sleepTime) {
        mTimeOutCount = timeOutCount;
        mSleepTime = sleepTime;
        mThread.start();
    }

}
