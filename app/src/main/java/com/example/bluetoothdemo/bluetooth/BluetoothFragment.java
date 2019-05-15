package com.example.bluetoothdemo.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetoothdemo.R;

import com.google.android.material.floatingactionbutton.FloatingActionButton;




public class BluetoothFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "BluetoothFragment";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView tv_state;
    private TextView tv_receive;
    private EditText ed_send;
    private Button btn_send;
    private FloatingActionButton float_btn_devices;
    private Button btn_clear_receive;
    private Switch sw_hex_asciiz_received;
    private Switch sw_hex_ascii_send;

    //flag fields
    private boolean sw_receive_hex_enable;
    private boolean sw_send_hex_enable;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;


    // Required empty public constructor
    public BluetoothFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (mBluetoothAdapter == null) {
                Toast.makeText(activity, "蓝牙不可使用！", Toast.LENGTH_LONG).show();
                activity.finish();
            }

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_state = view.findViewById(R.id.tv_state);
        tv_receive = view.findViewById(R.id.tv_receive);
        ed_send = view.findViewById(R.id.ev_send);
        btn_send = view.findViewById(R.id.btn_send);
        float_btn_devices = view.findViewById(R.id.float_button);
        btn_clear_receive = view.findViewById(R.id.btn_clear_receive);
        sw_hex_asciiz_received = view.findViewById(R.id.sw_hex_ascii);
        sw_hex_ascii_send = view.findViewById(R.id.sw_hex_ascii_send);

        //show the scrollbar when the text is too long
        tv_receive.setMovementMethod(ScrollingMovementMethod.getInstance());

        btn_send.setOnClickListener(this);
        btn_clear_receive.setOnClickListener(this);
        float_btn_devices.setOnClickListener(this);

        // enable the received hex mode
        sw_hex_asciiz_received.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sw_receive_hex_enable = isChecked;
            }
        });


        // enable the send hex mode
        sw_hex_ascii_send.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sw_send_hex_enable = isChecked;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupCommunication() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            //new a service and start communicate
            setupCommunication();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupCommunication() {
        Log.d(TAG, "setupCommunication()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {

            if (sw_send_hex_enable){
                message = EncodeUtil.charStr2hexStr(message);
            }

            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        tv_state.setText(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param state status
     */
    private void setStatus(CharSequence state) {
        tv_state.setText(state);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("StringFormatInvalid")
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(R.string.connected_to);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    //write message
                    // byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    int bytes = msg.arg1;
                    //we should transform data to the type what we want, ascii or hex
                    if (sw_receive_hex_enable) {
                        String temp = EncodeUtil.bytesToHexString(readBuf, bytes);
                        showReceivedMessage(temp);
                       // Log.e(TAG, "handleMessage: ---1--" + temp);
                    } else {
                        String temp = EncodeUtil.bytesToCharStr(readBuf, bytes);
                        showReceivedMessage(temp);
                       // Log.e(TAG, "handleMessage: ---2--" + temp);
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupCommunication();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    private void showReceivedMessage(String message) {
        String content;

        // Log.e(TAG, "showReceivedMessage: ---getLineCount---" + tv_receive.getLineCount());
        if (tv_receive.getLineCount() == tv_receive.getMaxLines()){
            content ="";
            tv_receive.setText("");
            Log.e(TAG, "showReceivedMessage: ---clear---" );
        }

        //if mode = hex, add backspace around the message
        if (sw_receive_hex_enable) {
            content = tv_receive.getText() + message + "\r";

        } else {
            content = tv_receive.getText() + message;
        }
        tv_receive.setText(content);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //After clicking the Send button, the text in the text box is sent to the connected Bluetooth device according to ASCII code.
            //The corresponding hexadecimal number is actually binary, such as a - - - ASCII code 97 - - - hexadecimal 61 - - - binary 0110 0001.
            case R.id.btn_send:
                String message = ed_send.getText().toString();
                sendMessage(message);
                break;
            case R.id.float_button:
                // Launch the DeviceListActivity to see devices and do scan
                ensureDiscoverable();
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                break;
            case R.id.btn_clear_receive:
                tv_receive.setText("");
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChatService.stop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


}
