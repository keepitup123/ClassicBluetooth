package com.example.bluetoothdemo.bluetooth;

import android.os.Bundle;

import android.view.WindowManager;

import com.example.bluetoothdemo.R;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;


public class BlueToothActivity extends FragmentActivity {

    private static final String TAG = "BlueToothActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothFragment fragment = new BluetoothFragment();
            transaction.replace(R.id.bluetooth_content_fragment, fragment);
            transaction.commit();
        }

        //to adapt the soft_input_keyboard，when the keyboard pop from bottom
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_blue_tooth);


    }
}
