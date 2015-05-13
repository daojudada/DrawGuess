package com.drawguess.activity;

import java.util.Set;

import org.json.JSONException;

import com.drawguess.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.drawguess.base.BaseActivity;
import com.drawguess.base.Constant;
import com.drawguess.bluetooth.BluetoothService;
import com.drawguess.interfaces.OnMsgRecListener;
import com.drawguess.net.MSGConst;
import com.drawguess.net.MSGProtocol;
import com.drawguess.util.LogUtils;
import com.drawguess.view.MultiListView;
import com.drawguess.view.MultiListView.OnRefreshListener;

/**
 * @description 蓝牙连接
 * @author ChenJianyan
 */

public class BluetoothActivity extends BaseActivity implements OnClickListener, OnRefreshListener  {


	/**
    * Tag for Log
    */
   private static final String TAG = "BluetoothActivity";

   /**
    * Return Intent extra
    */
   public static String EXTRA_DEVICE_ADDRESS = "device_address";
   

   /**
    * Name of the connected device
    */
   private String mConnectedDeviceName = null;
   private String saveStr = "";

   private boolean isMeDraw = false,isClick = false;

	/*
	 * 蓝牙连接服务类对象
	 */
	private BluetoothService mBtService;
	
	/*
	 * 蓝牙适配器对象
	 */
	private BluetoothAdapter mBtAdapter;

	/**
	 * Newly discovered devices
	 */
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
   
	private MultiListView newDevicesListView;
	private ListView pairedListView;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private Set<BluetoothDevice> pairedDevices;
   
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
   

   
	@Override
	protected void initEvents() {

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
		// If BT is not on, request that it be enabled.  
        // setupChat() will then be called during onActivityResult  
        if (!mBtAdapter.isEnabled()) {  
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);  
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);  
        }
        
		// fill two views with devices that might be available for connection
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        newDevicesListView.setOnRefreshListener(this);
        
        Button btnBack = (Button)findViewById(R.id.bluetooth_btn_back);
        btnBack.setOnClickListener(this);
        Button btnChange = (Button)findViewById(R.id.bluetooth_btn_change);
        btnChange.setOnClickListener(this);
        Button btnCancelDiscovery = (Button)findViewById(R.id.bluetooth_btn_cancel_discovery);
        btnCancelDiscovery.setOnClickListener(this);
        
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.listitem_device);
		
		// Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.listitem_device);

        // Find and set up the ListView for paired devices
        pairedListView = (ListView) findViewById(R.id.bluetooth_paired_devices);
        

        // Find and set up the ListView for newly discovered devices
        newDevicesListView = (MultiListView) findViewById(R.id.bluetooth_other_devices);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.bluetooth_btn_back:
			finish();
			break;
			
		case R.id.bluetooth_btn_cancel_discovery:
			if (mBtAdapter != null) {
				mBtAdapter.cancelDiscovery();
			}
			break;
			
		case R.id.bluetooth_btn_change:
			LogUtils.i(TAG, "localdevicename : "+mBtAdapter.getName()+" localdeviceAddress : "+mBtAdapter.getAddress());
            // mBtAdapter.setName("NewDeviceName");
			// Planned: pop up a new dialog and user input new device name in it.
            LogUtils.i(TAG, "localdevicename : "+mBtAdapter.getName()+" localdeviceAddress : "+mBtAdapter.getAddress());
			break;
		
		}
	}
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_bluetooth);

        initViews();
        initEvents();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        

        // Get a set of currently paired devices
        pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
        	pairedListView.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.bluetooth_none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
        

        doDiscovery();
    }
    
    @Override
    public void onStart() {
        super.onStart();
            // Otherwise, setup the chat session
        if (mBtService == null) {
            setupChat();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBtService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBtService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
            	mBtService.start();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }
    
    public void tryConnecting(int requestCode, String address) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                    connectDevice(address, true);
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                    connectDevice(address, false);
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
        }
    }
    
    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        LogUtils.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.bluetooth_scanning);

        // Turn on sub-title for new devices
        newDevicesListView.setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }
    

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            if (info.length() > 17) {
            	String address = info.substring(info.length() - 17); //Problematic statement when info.length() < 17 // temporarily solved
            
            	isMeDraw = true;
            	tryConnecting(REQUEST_CONNECT_DEVICE_SECURE, address);

            }
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
    		newDevicesListView.onRefreshComplete();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.bluetooth_select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.bluetooth_none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

	@Override
	public void onRefresh() {
		doDiscovery();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
		}
	}
	
	private void connectDevice(String address, boolean secure) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBtService.connect(device, secure);
        //mBtService.sendMessage(MSGConst.SEND_START, null);
    }
	
	
	
	private void setupChat() {
        LogUtils.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBtService = BluetoothService.getInstance(mHandler);
        mBtService.setListener(new OnMsgRecListener(){
			@Override
			public void processMessage(MSGProtocol ipmsg) {
                if(ipmsg.getCommandNo() == MSGConst.SEND_START){
                    Bundle b = new Bundle();
                    b.putBoolean("isMeDraw", isMeDraw);
                    startActivity(BtDrawGuessActivity.class,b);
                }
			}
        });

    }
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mBtService.sendMessage(MSGConst.SEND_START, null);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
       
                case Constant.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constant.DEVICE_NAME);
                    if (null != BluetoothActivity.this) {
                        showCustomToast( "Connected to " + mConnectedDeviceName);
                    }
                    break;
                case Constant.MESSAGE_TOAST:
                    if (null != BluetoothActivity.this) {
                    	if(isMeDraw)
                    		isMeDraw = false;
                        showCustomToast( msg.getData().getString(Constant.TOAST));
                    }
                    break;
            }
        }
    };
	
}
