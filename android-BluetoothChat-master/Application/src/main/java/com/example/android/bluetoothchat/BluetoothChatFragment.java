/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import java.util.Arrays;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;



    private EditText mSonar1;
    private EditText mSonar2;
    private EditText mSonar3;
    private EditText mSonar4;

    private EditText mSonar11;
    private EditText mSonar21;
    private EditText mSonar31;
    private EditText mSonar41;

    private EditText mSonar12;
    private EditText mSonar22;
    private EditText mSonar32;
    private EditText mSonar42;

    private EditText mSonar13;
    private EditText mSonar23;
    private EditText mSonar33;
    private EditText mSonar43;

    private EditText mSonar14;
    private EditText mSonar24;
    private EditText mSonar34;
    private EditText mSonar44;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);


        mSonar1 = (EditText) view.findViewById(R.id.sonar1);
        mSonar2 = (EditText) view.findViewById(R.id.sonar2);
        mSonar3 = (EditText) view.findViewById(R.id.sonar3);
        mSonar4 = (EditText) view.findViewById(R.id.sonar4);

        mSonar11 = (EditText) view.findViewById(R.id.sonar11);
        mSonar21 = (EditText) view.findViewById(R.id.sonar21);
        mSonar31 = (EditText) view.findViewById(R.id.sonar31);
        mSonar41 = (EditText) view.findViewById(R.id.sonar41);

        mSonar12 = (EditText) view.findViewById(R.id.sonar12);
        mSonar22 = (EditText) view.findViewById(R.id.sonar22);
        mSonar32 = (EditText) view.findViewById(R.id.sonar32);
        mSonar42 = (EditText) view.findViewById(R.id.sonar42);

        mSonar13 = (EditText) view.findViewById(R.id.sonar13);
        mSonar23 = (EditText) view.findViewById(R.id.sonar23);
        mSonar33 = (EditText) view.findViewById(R.id.sonar33);
        mSonar43 = (EditText) view.findViewById(R.id.sonar43);

        mSonar14 = (EditText) view.findViewById(R.id.sonar14);
        mSonar24 = (EditText) view.findViewById(R.id.sonar24);
        mSonar34 = (EditText) view.findViewById(R.id.sonar34);
        mSonar44 = (EditText) view.findViewById(R.id.sonar44);


    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
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
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }
    private int word_counter;
    private String pakiet;
    private String[] tablica_pakietow;
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    word_counter++;
                    if(word_counter==1 )
                    {
                        pakiet = "";
                    }
                    else if (word_counter==22)
                    {

                        if(readMessage.equals("E"))
                        {

                            tablica_pakietow = pakiet.split(";");
                            if(suma_kontrolna(tablica_pakietow[0],tablica_pakietow[1],tablica_pakietow[2],tablica_pakietow[3],tablica_pakietow[4])==1)
                            {
                                mSonar1.setText(tablica_pakietow[0]);
                                mSonar2.setText(tablica_pakietow[1]);
                                mSonar3.setText(tablica_pakietow[2]);
                                mSonar4.setText(tablica_pakietow[3]);

                                zmieniajKolory(tablica_pakietow[0],tablica_pakietow[1],tablica_pakietow[2],tablica_pakietow[3]);
                            }
                            else
                            {
                                mSonar1.setText("error");
                                mSonar2.setText("error");
                                mSonar3.setText("error");
                                mSonar4.setText("error");
                            }

                        }
                        pakiet="";
                        word_counter=0;
                        //Arrays.fill(tablica_pakietow, null);
                    }
                    else if(word_counter>1)
                    {
                       pakiet+=readMessage;
                        //stringbuiler
                    }


                    mSonar1.setBackgroundColor(0xfff0ff00);
                    mSonar2.setBackgroundColor(0xfff0ff00);
                    mSonar3.setBackgroundColor(0xfff0ff00);
                    mSonar4.setBackgroundColor(0xfff0ff00);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
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
                    setupChat();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }
    private int suma_kontrolna(String s1, String s2, String s3,String s4, String s5)
    {
        int son1 = Integer.parseInt(s1);
        int son2 = Integer.parseInt(s2);
        int son3 = Integer.parseInt(s3);
        int son4 = Integer.parseInt(s4);
        int suma = Integer.parseInt(s5);
        int wynik = son1+son2+son3+son4;

        if(wynik==suma)
        {
            return 1;
        }
        else
        {
            return 0;
        }


    }
    private void zmieniajKolory(String son1, String son2,String son3, String son4)
    {
        int so1 = Integer.parseInt(son1);
        int so2 = Integer.parseInt(son2);
        int so3 = Integer.parseInt(son3);
        int so4 = Integer.parseInt(son4);


        if(so1>0 && so1<=30)
        {
            mSonar11.setBackgroundColor(0xffff0000);
            mSonar12.setBackgroundColor(0xffff0000);
            mSonar13.setBackgroundColor(0xffff0000);
            mSonar14.setBackgroundColor(0xffff0000);
        }
        if(so2>0 && so2<=30)
        {
            mSonar21.setBackgroundColor(0xffff0000);
            mSonar22.setBackgroundColor(0xffff0000);
            mSonar23.setBackgroundColor(0xffff0000);
            mSonar24.setBackgroundColor(0xffff0000);
        }
        if(so3>0 && so3<=30)
        {
            mSonar31.setBackgroundColor(0xffff0000);
            mSonar32.setBackgroundColor(0xffff0000);
            mSonar33.setBackgroundColor(0xffff0000);
            mSonar34.setBackgroundColor(0xffff0000);
        }
        if(so4>0 && so4<=30)
        {
            mSonar41.setBackgroundColor(0xffff0000);
            mSonar42.setBackgroundColor(0xffff0000);
            mSonar43.setBackgroundColor(0xffff0000);
            mSonar44.setBackgroundColor(0xffff0000);
        }
        /////////////////////////////////////////////////////////////
        if(so1>30 && so1<=60)
        {
            mSonar11.setBackgroundColor(0xf00fff00);
            mSonar12.setBackgroundColor(0xffff0000);
            mSonar13.setBackgroundColor(0xffff0000);
            mSonar14.setBackgroundColor(0xffff0000);
        }
        if(so2>30 && so2<=60)
        {
            mSonar21.setBackgroundColor(0xf00fff00);
            mSonar22.setBackgroundColor(0xffff0000);
            mSonar23.setBackgroundColor(0xffff0000);
            mSonar24.setBackgroundColor(0xffff0000);
        }
        if(so3>30 && so3<=60)
        {
            mSonar31.setBackgroundColor(0xf00fff00);
            mSonar32.setBackgroundColor(0xffff0000);
            mSonar33.setBackgroundColor(0xffff0000);
            mSonar34.setBackgroundColor(0xffff0000);
        }
        if(so4>30 && so4<=60)
        {
            mSonar41.setBackgroundColor(0xf00fff00);
            mSonar42.setBackgroundColor(0xffff0000);
            mSonar43.setBackgroundColor(0xffff0000);
            mSonar44.setBackgroundColor(0xffff0000);
        }
        ///////////////////////////////////////////////////////////
        if(so1>60 && so1<=90)
        {
            mSonar11.setBackgroundColor(0xf00fff00);
            mSonar12.setBackgroundColor(0xf00fff00);
            mSonar13.setBackgroundColor(0xffff0000);
            mSonar14.setBackgroundColor(0xffff0000);
        }
        if(so2>60 && so2<=90)
        {
            mSonar21.setBackgroundColor(0xf00fff00);
            mSonar22.setBackgroundColor(0xf00fff00);
            mSonar23.setBackgroundColor(0xffff0000);
            mSonar24.setBackgroundColor(0xffff0000);
        }
        if(so3>60 && so3<=90)
        {
            mSonar31.setBackgroundColor(0xf00fff00);
            mSonar32.setBackgroundColor(0xf00fff00);
            mSonar33.setBackgroundColor(0xffff0000);
            mSonar34.setBackgroundColor(0xffff0000);
        }
        if(so4>60 && so4<=90)
        {
            mSonar41.setBackgroundColor(0xf00fff00);
            mSonar42.setBackgroundColor(0xf00fff00);
            mSonar43.setBackgroundColor(0xffff0000);
            mSonar44.setBackgroundColor(0xffff0000);
        }
        /////////////////////////////////////////////////////////
        if(so1>90 && so1<=120)
        {
            mSonar11.setBackgroundColor(0xf00fff00);
            mSonar12.setBackgroundColor(0xf00fff00);
            mSonar13.setBackgroundColor(0xf00fff00);
            mSonar14.setBackgroundColor(0xffff0000);
        }
        if(so2>90 && so2<=120)
        {
            mSonar21.setBackgroundColor(0xf00fff00);
            mSonar22.setBackgroundColor(0xf00fff00);
            mSonar23.setBackgroundColor(0xf00fff00);
            mSonar24.setBackgroundColor(0xffff0000);
        }
        if(so3>90 && so3<=120)
        {
            mSonar31.setBackgroundColor(0xf00fff00);
            mSonar32.setBackgroundColor(0xf00fff00);
            mSonar33.setBackgroundColor(0xf00fff00);
            mSonar34.setBackgroundColor(0xffff0000);
        }
        if(so4>90 && so4<=120)
        {
            mSonar41.setBackgroundColor(0xf00fff00);
            mSonar42.setBackgroundColor(0xf00fff00);
            mSonar43.setBackgroundColor(0xf00fff00);
            mSonar44.setBackgroundColor(0xffff0000);
        }
        ////////////////////////////////////////////////////////////
        if(so1>120)
        {
            mSonar11.setBackgroundColor(0xf00fff00);
            mSonar12.setBackgroundColor(0xf00fff00);
            mSonar13.setBackgroundColor(0xf00fff00);
            mSonar14.setBackgroundColor(0xf00fff00);
        }
        if(so2>120)
        {
            mSonar21.setBackgroundColor(0xf00fff00);
            mSonar22.setBackgroundColor(0xf00fff00);
            mSonar23.setBackgroundColor(0xf00fff00);
            mSonar24.setBackgroundColor(0xf00fff00);
        }
        if(so3>120)
        {
            mSonar31.setBackgroundColor(0xf00fff00);
            mSonar32.setBackgroundColor(0xf00fff00);
            mSonar33.setBackgroundColor(0xf00fff00);
            mSonar34.setBackgroundColor(0xf00fff00);
        }
        if(so4>120)
        {
            mSonar41.setBackgroundColor(0xf00fff00);
            mSonar42.setBackgroundColor(0xf00fff00);
            mSonar43.setBackgroundColor(0xf00fff00);
            mSonar44.setBackgroundColor(0xf00fff00);
        }



    }

}
