package com.drawguess.activity;

import java.util.Set;

import com.drawguess.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.drawguess.base.BaseActivity;
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
    * Member fields
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
		// TODO Auto-generated method stub
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        newDevicesListView.setOnRefreshListener(this);
        
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // If BT is not on, request that it be enabled.  
        // setupChat() will then be called during onActivityResult  
        if (!mBtAdapter.isEnabled()) {  
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);  
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);  
        }
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
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
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
            
            	// Create the result Intent and include the MAC address
            	Intent intent = new Intent();
            	
            	// Set result and finish this Activity
            	// setResult(Activity.RESULT_OK, intent);
        		// finish();  //make bluetooth connection in this activity or service instead
            	// or at least transfer the address to the activity that utilize the connection
            	
            	intent.setClass(BluetoothActivity.this, BtDrawGuessActivity.class);
            	Bundle bundle = new Bundle();
            	bundle.putString(EXTRA_DEVICE_ADDRESS, address);
            	intent.putExtras(bundle);
            	startActivity(intent);

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
	
	
	//public static 
}
