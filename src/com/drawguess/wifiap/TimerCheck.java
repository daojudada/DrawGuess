package com.drawguess.wifiap;

/**
 * 时间检测类
 * @author copy from other
 *
 */
public abstract class TimerCheck {
    private int mCount = 0;
    private boolean mExitFlag = false;
    private int mSleepTime = 1000; // 1s
    private Thread mThread = null;
    private int mTimeOutCount = 1;

    public TimerCheck() {
        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
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
                    }
                }
            }
        });
    }

    public abstract void doTimeOutWork();

    /**
     * Do not process UI work in this.
     */
    public abstract void doTimerCheckWork();

    public void exit() {
        mExitFlag = true;
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
