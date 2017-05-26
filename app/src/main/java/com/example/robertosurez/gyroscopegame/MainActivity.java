package com.example.robertosurez.gyroscopegame;


import android.app.Activity;
import android.app.Dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidifygeeks.library.fragment.PageFragment;
import com.androidifygeeks.library.fragment.TabDialogFragment;
import com.androidifygeeks.library.iface.IFragmentListener;
import com.androidifygeeks.library.iface.ISimpleDialogCancelListener;
import com.androidifygeeks.library.iface.ISimpleDialogListener;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ISimpleDialogListener, ISimpleDialogCancelListener, IFragmentListener {

    Button buttonConect;
    Button buttonPlay;
    private TextView status;

    private Dialog dialog;

    private static final int REQUEST_TABBED_DIALOG = 42;

    private static final String TAG = MainActivity.class.getSimpleName();

    private final Set<Fragment> mMyScheduleFragments = new HashSet<>();


    /**
     * Bluetooth
     */
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    public static BluetoothController BTcontroller;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonConect = (Button) findViewById(R.id.buttonConnect);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        status = (TextView)  findViewById(R.id.textStatus);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }


        //show bluetooth devices dialog when click connect button
        buttonConect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabDialogFragment.createBuilder(MainActivity.this, getSupportFragmentManager())
                        .setTitle("How to play")
                        .setTabButtonText(new CharSequence[]{"Step 1", "Step 2", "Step 3"})
                        .setPositiveButtonText("Play")
                        .setNegativeButtonText("Cancel")
                        .setRequestCode(REQUEST_TABBED_DIALOG)
                        .show();

            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    BTcontroller = new BluetoothController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);


        }else {
            BTcontroller = new BluetoothController(this, handler);
        }
        visible();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (BTcontroller != null) {
            if (BTcontroller.getState() == BluetoothController.STATE_NONE) {
                BTcontroller.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BTcontroller != null)
            BTcontroller.stop();
    }


    @Override
    public void onCancelled(int requestCode) {

    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {

    }

    @Override
    public void onNeutralButtonClicked(int requestCode) {

    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        if (requestCode == REQUEST_TABBED_DIALOG) {
            Intent intent = new Intent(MainActivity.this, DeviceOrientation.class);
            startActivity(intent);
        }
    }

    @Override
    public void onFragmentViewCreated(Fragment fragment) {
        int selectedTabPosition = fragment.getArguments().getInt(PageFragment.ARG_DAY_INDEX, 0);
        View rootContainer = fragment.getView().findViewById(R.id.root_container);
        Log.i(TAG, "Position: " + selectedTabPosition);

        switch (selectedTabPosition) {
            case 0:
                selectedTabPositionZeroCase(rootContainer);
                break;
            case 1:
                selectedTabPositionOneCase(rootContainer);
                break;
            case 2:
                selectedTabPositionTwoCase(rootContainer);
                break;
            default:
                break;
        }


    }



    private void selectedTabPositionZeroCase(View rootContainer) {
        // add view in container for first tab
        View tabProductDetailLayout = getLayoutInflater().inflate(R.layout.tab_one_layout, (ViewGroup) rootContainer);

        TextView textView = (TextView) tabProductDetailLayout.findViewById(R.id.text_view);
        ImageView image = (ImageView) tabProductDetailLayout.findViewById(R.id.image);
        textView.setText("Hold the device.\nDo not let the device in a surface!");
        image.setImageResource(R.drawable.tab1opt);
    }

    private void selectedTabPositionOneCase(View rootContainer) {
        // add view in container for second tab
        View tabProductDetailLayout2 = getLayoutInflater().inflate(R.layout.tab_one_layout, (ViewGroup) rootContainer);

        TextView textView1 = (TextView) tabProductDetailLayout2.findViewById(R.id.text_view);
        textView1.setText("Turn the device to the right, \nleft or upside down");
        ImageView image = (ImageView) tabProductDetailLayout2.findViewById(R.id.image);
        image.setImageResource(R.drawable.tab2opt);
    }

    private void selectedTabPositionTwoCase(View rootContainer) {
        View tabProductDetailLayout2 = getLayoutInflater().inflate(R.layout.tab_one_layout, (ViewGroup) rootContainer);

        TextView textView1 = (TextView) tabProductDetailLayout2.findViewById(R.id.text_view);
        textView1.setText("You can see the movement you have made, \nand the movement of the other person");
        ImageView image = (ImageView) tabProductDetailLayout2.findViewById(R.id.image);
        image.setImageResource(R.drawable.tab3);
    }

    @Override
    public void onFragmentAttached(Fragment fragment) {
        mMyScheduleFragments.add(fragment);
    }

    @Override
    public void onFragmentDetached(Fragment fragment) {
        mMyScheduleFragments.remove(fragment);
    }

    private void visible() {
        if (bluetoothAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(), "Bluetooth is on",Toast.LENGTH_LONG).show();
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(getVisible, 0);
        }
    }


    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }



    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        BTcontroller.connect(device);
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothController.STATE_CONNECTED:
                            setStatus("Status: \nConnected to " + connectingDevice.getName());
                            buttonPlay.setEnabled(true);
                            break;
                        case BluetoothController.STATE_CONNECTING:
                            setStatus("Status: \nConnecting...");
                            buttonPlay.setEnabled(false);
                            break;
                        case BluetoothController.STATE_LISTEN:
                        case BluetoothController.STATE_NONE:
                            setStatus("Status: \nNot connected");
                            buttonPlay.setEnabled(false);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    //Do nothing

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    try{
                        DeviceOrientation.setDirectionReceive(readMessage);
                    }catch (NullPointerException e){

                    }

                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });


    private void setStatus(String s) {
        status.setText(s);
    }


    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };



}
