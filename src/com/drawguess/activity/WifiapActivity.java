package com.drawguess.activity;


import java.util.ArrayList;
import java.util.List;

import com.drawguess.R;
import com.drawguess.adapter.WifiapAdapter;
import com.drawguess.base.BaseActivity;
import com.drawguess.base.BaseDialog;
import com.drawguess.dialog.ConnWifiDialog;
import com.drawguess.util.LogUtils;
import com.drawguess.util.SessionUtils;
import com.drawguess.util.TextUtils;
import com.drawguess.util.WifiUtils;
import com.drawguess.util.WifiUtils.WifiCipherType;
import com.drawguess.wifiap.WifiApConst;
import com.drawguess.wifiap.WifiapBroadcast;
import com.drawguess.wifiap.WifiapBroadcast.NetWorkChangeListener;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @description wifi网络连接
 * @author GuoJun
 */
public class WifiapActivity extends BaseActivity implements OnClickListener, NetWorkChangeListener,
        OnScrollListener, OnItemClickListener {

   
    private static final String TAG = "WifiapActivity";
    private static ApHandler mHandler;
    private boolean isRespond = true;
    private String localIPaddress; // 本地WifiIP
    private Button mBtnBack;
    private Button mBtnCreateAp;
    private Button mBtnNext;
    private ConnWifiDialog mConnWifiDialog; // 连接热点窗口

    private BaseDialog mHintDialog; // 提示窗口
    private LinearLayout mLlApInfo;
    private ListView mLvWifiList;
    private SearchWifiThread mSearchWifiThread;
    private TextView mTvApSSID;
    private TextView mTvStatusInfo;
    private WifiapAdapter mWifiApAdapter;
    
    private WifiapBroadcast mWifiapBroadcast;

    private ArrayList<ScanResult> mWifiList; // 符合条件的热点列表

    private String serverIPaddres; // 热点IP

    /** 执行登陆 **/
    private void doLogin() {
        if (!isValidated()) {
            return;
        }
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    // 设置用户Session
                    SessionUtils.setIsClient(!WifiUtils.isWifiApEnabled());
                    SessionUtils.setLocalIPaddress(localIPaddress);
                    SessionUtils.setServerIPaddress(serverIPaddres);
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    startActivity(GameRoomActivity.class);
                }
                else {
                    showShortToast("操作失败,请检查网络是否正常。");
                }
            }
        });
    }

    /**
     * 获取Wifi热点名
     * 
     * <p>
     * BuildBRAND 系统定制商 ； BuildMODEL 版本
     * </p>
     * 
     * @return 返回 定制商+版本 (String类型),用于创建热点。
     */
    public String getLocalHostName() {
        String str1 = Build.BRAND;
        String str2 = TextUtils.getRandomNumStr(3);
        return str1 + "_" + str2;
    }

    private void getWifiList() {
        mWifiList.clear();
        WifiUtils.startScan();
        List<ScanResult> scanResults = WifiUtils.getScanResults();
        mWifiList.addAll(scanResults);
    }

    /** 初始化控件设置 **/
    protected void initAction() {

        if (!WifiUtils.isWifiConnect() && !WifiUtils.isWifiApEnabled()) { // 无开启热点无连接WIFI
            WifiUtils.OpenWifi();
        }

        if (WifiUtils.isWifiConnect()) { // Wifi已连接
            mTvStatusInfo.setText(getString(R.string.wifiap_text_wifi_connected)
                    + WifiUtils.getSSID());

        }

        if (WifiUtils.isWifiApEnabled()) { // 已开启热点
            if (WifiUtils.getApSSID().startsWith(WifiApConst.WIFI_AP_HEADER)) {
                mTvStatusInfo.setText(getString(R.string.wifiap_text_ap_1));
                mLvWifiList.setVisibility(View.GONE);
                mLlApInfo.setVisibility(View.VISIBLE);
                mTvApSSID.setText("SSID: " + WifiUtils.getApSSID());
                mBtnCreateAp.setText(getString(R.string.wifiap_btn_closeap));
            }
            else {
                WifiUtils.closeWifiAp();
                WifiUtils.OpenWifi();
                mTvStatusInfo.setText(getString(R.string.wifiap_text_wifi_1_0));
            }
        }

        if (WifiUtils.isWifiEnabled() && !WifiUtils.isWifiConnect()) { // Wifi已开启，未连接
            mTvStatusInfo.setText(getString(R.string.wifiap_text_wifi_1_0));
        }

        mSearchWifiThread.start();
    }

    /** 动态注册广播 */
    public void initBroadcast() {
        mWifiapBroadcast = new WifiapBroadcast(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(mWifiapBroadcast, filter);
    }

   

    /** 初始化全局设置 **/
    @Override
    protected void initEvents() {
        mWifiList = new ArrayList<ScanResult>();
        mWifiApAdapter = new WifiapAdapter(this, mWifiList);
        mLvWifiList.setAdapter(mWifiApAdapter);

        hintDialogOnClick hintClick = new hintDialogOnClick();

        mHintDialog = BaseDialog.getDialog(WifiapActivity.this, R.string.dialog_tips, "",
                getString(R.string.btn_yes), hintClick, getString(R.string.btn_cancel), hintClick);

        mHandler = new ApHandler();
        mConnWifiDialog = new ConnWifiDialog(this, mHandler);
        mSearchWifiThread = new SearchWifiThread(mHandler);
        mLvWifiList.setOnScrollListener(this);
        mLvWifiList.setOnItemClickListener(this);
        mBtnCreateAp.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
    }

    /** 初始化视图 获取控件对象 **/
    @Override
	protected void initViews() {
        mLlApInfo = (LinearLayout) findViewById(R.id.wifiap_lv_create_ok);
        mTvStatusInfo = (TextView) findViewById(R.id.wifiap_tv_wifistatus);
        mTvApSSID = (TextView) findViewById(R.id.wifiap_tv_createap_ssid);
        mLvWifiList = (ListView) findViewById(R.id.wifiap_lv_wifi);
        mBtnBack = (Button) findViewById(R.id.wifiap_btn_back);
        mBtnCreateAp = (Button) findViewById(R.id.wifiap_btn_createap);
        mBtnNext = (Button) findViewById(R.id.wifiap_btn_next);
    }

    /**
     * IP地址正确性验证
     * 
     * @return boolean 返回是否为正确， 正确(true),不正确(false)
     */
    private boolean isValidated() {

        setIPaddress();
        String nullIP = "0.0.0.0";

        if (nullIP.equals(localIPaddress) || nullIP.equals(serverIPaddres)
                || localIPaddress == null || serverIPaddres == null) {
            showShortToast(R.string.wifiap_toast_connectap_unavailable);
            return false;
        }

        return true;
    }

    /** 监听 主体界面按钮 **/
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

    	// 创建热点
        case R.id.wifiap_btn_createap:

            // 如果不支持热点创建
            if (WifiUtils.getWifiApStateInt() == 4) {
                showShortToast(R.string.wifiap_dialog_createap_nonsupport);
                return;
            }

            // 如果wifi正打开着的，就提醒用户
            if (WifiUtils.isWifiEnabled()) {
                mHintDialog
                        .setMessage(getString(R.string.wifiap_dialog_createap_closewifi_confirm));
                mHintDialog.show();
                return;
            }

            // 如果存在一个共享热点
            if (((WifiUtils.getWifiApStateInt() == 3) || (WifiUtils.getWifiApStateInt() == 13))
                    && (WifiUtils.getApSSID().startsWith(WifiApConst.WIFI_AP_HEADER))) {
                mHintDialog.setMessage(getString(R.string.wifiap_dialog_closeap_confirm));
                mHintDialog.show();
                return;
            }

            mHintDialog
                    .setMessage(getString(R.string.wifiap_dialog_createap_closewifi_confirm));
            mHintDialog.show();
            return;

            // 返回按钮
        case R.id.wifiap_btn_back:
            if (mHintDialog.isShowing()) {
                mHintDialog.dismiss();
            }
            finish();
            break;

        // 下一步按钮
        case R.id.wifiap_btn_next:
            if (mHintDialog.isShowing()) {
                mHintDialog.dismiss();
            }
            doLogin();
            break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifiap);
        initBroadcast(); // 注册广播
        initViews();
        initEvents();
        initAction();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mWifiapBroadcast); // 撤销广播
        mSearchWifiThread.stop();
        mSearchWifiThread = null;
        super.onDestroy();
    }
    
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ScanResult ap = mWifiList.get(position);
        if (ap.SSID.startsWith(WifiApConst.WIFI_AP_HEADER)) {
            mTvStatusInfo.setText(getString(R.string.wifiap_btn_connecting) + ap.SSID);
            // 连接网络
            boolean connFlag = WifiUtils.connectWifi(ap.SSID, WifiApConst.WIFI_AP_PASSWORD,
                    WifiCipherType.WIFICIPHER_WPA);
            if (!connFlag) {
                mTvStatusInfo.setText(getString(R.string.wifiap_toast_connectap_error_1));
                mHandler.sendEmptyMessage(WifiApConst.WiFiConnectError);
            }
        }
        else if (!WifiUtils.isWifiConnect() || !ap.BSSID.equals(WifiUtils.getBSSID())) {
            mConnWifiDialog.setTitle(ap.SSID);
            mConnWifiDialog.setScanResult(ap);
            mConnWifiDialog.show();
        }
    }
    

    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                setRespondFlag(true);
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                setRespondFlag(false); // 滚动时不刷新列表
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                setRespondFlag(false); // 滚动时不刷新列表
                break;
        }
    }

    /**
     * 刷新热点列表UI
     * 
     * @param list
     */
    public void refreshAdapter(List<ScanResult> list) {
        mWifiApAdapter.setData(list);
        mWifiApAdapter.notifyDataSetChanged();
    }

    private void setAsyncTask() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                WifiUtils.startWifiAp(WifiApConst.WIFI_AP_HEADER + getLocalHostName(),
                        WifiApConst.WIFI_AP_PASSWORD, mHandler);
				return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                dismissLoadingDialog();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog("正在创建热点");
            }
        });
    }

    /**
     * 设置IP地址信息
     * 
     * @param isClient
     *            是否为客户端
     */
    public void setIPaddress() {
        if (WifiUtils.isWifiApEnabled()) {
            serverIPaddres = localIPaddress = "192.168.43.1";
        }
        else {
            localIPaddress = WifiUtils.getLocalIPAddress();
            serverIPaddres = WifiUtils.getServerIPAddress();
        }
        LogUtils.i(TAG, "localIPaddress:" + localIPaddress + " serverIPaddres:" + serverIPaddres);
    }

    public void setRespondFlag(boolean flag) {
        isRespond = flag;
    }

    @Override
    public void WifiConnected() {
    	mHandler.sendEmptyMessage(WifiApConst.WiFiConnectSuccess);

    }
    
    @Override
    public void wifiStatusChange() {
    	mHandler.sendEmptyMessage(WifiApConst.NetworkChanged);

    }
    
    private class ApHandler extends Handler {
        public ApHandler() {
        }


        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case WifiApConst.ApScanResult: // 扫描Wifi列表
                    if (isRespond) {
                        getWifiList();
                        refreshAdapter(mWifiList);
                    }
                    break;

                case WifiApConst.ApCreateApSuccess: // 创建热点成功
                    mSearchWifiThread.stop();
                    mTvStatusInfo.setText(getString(R.string.wifiap_text_createap_succeed));
                    mLvWifiList.setVisibility(View.GONE);
                    mLlApInfo.setVisibility(View.VISIBLE);
                    mTvApSSID.setText("SSID: " + WifiUtils.getApSSID());
                    mBtnCreateAp.setText(getString(R.string.wifiap_btn_closeap));
                    mBtnBack.setClickable(true);
                    mBtnCreateAp.setClickable(true);
                    mBtnNext.setClickable(true);
                    break;

                case WifiApConst.WiFiConnectSuccess: // 连接热点成功
                    String str = getString(R.string.wifiap_text_wifi_connected)
                            + WifiUtils.getSSID();
                    mTvStatusInfo.setText(str);
                    showShortToast(str);
                    break;

                case WifiApConst.WiFiConnectError: // 连接热点错误
                    showShortToast(R.string.wifiap_toast_connectap_error);
                    break;

                case WifiApConst.NetworkChanged: // Wifi状态变化
                    if (WifiUtils.isWifiEnabled()) {
                        mTvStatusInfo.setText(getString(R.string.wifiap_text_wifi_1_0));
                    }
                    else {
                        mTvStatusInfo.setText(getString(R.string.wifiap_text_wifi_0));
                        showShortToast(R.string.wifiap_text_wifi_disconnect);
                    }

                default:
                    break;
            }
        }
    }

    public class hintDialogOnClick implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface hintDialog, int which) {
            switch (which) {

            // 确定
            case 0:
                hintDialog.dismiss();
                if (WifiUtils.isWifiApEnabled()) {

                    // 执行关闭热点事件
                    WifiUtils.closeWifiAp();
                    WifiUtils.OpenWifi();

                    showShortToast(R.string.wifiap_text_ap_0);
                    mTvStatusInfo.setText(getString(R.string.wifiap_text_wifi_1_0));
                    mBtnCreateAp.setText(getString(R.string.wifiap_btn_createap));
                    mLlApInfo.setVisibility(View.GONE);
                    mLvWifiList.setVisibility(View.VISIBLE);

                    localIPaddress = null;
                    serverIPaddres = null;

                    mSearchWifiThread.start();
                }
                else {
                    // 创建热点
                    mTvStatusInfo.setText(getString(R.string.wifiap_text_createap_creating));
                    mBtnBack.setClickable(false);
                    mBtnCreateAp.setClickable(false);
                    mBtnNext.setClickable(false);
                    setAsyncTask();
                }
                break;

            // 取消
            case 1:
                hintDialog.cancel();
                break;
            }
        }
    }
    /**
     * 定时刷新Wifi列表信息
     */
    class SearchWifiThread implements Runnable {
        private Handler handler = null;
        private boolean running = false;
        private Thread thread = null;

        SearchWifiThread(Handler handler) {
            this.handler = handler;
        }

        @Override
		public void run() {
            while (!WifiUtils.isWifiApEnabled()) {
                if (!this.running)
                    return;
                try {
                    Thread.sleep(2000); // 扫描间隔
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(WifiApConst.ApScanResult);
            }
        }

        public void start() {
            try {
                this.thread = new Thread(this);
                this.running = true;
                this.thread.start();
            }
            finally {
            }
        }

        public void stop() {
            try {
                this.running = false;
                this.thread = null;
            }
            finally {
            }
        }
    }

}
